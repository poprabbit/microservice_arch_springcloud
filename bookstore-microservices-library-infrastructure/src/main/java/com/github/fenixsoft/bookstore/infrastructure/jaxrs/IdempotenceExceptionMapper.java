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

package com.github.fenixsoft.bookstore.infrastructure.jaxrs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * 用于处理幂等方法产生的异常，前端收到的是正常结果
 *
 * @author icyfenix@gmail.c
 * @date 2020/3/12 16:43
 **/
@Provider
public class IdempotenceExceptionMapper implements ExceptionMapper<IdempotenceException> {

    private static final Logger log = LoggerFactory.getLogger(IdempotenceExceptionMapper.class);

    @Override
    public Response toResponse(IdempotenceException exception) {
        log.error(exception.getMessage(), exception);
        return CommonResponse.success(exception.getMessage());
    }
}
