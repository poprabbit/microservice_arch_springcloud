package com.github.fenixsoft.bookstore.warehouse.application;

import com.github.fenixsoft.bookstore.domain.warehouse.DeliveredStatus;
import com.github.fenixsoft.bookstore.domain.warehouse.Product;
import com.github.fenixsoft.bookstore.domain.warehouse.Stockpile;
import com.github.fenixsoft.bookstore.dto.Item;
import com.github.fenixsoft.bookstore.infrastructure.jaxrs.IdempotenceException;
import com.github.fenixsoft.bookstore.warehouse.domain.PaymentStockpile;
import com.github.fenixsoft.bookstore.warehouse.domain.PaymentStockpileRepository;
import com.github.fenixsoft.bookstore.warehouse.domain.StockpileService;
import org.springframework.dao.DataIntegrityViolationException;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品库存的领域服务
 *
 * @author icyfenix@gmail.com
 * @date 2020/4/19 21:42
 **/
@Named
@Transactional
public class StockpileApplicationService {

    @Inject
    private StockpileService stockpileService;

    @Inject
    private PaymentStockpileRepository paymentStockpileRepository;

    /**
     * 根据产品查询库存
     */
    public Stockpile getStockpile(Integer productId) {
        return stockpileService.getByProductId(productId);
    }

    /**
     * 将指定的产品库存调整为指定数额
     */
    public void setStockpileAmountByProductId(Integer productId, Integer amount) {
        stockpileService.set(productId, amount);
    }

    /**
     * 调整商品出库状态
     */
    public void setDeliveredStatus(Integer productId, DeliveredStatus status, Integer amount, String payId) {
        try {
            // 唯一主键保证幂等
            paymentStockpileRepository.save(new PaymentStockpile(payId, productId, status.name()));
        } catch (DataIntegrityViolationException e) {
            String msg = String.format("PaymentStockpile Idempotent, payId: %s, productId: %s, status: %s", payId, productId, status);
            throw new IdempotenceException(msg);
        }
        switch (status) {
            case DECREASE:
                stockpileService.decrease(productId, amount);
                break;
            case INCREASE:
                stockpileService.increase(productId, amount);
                break;
            case FROZEN:
                stockpileService.frozen(productId, amount);
                break;
            case THAWED:
                stockpileService.thawed(productId, amount);
                break;
        }
    }

    /**
     * 冻结库存并填充商品信息
     */
    public List<Product> frozenAndReplenishProducts(List<Item> items) {
        return items.stream()
                .map(i -> stockpileService.frozen(i.getProductId(), i.getAmount()))
                .collect(Collectors.toList());
    }

}
