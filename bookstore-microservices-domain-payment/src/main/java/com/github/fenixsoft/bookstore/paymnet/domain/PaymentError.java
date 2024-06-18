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

import com.github.fenixsoft.bookstore.domain.BaseEntity;

import javax.persistence.Entity;

/**
 * 支付单三方支付模型
 * <p>
 * 就是传到客户端让用户给扫码或者其他别的方式付钱的对象
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/12 17:07
 **/
@Entity
public class PaymentError extends BaseEntity {


    private String payId;
    private Integer productId;
    private Integer amount;


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

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
