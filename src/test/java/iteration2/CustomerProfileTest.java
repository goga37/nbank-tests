package iteration2;

import api.generators.RandomModelGenerator;
import iteration1.BaseTest;
import api.models.Customer;
import api.models.CustomerProfileRequest;
import api.models.CustomerProfileResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import api.skelethon.steps.AccountSteps;
import api.specs.ApiError;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import static api.models.assertions.CustomerAssert.assertThatCustomer;
import static api.models.assertions.ProfileAssert.assertThatProfile;
import static api.skelethon.steps.AccountSteps.createUserWithAccount;
import static api.skelethon.steps.ProfileSteps.getProfile;
import static api.skelethon.steps.ProfileSteps.updateProfile;

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