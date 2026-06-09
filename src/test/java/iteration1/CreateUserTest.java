package iteration1;

import generators.RandomData;
import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import models.UserRole;
import models.comparison.ModelAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import java.util.stream.Stream;

public class CreateUserTest extends BaseTest {
    @Test
    public void adminCanCreateUserWithCorrectData() {
        CreateUserRequest createUserRequest = RandomModelGenerator.generate(CreateUserRequest.class);

        CreateUserResponse createUserResponse = new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER)
                .post(createUserRequest);

        ModelAssertions.assertThatModels(createUserRequest, createUserResponse).match();
    }

    public static Stream<Arguments> userInvalidData() {
        return Stream.of(
                Arguments.of("", "K!ate2000!!", "USER", "username", "Username cannot be blank"),
                Arguments.of("ab", "K!ate2000!!", "USER", "username", "Username must be between 3 and 15 characters"),
                Arguments.of("abd!", "K!ate2000!!", "USER", "username", "Username must contain only letters, digits, dashes, underscores, and dots")
        );
    }

    @MethodSource("userInvalidData")
    @ParameterizedTest
    public void adminCanCreateUserWithInavalidData(String username, String password, String role, String errorKey, String errorValue) {
        CreateUserRequest createUserRequest = CreateUserRequest
                .builder()
                .username(username)
                .password(password)
                .role(role)
                .build();

        new CrudRequester(
                RequestSpecs.adminSpec(),
                ResponseSpecs.requestReturnsBadRequest(errorKey, errorValue),
                Endpoint.ADMIN_USER)
                .post(createUserRequest);
    }
}