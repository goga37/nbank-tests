package iteration2;

import generators.RandomModelGenerator;
import iteration1.BaseTest;
import models.Customer;
import models.CustomerProfileRequest;
import models.CustomerProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skelethon.steps.AccountSteps;
import specs.ApiError;
import specs.RequestSpecs;
import specs.ResponseSpecs;

import static models.assertions.CustomerAssert.assertThatCustomer;
import static models.assertions.ProfileAssert.assertThatProfile;
import static requests.skelethon.steps.AccountSteps.createUserWithAccount;
import static requests.skelethon.steps.ProfileSteps.getProfile;
import static requests.skelethon.steps.ProfileSteps.updateProfile;

public class CustomerProfileTest extends BaseTest {

    @Test
    public void updateProfileNameSuccess() {
        AccountSteps.UserContext user = createUserWithAccount();
        CustomerProfileRequest profileRequest = RandomModelGenerator.generate(CustomerProfileRequest.class);

        CustomerProfileResponse response = updateProfile(user, profileRequest);

        assertThatProfile(response)
                .isUpdatedSuccessfully(user.userId(), user.username(), profileRequest.getName());

        Customer profile = getProfile(user);
        assertThatCustomer(profile)
                .matches(user.userId(), user.username(), profileRequest.getName());
    }

    @Test
    public void updateProfileUnauthorizedReturns401() {
        updateProfile(
                RequestSpecs.unAuthSpec(),
                RandomModelGenerator.generate(CustomerProfileRequest.class),
                ResponseSpecs.responseStatus401()
        );
    }

    @Test
    public void updateProfileInvalidTokenReturns401() {
        updateProfile(
                RequestSpecs.invalidTokenSpec(),
                RandomModelGenerator.generate(CustomerProfileRequest.class),
                ResponseSpecs.responseStatus401()
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"John", "John123 Smith", "John@ Smith", ""})
    public void updateProfileInvalidNameReturns400(String name) {
        AccountSteps.UserContext user = createUserWithAccount();

        updateProfile(
                user.spec(),
                CustomerProfileRequest.builder().name(name).build(),
                ResponseSpecs.responseStatus400(ApiError.INVALID_PROFILE_NAME)
        );

        Customer profile = getProfile(user);
        assertThatCustomer(profile).hasNullName();
    }
}