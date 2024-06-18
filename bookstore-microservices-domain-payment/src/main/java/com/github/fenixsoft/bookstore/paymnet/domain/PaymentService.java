/*
 * Copyright 2012-2020. the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. More information from:
 *
 *        https://github.com/fenixsoft
 */

package com.github.fenixsoft.bookstore.paymnet.domain;

import com.github.fenixsoft.bookstore.dto.Item;
import com.github.fenixsoft.bookstore.dto.Settlement;
import com.github.fenixsoft.bookstore.infrastructure.jaxrs.CodedMessage;
import com.github.fenixsoft.bookstore.paymnet.domain.client.ProductServiceClient;
import com.github.fenixsoft.bookstore.paymnet.infrastructure.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;

import javax.annotation.Resource;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Iterator;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

/**
 * 支付单相关的领域服务
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/12 23:24
 **/
@Named
public class PaymentService {
    /**
     * 默认支付单超时时间：2分钟（缓存TTL时间的一半）
     */
    private static final long DEFAULT_PRODUCT_FROZEN_EXPIRES = CacheConfiguration.SYSTEM_DEFAULT_EXPIRES / 2;

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final Timer timer = new Timer();

    @Inject
    private ProductServiceClient stockpileService;

    @Inject
    private PaymentRepository paymentRepository;

    @Inject
    private PaymentErrorRepository peRepository;

    @Resource(name = "settlement")
    private Cache settlementCache;


    /**
     * 生成支付单
     * <p>
     * 根据结算单的货物，计算总价，生成支付单
     */
    public Payment producePayment(Settlement bill) {
        Double total = bill.getItems().stream().mapToDouble(i -> bill.productMap.get(i.getProductId()).getPrice() * i.getAmount()).sum() + 12;   // 12元固定运费，客户端写死的，这里陪着演一下，避免总价对不上
        Payment payment = new Payment(total, DEFAULT_PRODUCT_FROZEN_EXPIRES);
        paymentRepository.save(payment);
        // 将支付单存入缓存
        settlementCache.put(payment.getPayId(), bill);
        log.info("创建支付订单，总额：{}", payment.getTotalPrice());
        return payment;
    }

    public void accomplish(String payId) {
        synchronized (payId.intern()) {
            Payment payment = paymentRepository.getByPayId(payId);
            if (payment.getPayState() == Payment.State.WAITING) {
                payment.setPayState(Payment.State.PAYED);
                paymentRepository.save(payment);
                log.info("编号为{}的支付单已支付，等待扣减库存", payId);
            } else {
                throw new UnsupportedOperationException("当前订单不允许支付，当前状态为：" + payment.getPayState());
            }
        }
        accomplishSettlement(Payment.State.PAYED, payId);
    }

    public void cancel(String payId) {
        synchronized (payId.intern()) {
            Payment payment = paymentRepository.getByPayId(payId);
            if (payment.getPayState() == Payment.State.WAITING) {
                payment.setPayState(Payment.State.CANCEL);
                paymentRepository.save(payment);
                log.info("编号为{}的支付单已被取消", payId);
            } else {
                throw new UnsupportedOperationException("当前订单不允许取消，当前状态为：" + payment.getPayState());
            }
        }
        accomplishSettlement(Payment.State.CANCEL, payId);
    }

    /**
     * 设置支付单自动冲销解冻的触发器
     * <p>
     * 如果在触发器超时之后，如果支付单未仍未被支付（状态是WAITING）
     * 则自动执行冲销，将冻结的库存商品解冻，以便其他人可以购买，并将Payment的状态修改为TIMEOUT。
     * <p>
     * 注意：
     * 使用TimerTask意味着节点带有状态，这在分布式应用中是必须明确【反对】的，如以下缺陷：
     * 1. 如果要考虑支付订单的取消场景，无论支付状态如何，这个TimerTask到时间之后都应当被执行。不应尝试使用TimerTask::cancel来取消任务。
     * 因为只有带有上下文状态的节点才能完成取消操作，如果要在集群中这样做，就必须使用支持集群的定时任务（如Quartz）以保证多节点下能够正常取消任务。
     * 2. 如果节点被重启、同样会面临到状态的丢失，导致一部分处于冻结的触发器永远无法被执行，所以需要系统启动时根据数据库状态有一个恢复TimeTask的的操作
     * 3. todo 即时只考虑正常支付的情况，真正生产环境中这种代码需要一个支持集群的同步锁（如用Redis实现互斥量），避免解冻支付和该支付单被完成两个事件同时在不同的节点中发生
     */
    public void setupAutoThawedTrigger(Payment payment) {
        String payId = payment.getPayId();
        timer.schedule(new TimerTask() {
            public void run() {
                synchronized (payId.intern()) {
                    // 使用2分钟之前的Payment到数据库中查出当前的Payment
                    Payment currentPayment = paymentRepository.getByPayId(payId);
                    if (currentPayment.getPayState() == Payment.State.WAITING) {
                        payment.setPayState(Payment.State.TIMEOUT);
                        paymentRepository.save(payment);
                        log.info("编号为{}的支付单已超时，状态转变为TIMEOUT", payId);
                    }
                }
                accomplishSettlement(Payment.State.TIMEOUT, payment.getPayId());
            }
        }, payment.getExpires());
    }

    /**
     * 根据订单完成状态，完成支付结算单
     * <p>
     * 注意：
     * 在这是一套很不严谨的类TCC事务，仅作演示，没有BEST-EFFORT的重试，也没有将支付ID传递过去以保证幂等性，不能在正式项目中照搬
     * done 正式项目中，一般不会自己零开始写分布式事务，而是引入一套事务中间件（譬如Seata），具体分布式事务的内容请参考文档
     */
    private void accomplishSettlement(Payment.State endState, String payId) {
        CompletableFuture.runAsync(() -> {
                    boolean done = doAccomplishSettlement(endState, payId);
                    // 最大努力交付
                    for (int i = 0; i < 2 && !done; i++) {
                        done = doAccomplishSettlement(endState, payId);
                    }
                    if (done) {
                        Payment payment = paymentRepository.getByPayId(payId);
                        payment.setPayState(Payment.State.ACCOMPLISHED);
                        paymentRepository.save(payment);
                        log.info("编号为{}，状态为{} 的支付单已清算库存", payId, endState);
                    } else {
                        // 模拟人工介入
                        PaymentError pe = new PaymentError();
                        pe.setPayId(payId);
                        Settlement settlement = (Settlement) Objects.requireNonNull(Objects.requireNonNull(settlementCache.get(payId)).get());
                        settlement.getItems().forEach(i -> {
                            pe.setProductId(i.getProductId());
                            pe.setAmount(i.getAmount());
                            peRepository.save(pe);
                        });
                    }
                    // 清除缓存
                    settlementCache.evict(payId);
                })
                .exceptionally(ex -> {
                    log.error(ex.getMessage(), ex);
                    return null;
                });
    }

    /**
     * 根据订单完成状态，执行库存调整（扣减库存或者解冻），返回成功标志
     */
    private boolean doAccomplishSettlement(Payment.State endState, String payId) {
        Settlement settlement = (Settlement) Objects.requireNonNull(Objects.requireNonNull(settlementCache.get(payId)).get());
        Iterator<Item> ii = settlement.getItems().iterator();
        while (ii.hasNext()) {
            Item i = ii.next();
            CodedMessage cm;
            if (endState == Payment.State.PAYED) {
                cm = stockpileService.decrease(i.getProductId(), i.getAmount(), payId);
            } else {
                // 其他状态，无论是TIMEOUT还是CANCEL，都进行解冻
                cm = stockpileService.thawed(i.getProductId(), i.getAmount(), payId);
            }
            if (CodedMessage.CODE_SUCCESS.equals(cm.getCode())) {
                ii.remove();
            }
        }
        return settlement.getItems().isEmpty();
    }

}
