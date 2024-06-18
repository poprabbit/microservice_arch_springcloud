package com.github.fenixsoft.bookstore.warehouse.resource;

import com.github.fenixsoft.bookstore.domain.security.Role;
import com.github.fenixsoft.bookstore.domain.warehouse.DeliveredStatus;
import com.github.fenixsoft.bookstore.domain.warehouse.Product;
import com.github.fenixsoft.bookstore.domain.warehouse.Stockpile;
import com.github.fenixsoft.bookstore.dto.Item;
import com.github.fenixsoft.bookstore.infrastructure.jaxrs.CommonResponse;
import com.github.fenixsoft.bookstore.warehouse.application.StockpileApplicationService;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Component;

import javax.annotation.security.RolesAllowed;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

/**
 * 库存相关的资源
 *
 * @author icyfenix@gmail.com
 * @date 2020/4/19 21:40
 **/

@Path("/products")
@Component
@CacheConfig(cacheNames = "resource.product")
@Produces(MediaType.APPLICATION_JSON)
public class StockpileResource {

    @Inject
    private StockpileApplicationService service;

    /**
     * 将指定的产品库存调整为指定数额
     */
    @PATCH
    @Path("/stockpile/{productId}")
    @RolesAllowed(Role.ADMIN)
    @PreAuthorize("#oauth2.hasAnyScope('BROWSER')")
    public Response updateStockpile(@PathParam("productId") Integer productId, @QueryParam("amount") Integer amount) {
        return CommonResponse.op(() -> service.setStockpileAmountByProductId(productId, amount));
    }

    /**
     * 根据产品查询库存
     */
    @GET
    @Path("/stockpile/{productId}")
    @RolesAllowed(Role.ADMIN)
    @PreAuthorize("#oauth2.hasAnyScope('BROWSER','SERVICE')")
    public Stockpile queryStockpile(@PathParam("productId") Integer productId) {
        return service.getStockpile(productId);
    }

    // 以下是开放给内部微服务调用的方法

    /**
     * 根据订单id，商品id调整库存，保证幂等
     */
    @PATCH
    @Path("/stockpile/delivered/{productId}")
    @PreAuthorize("#oauth2.hasAnyScope('SERVICE')")
    public Response setDeliveredStatus(@PathParam("productId") Integer productId, @QueryParam("status") DeliveredStatus status, @QueryParam("amount") Integer amount, @QueryParam("payId") String payId) {
        return CommonResponse.op(() -> service.setDeliveredStatus(productId, status, amount, payId));
    }

    /**
     * 冻结库存并填充商品信息
     */
    @PATCH
    @Path("/stockpile/frozenBySettlement")
    @PreAuthorize("#oauth2.hasAnyScope('SERVICE')")
    public List<Product> frozenBySettlement(List<Item> items) {
        return service.frozenAndReplenishProducts(items);
    }
}
