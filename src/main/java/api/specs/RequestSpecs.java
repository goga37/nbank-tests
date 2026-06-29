package api.specs;

import api.configs.Config;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.filter.log.RequestLoggingFilter;
import io.restassured.filter.log.ResponseLoggingFilter;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import api.models.LoginUserRequest;
import api.skelethon.Endpoint;
import api.skelethon.requests.CrudRequester;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestSpecs {
    private static Map<String, String> authHeaders = new HashMap<>(Map.of("admin", "Basic YWRtaW46YWRtaW4="));

    private RequestSpecs() {
    }

    private static RequestSpecBuilder defaulytRequestBuilder() {
        return new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .addFilters(List.of(new RequestLoggingFilter(), new ResponseLoggingFilter()))
                .setBaseUri(Config.getProperty("server") + Config.getProperty("apiVersion"));
    }

    public static RequestSpecification unAuthSpec() {
        return defaulytRequestBuilder().build();
    }

    public static RequestSpecification invalidTokenSpec() {
        return defaulytRequestBuilder()
                .addHeader("Authorization", "Bearer invalid.token.garbage")
                .build();
    }

    public static RequestSpecification adminSpec() {
        return defaulytRequestBuilder()
                .addHeader("Authorization", "Basic YWRtaW46YWRtaW4=")
                .build();
    }

    public static RequestSpecification authAsUser(String username, String password) {
        return defaulytRequestBuilder()
                .addHeader("Authorization", getUserAuthHeader(username, password))
                .build();
    }

    public static String getUserAuthHeader(String username, String password) {
        if (!authHeaders.containsKey(username)) {
            String userAuthHeader = new CrudRequester(
                    RequestSpecs.unAuthSpec(),
                    ResponseSpecs.requestReturnsOK(),
                    Endpoint.LOGIN)
                    .post(LoginUserRequest.builder()
                            .username(username)
                            .password(password)
                            .build())
                    .extract().header("Authorization");
            authHeaders.put(username, userAuthHeader);
        }
        return authHeaders.get(username);
    }
}