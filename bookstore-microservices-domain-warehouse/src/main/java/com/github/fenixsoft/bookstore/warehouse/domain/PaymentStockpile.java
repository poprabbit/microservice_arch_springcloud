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

package com.github.fenixsoft.bookstore.warehouse.domain;

import com.github.fenixsoft.bookstore.domain.BaseEntity;

import javax.persistence.Entity;

/**
 * 订单库存操作模型
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/12 16:34
 **/
@Entity
public class PaymentStockpile extends BaseEntity {
    private String payId;
    private Integer productId;
    private String stockOp;

    public PaymentStockpile() {
    }

    public PaymentStockpile(String payId, Integer productId, String stockOp) {
        this.payId = payId;
        this.productId = productId;
        this.stockOp = stockOp;
    }

    public String getStockOp() {
        return stockOp;
    }

    public void setStockOp(String stockOp) {
        this.stockOp = stockOp;
    }

    public String getPayId() {
        return payId;
    }

    public void setPayId(String payId) {
        this.payId = payId;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }
}
