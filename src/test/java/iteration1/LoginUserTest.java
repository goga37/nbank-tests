package iteration1;

import generators.RandomData;
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

public class LoginUserTest extends BaseTest{

    @Test
    public void adminCanGenerateAuthTokenTest() {
        LoginUserRequest UserRequest = LoginUserRequest
                .builder()
                .username("admin")
                .password("admin")
                .build();

        new ValidatedCrudRequester<LoginUserResponse>(
                RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(UserRequest);

    }

    @Test
    public void userCanGenerateAuthTokenTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        new CrudRequester(
                RequestSpecs.unAuthSpec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.LOGIN)
                .post(LoginUserRequest.builder()
                        .username(userRequest.getUsername())
                        .password(userRequest.getPassword()).build())
                .assertThat()
                .header("Authorization", Matchers.notNullValue());

    }
}

