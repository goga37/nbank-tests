package iteration1;

import configs.Config;
import generators.RandomModelGenerator;
import models.*;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import requests.skelethon.steps.AdminSteps;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class LoginUserTest extends BaseTest {

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest loginRequest = LoginUserRequest.builder()
                .username(Config.getProperty("admin.username"))
                .password(Config.getProperty("admin.password"))
                .build();

        new ValidatedCrudRequester<LoginUserResponse>(
                RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(loginRequest);
    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        LoginUserRequest loginRequest = RandomModelGenerator.generate(LoginUserRequest.class);

        AdminSteps.createUser(CreateUserRequest.builder()
                .username(loginRequest.getUsername())
                .password(loginRequest.getPassword())
                .role("USER")
                .build());

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(loginRequest)
                .assertThat()
                .header("Authorization", Matchers.notNullValue());
    }
}