package iteration1;

import api.configs.Config;
import api.generators.RandomModelGenerator;
import api.models.CreateUserRequest;
import api.models.LoginUserRequest;
import api.models.LoginUserResponse;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import api.skelethon.Endpoint;
import api.skelethon.requests.CrudRequester;
import api.skelethon.requests.ValidatedCrudRequester;
import api.skelethon.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

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