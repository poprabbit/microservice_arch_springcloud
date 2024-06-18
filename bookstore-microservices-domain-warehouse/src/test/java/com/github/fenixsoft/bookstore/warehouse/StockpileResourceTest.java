package com.github.fenixsoft.bookstore.warehouse;

import com.github.fenixsoft.bookstore.domain.warehouse.Product;
import com.github.fenixsoft.bookstore.domain.warehouse.Stockpile;
import com.github.fenixsoft.bookstore.dto.Item;
import com.github.fenixsoft.bookstore.resource.JAXRSResourceBase;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.GenericType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author icyfenix@gmail.com
 * @date 2020/4/21 17:40
 **/
class StockpileResourceTest extends JAXRSResourceBase {

    @Test
    void updateAndQueryStockpile() {
        authenticatedScope(() -> {
            assertOK(patch("/products/stockpile/1?amount=20"));
            Stockpile stockpile = get("/products/stockpile/1").readEntity(Stockpile.class);
            assertEquals(20, stockpile.getAmount());
        });
    }

    @Test
    void frozenStockpile() {
        authenticatedService(() -> {
            // 异常用例
            List<Item> items = Collections.singletonList(new Item(99, 99));
            assertServerError(patch("/products/stockpile/frozenBySettlement", items));
            // 正常用例
            items = Arrays.asList(new Item(1, 1), new Item(2, 2));
            List<Product> products = patch("/products/stockpile/frozenBySettlement", items).readEntity(new GenericType<List<Product>>() {
            });
            assertEquals(129d, products.get(0).getPrice(), "期望商品1的标题是：129");
            assertEquals(69, products.get(1).getPrice(), "期望商品2的标题是：69");
            Stockpile stockpile = get("/products/stockpile/2").readEntity(Stockpile.class);
            assertEquals(28, stockpile.getAmount(), "期望商品2的库存是：28");
        });
    }

    @Test
    void delivered() {
        authenticatedService(() -> {
            List<Item> items = Collections.singletonList(new Item(10, 3));
            assertOK(patch("/products/stockpile/frozenBySettlement", items));
            String payId = UUID.randomUUID().toString();
            // 支付完成
            assertOK(patch("/products/stockpile/delivered/3?payId=" + payId + "&status=DECREASE&amount=10"));
            Stockpile stockpile = get("/products/stockpile/3").readEntity(Stockpile.class);
            assertEquals(20, stockpile.getAmount());
            assertEquals(0, stockpile.getFrozen());
            // 模拟重复扣减库存
            assertOK(patch("/products/stockpile/delivered/3?payId=" + payId + "&status=DECREASE&amount=10"));
            stockpile = get("/products/stockpile/3").readEntity(Stockpile.class);
            assertEquals(0, stockpile.getFrozen());
        });
    }
}
