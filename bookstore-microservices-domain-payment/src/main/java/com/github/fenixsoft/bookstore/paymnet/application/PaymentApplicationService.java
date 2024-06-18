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

package com.github.fenixsoft.bookstore.paymnet.application;

import com.github.fenixsoft.bookstore.dto.Settlement;
import com.github.fenixsoft.bookstore.paymnet.domain.Payment;
import com.github.fenixsoft.bookstore.paymnet.domain.PaymentService;
import com.github.fenixsoft.bookstore.paymnet.domain.client.ProductServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

/**
 * 支付应用务
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/12 16:29
 **/
@Named
@Transactional
public class PaymentApplicationService {

    @Inject
    private PaymentService paymentService;

    @Inject
    private ProductServiceClient productService;

    /**
     * 根据结算清单的内容执行，生成对应的支付单
     */
    public Payment executeBySettlement(Settlement bill) {
        // 冻结库存（保证有货提供），从服务中获取商品的价格，计算要支付的总价（安全原因，这个不能由客户端传上来）
        productService.frozenAndReplenishProducts(bill);
        // 生成付款单
        Payment payment = paymentService.producePayment(bill);
        // 设立解冻定时器（超时未支付则释放冻结的库存和资金）
        paymentService.setupAutoThawedTrigger(payment);
        return payment;
    }

    /**
     * 完成支付
     * 保存已支付的状态，异步执行库存扣减
     */
    public void accomplishPayment(Integer accountId, String payId) {
        paymentService.accomplish(payId);
    }

    /**
     * 取消支付
     * 保存已取消的状态，异步执行库存解冻
     */
    public void cancelPayment(String payId) {
        paymentService.cancel(payId);
    }

}
