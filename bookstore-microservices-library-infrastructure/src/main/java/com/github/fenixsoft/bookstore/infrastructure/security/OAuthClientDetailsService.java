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

package com.github.fenixsoft.bookstore.infrastructure.security;

import com.github.fenixsoft.bookstore.domain.security.GrantType;
import com.github.fenixsoft.bookstore.domain.security.Scope;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.builders.InMemoryClientDetailsServiceBuilder;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Named;
import java.util.Arrays;
import java.util.List;

/**
 * OAuth2客户端类型定义
 * <p>
 * OAuth2支持四种授权模式，这里前端API使用密码授权模式，微服务使用客户端授权模式。
 *
 * @author icyfenix@gmail.com
 * @date 2020/3/7 19:45
 **/
@Named
public class OAuthClientDetailsService implements ClientDetailsService {

    /**
     * 客户端模型
     */
    private static class Client {
        /**
         * 客户端ID
         */
        String clientId;

        /**
         * 客户端密钥
         * 在OAuth2协议中，ID是可以公开的，密钥应当保密，密钥用以证明当前申请授权的客户端是未被冒充的
         */
        String clientSecret;

        /**
         * 授权类型
         * 前端API使用密码授权模式，微服务使用客户端授权模式
         */
        String[] grantTypes;

        /**
         * 授权范围
         */
        String[] scopes;

        Client(String clientId, String clientSecret, String[] grantTypes, String[] scopes) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.grantTypes = grantTypes;
            this.scopes = scopes;
        }
    }


    /**
     * 客户端列表
     * <p>
     * 此场景中微服务一种有JavaScript前端、Account微服务、Warehouse微服务、Payment微服务四种客户端
     * todo 如果正式使用，这部分信息应该做成可以配置的，以便快速增加微服务的类型。clientSecret也不应该出现在源码中，应由外部配置传入
     */
    private static final List<Client> clients = Arrays.asList(
            new Client("bookstore_frontend", "bookstore_secret", new String[]{GrantType.PASSWORD, GrantType.REFRESH_TOKEN}, new String[]{Scope.BROWSER}),
            new Client("account", "account_secret", new String[]{GrantType.CLIENT_CREDENTIALS}, new String[]{Scope.SERVICE}),
            new Client("warehouse", "warehouse_secret", new String[]{GrantType.CLIENT_CREDENTIALS}, new String[]{Scope.SERVICE}),
            new Client("payment", "payment_secret", new String[]{GrantType.CLIENT_CREDENTIALS}, new String[]{Scope.SERVICE}),
            new Client("security", "security_secret", new String[]{GrantType.CLIENT_CREDENTIALS}, new String[]{Scope.SERVICE})
    );


    @Inject
    private PasswordEncoder passwordEncoder;

    private ClientDetailsService clientDetailsService;

    /**
     * 构造OAuth2客户端服务
     *
     * <p>
     * 授权Endpoint示例：
     * /oauth/token?grant_type=password & username=#USER# & password=#PWD# & client_id=bookstore_frontend & client_secret=bookstore_secret
     * 刷新令牌Endpoint示例：
     * /oauth/token?grant_type=refresh_token & refresh_token=#REFRESH_TOKEN# & client_id=bookstore_frontend & client_secret=bookstore_secret
     */
    @PostConstruct
    public void init() throws Exception {
        InMemoryClientDetailsServiceBuilder builder = new InMemoryClientDetailsServiceBuilder();
        clients.forEach(client -> {
            // 提供客户端ID和密钥，并指定该客户端支持的授权类型
            builder.withClient(client.clientId)
                    .secret(passwordEncoder.encode(client.clientSecret))
                    .scopes(client.scopes)
                    .authorizedGrantTypes(client.grantTypes);
        });
        clientDetailsService = builder.build();
    }

    /**
     * 外部根据客户端ID查询验证方式
     */
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {
        return clientDetailsService.loadClientByClientId(clientId);
    }
}
