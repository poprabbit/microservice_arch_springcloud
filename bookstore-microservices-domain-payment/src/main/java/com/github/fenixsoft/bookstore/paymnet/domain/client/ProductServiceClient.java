package com.github.fenixsoft.bookstore.paymnet.domain.client;

import com.github.fenixsoft.bookstore.domain.warehouse.DeliveredStatus;
import com.github.fenixsoft.bookstore.domain.warehouse.Product;
import com.github.fenixsoft.bookstore.domain.warehouse.Stockpile;
import com.github.fenixsoft.bookstore.dto.Item;
import com.github.fenixsoft.bookstore.dto.Settlement;
import com.github.fenixsoft.bookstore.infrastructure.jaxrs.CodedMessage;
import org.springframework.cloud.openfeign.FeignClient;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 仓库商品和库存相关远程服务
 *
 * @author icyfenix@gmail.com
 * @date 2020/4/19 22:22
 **/
@FeignClient(name = "warehouse")
public interface ProductServiceClient {

    /**
     * 是否需要判断执行结果？
     */
    default void frozenAndReplenishProducts(Settlement bill) {
        bill.productMap = frozenBySettlement(bill.getItems()).stream().collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    @PATCH
    @Path("/restful/products/stockpile/frozenBySettlement")
    @Consumes(MediaType.APPLICATION_JSON)
    List<Product> frozenBySettlement(@QueryParam("items") Collection<Item> items);

    @GET
    @Path("/restful/products/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    Product getProduct(@PathParam("id") Integer id);

    @GET
    @Path("/restful/products")
    @Consumes(MediaType.APPLICATION_JSON)
    Product[] getProducts();

    default CodedMessage decrease(Integer productId, Integer amount, String payId) {
        return setDeliveredStatus(productId, DeliveredStatus.DECREASE, amount, payId);
    }

    default CodedMessage thawed(Integer productId, Integer amount, String payId) {
        return setDeliveredStatus(productId, DeliveredStatus.THAWED, amount, payId);
    }

    @PATCH
    @Path("/restful/products/stockpile/delivered/{productId}")
    @Consumes(MediaType.APPLICATION_JSON)
    CodedMessage setDeliveredStatus(@PathParam("productId") Integer productId, @QueryParam("status") DeliveredStatus status, @QueryParam("amount") Integer amount, @QueryParam("payId") String payId);

    @GET
    @Path("/restful/products/stockpile/{productId}")
    @Consumes(MediaType.APPLICATION_JSON)
    Stockpile queryStockpile(@PathParam("productId") Integer productId);

}
