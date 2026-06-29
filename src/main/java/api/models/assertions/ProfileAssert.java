package api.models.assertions;

import api.models.CustomerProfileResponse;
import org.assertj.core.api.AbstractAssert;

public class ProfileAssert extends AbstractAssert<ProfileAssert, CustomerProfileResponse> {

    protected ProfileAssert(CustomerProfileResponse actual) {
        super(actual, ProfileAssert.class);
    }

    public static ProfileAssert assertThatProfile(CustomerProfileResponse actual) {
        return new ProfileAssert(actual);
    }

    public ProfileAssert hasMessage(String expectedMessage) {
        isNotNull();
        if (!expectedMessage.equals(actual.getMessage())) {
            failWithMessage("Expected message to be <%s> but was <%s>",
                    expectedMessage, actual.getMessage());
        }
        return this;
    }

    // Составная проверка — аналог TransferAssert.isSuccessful()
    // Проверяет customer + сообщение об успехе одним вызовом
    public ProfileAssert isUpdatedSuccessfully(long expectedId, String expectedUsername, String expectedName) {
        isNotNull();
        CustomerAssert.assertThatCustomer(actual.getCustomer())
                .matches(expectedId, expectedUsername, expectedName);
        return hasMessage("Profile updated successfully");
    }
}