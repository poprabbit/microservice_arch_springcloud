package com.github.fenixsoft.bookstore.resource;

import com.github.fenixsoft.bookstore.infrastructure.security.JWTAccessToken;
import org.glassfish.jersey.client.HttpUrlConnectorProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * 单元测试基类
 * <p>
 * 提供对JAX-RS资源的HTTP访问方法、登录授权、JSON字符串访问等支持
 *
 * @author icyfenix@gmail.com
 * @date 2020/4/6 19:32
 **/
@SpringBootTest(properties = "spring.cloud.config.enabled:false", webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class JAXRSResourceBase extends DBRollbackBase {

    @Value("${local.server.port}")
    protected int port;

    @Inject
    private JWTAccessToken jwtAccessToken;

    private String accessToken = null;

    protected Invocation.Builder build(String path) {
        Invocation.Builder builder = ClientBuilder.newClient().target("http://localhost:" + port + "/restful" + path)
                .property(HttpUrlConnectorProvider.SET_METHOD_WORKAROUND, true)
                .request(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON);
        if (accessToken != null) {
            builder.header("Authorization", "bearer " + accessToken);
        }
        return builder;
    }

    protected JSONObject json(Response response) throws JSONException {
        return new JSONObject(response.readEntity(String.class));
    }

    protected JSONArray jsonArray(Response response) throws JSONException {
        return new JSONArray(response.readEntity(String.class));
    }

    /**
     * 单元测试中登陆固定使用icyfenix这个用户
     */
    protected void login() {
        // 这是一个50年后才会过期的令牌，囧
        // HS256 JWT
        // accessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJpY3lmZW5peCIsInNjb3BlIjpbIkJST1dTRVIiXSwiZXhwIjozMTY0MjYyNDMwLCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImp0aSI6IjZmMWJhOGUwLTVkMGMtNGIxMC1iM2I3LTI0MDhmZTFlNzBhZSIsImNsaWVudF9pZCI6ImJvb2tzdG9yZV9mcm9udGVuZCIsInVzZXJuYW1lIjoiaWN5ZmVuaXgifQ.TmbYv6_OQlJg8ViEW5406UzNzNKjPSronrksjZ_epKM";
        // RS256 JWT
        accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiJpY3lmZW5peCIsInNjb3BlIjpbIkJST1dTRVIiXSwiZXhwIjozMTcxNzc5NTc1LCJhdXRob3JpdGllcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImp0aSI6IjZmMzM1ODRhLTE0OTgtNGJhYi05ZTcwLTBhN2Y5MDk5YzIwMyIsImNsaWVudF9pZCI6ImJvb2tzdG9yZV9mcm9udGVuZCIsInVzZXJuYW1lIjoiaWN5ZmVuaXgifQ.VeufGCgg_royfFeFVNJgV8zSQETt9PCGDQS_XCsv4yiWxKQvvWYVqx_714VoI4LBsdGSVDSdYfClvIOMHK7RS6qI1xiw7QHfhreFqYmLBYKvbhNJmmfZFw8lLeaQ68XDpFX1BjkIveyvURFCedCffqhU8DgGSxwHZVJ61mSnqt6OE8JDXKv18gP7rmsg4xDwkvyy-CL9kBKEsSx9iZ8Dm14O0EYPenyhXW7DESPOL63SEusjkjTdK5z_G-vBAeuMkAJZlh9a4DtWBnI5PMjfl_kBcgYePOrA1GypZqH7mgHXGybLEV5VpGaOZuJCNRWCjT5NgF4NhlgAEwMnctdMJg";
    }

    /**
     * 单元测试中微服务客户端令牌
     */
    private void loginService() {
        // 这是一个50年后才会过期的令牌，囧
        // RS256 JWT
        accessToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCIsImtpZCI6ImJvb2tzdG9yZS1qd3Qta2lkIn0.eyJzY29wZSI6WyJTRVJWSUNFIl0sImV4cCI6MzI5NTUwMzEzOSwianRpIjoiNGM5MTg1ZDMtZTliZi00OGQ3LWE2NzItZTQ5NDRjZTM1MDA3IiwiY2xpZW50X2lkIjoid2FyZWhvdXNlIn0.faqBWX7HSTOYce-EcLIC6WZxc7ghlVdC3McyyOszjSST1GDxs-_W7lmm6dkAFM5jM1TtFMtu37YLc62ZCbp9O9nihBrJJn4Ci7xDCVUP8ii2VxXLotlt9iVJ7E3kCRvdHhGiu-Y2vd2UkxIkH9n7vHQgjhyT7gcjfTX77JNDr-XZa2gDeSZxL7cQB8xNE2ZvwMJhbZ1fi5lJooX1bVaIapcvF9W0UMd3cYfn2TYShRwAJ-0kDx5Zfc82QUz4IzwugWK4VHjQh4YiW6BpLmbMDYK7xI3th5AAD0bxl1Fd3d6EMG6ctfhsJZf_bcrleKXgs2OMMQFwVt6xGJUUAyMaMg";
    }

    protected void logout() {
        accessToken = null;
    }

    protected void authenticatedScope(Runnable runnable) {
        try {
            login();
            runnable.run();
        } finally {
            logout();
        }
    }

    protected void authenticatedService(Runnable runnable) {
        try {
            loginService();
            runnable.run();
        } finally {
            logout();
        }
    }

    protected <T> T authenticatedGetter(Supplier<T> supplier) {
        try {
            login();
            return supplier.get();
        } finally {
            logout();
        }
    }

    protected Response get(String path) {
        return build(path).get();
    }

    protected Response delete(String path) {
        return build(path).delete();
    }

    protected Response post(String path, Object entity) {
        return build(path).post(Entity.json(entity));
    }

    protected Response put(String path, Object entity) {
        return build(path).put(Entity.json(entity));
    }

    protected Response patch(String path) {
        return build(path).method("PATCH", Entity.text("MUST_BE_PRESENT"));
    }

    protected Response patch(String path, Object entity) {
        return build(path).method("PATCH", Entity.json(entity));
    }

    protected static void assertOK(Response response) {
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(), "期望HTTP Status Code应为：200/OK");
    }

    protected static void assertNoContent(Response response) {
        assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(), "期望HTTP Status Code应为：204/NO_CONTENT");
    }

    protected static void assertBadRequest(Response response) {
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(), "期望HTTP Status Code应为：400/BAD_REQUEST");
    }

    protected static void assertForbidden(Response response) {
        assertEquals(Response.Status.FORBIDDEN.getStatusCode(), response.getStatus(), "期望HTTP Status Code应为：403/FORBIDDEN");
    }

    protected static void assertServerError(Response response) {
        assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus(), "期望HTTP Status Code应为：500/INTERNAL_SERVER_ERROR");
    }

    protected static void assertNotFound(Response response) {
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus(), "期望HTTP Status Code应为：404/NOT_FOUND");
    }


}
