package com.github.fenixsoft.bookstore.paymnet.mock;

import com.github.fenixsoft.bookstore.domain.warehouse.DeliveredStatus;
import com.github.fenixsoft.bookstore.domain.warehouse.Product;
import com.github.fenixsoft.bookstore.domain.warehouse.Stockpile;
import com.github.fenixsoft.bookstore.dto.Item;
import com.github.fenixsoft.bookstore.infrastructure.jaxrs.CodedMessage;
import com.github.fenixsoft.bookstore.paymnet.domain.client.ProductServiceClient;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * @author icyfenix@gmail.com
 * @date 2020/4/21 17:07
 **/
@Named
public class ProductServiceClientMock implements ProductServiceClient {

    @Override
    public List<Product> frozenBySettlement(Collection<Item> items) {
        return null;
    }

    @Override
    public Product getProduct(Integer id) {
        if (id == 1) {
            Product product = new Product();
            product.setId(1);
            product.setTitle("深入理解Java虚拟机（第3版）");
            product.setPrice(129d);
            product.setRate(9.6f);
            return product;
        } else {
            return null;
        }
    }

    @Override
    public Product[] getProducts() {
        return new Product[]{getProduct(1)};
    }

    @Override
    public CodedMessage setDeliveredStatus(Integer productId, DeliveredStatus status, Integer amount, String payId) {
        return null;
    }

    @Override
    public Stockpile queryStockpile(Integer productId) {
        if (productId == 1) {
            Stockpile stock = new Stockpile();
            stock.setId(1);
            stock.setAmount(30);
            stock.setProduct(getProduct(1));
            return stock;
        } else {
            return null;
        }
    }

}
