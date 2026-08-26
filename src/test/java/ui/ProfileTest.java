package ui;

import api.generators.RandomModelGenerator;
import api.models.Customer;
import api.models.CustomerProfileRequest;
import api.specs.ApiError;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.ProfilePage;
import ui.pages.UserDashboard;

import java.util.stream.Stream;

import static api.generators.RandomData.randomNameWithDigits;
import static api.generators.RandomData.randomNameWithoutSpace;
import static api.generators.RandomData.randomSingleWordName;
import static api.models.assertions.CustomerAssert.assertThatCustomer;

public class ProfileTest extends BaseUiTest {
    @Test
    @UserSession
    public void userChangesProfileNameTest() {
        String newName = RandomModelGenerator.generate(CustomerProfileRequest.class).getName();

        new ProfilePage().open().changeName(newName)
                .checkAlertMessageAndAccept(BankAlert.PROFILE_UPDATE_SUCCESS.getMessage());

        new UserDashboard().open().checkWelcomeText(newName);

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasName(newName);
    }

    private static Stream<Arguments> invalidProfileNames() {
        return Stream.of(
                Arguments.of("", BankAlert.PROFILE_INVALID_NAME.getMessage()),
                Arguments.of(randomSingleWordName(), ApiError.INVALID_PROFILE_NAME.getMessage()),
                Arguments.of(randomNameWithDigits(), ApiError.INVALID_PROFILE_NAME.getMessage()),
                Arguments.of(randomNameWithoutSpace(), ApiError.INVALID_PROFILE_NAME.getMessage())
        );
    }

    @ParameterizedTest
    @MethodSource("invalidProfileNames")
    @UserSession
    public void userCannotChangeNameToInvalidValueTest(String newName, String expectedMessage) {
        new ProfilePage().open().changeName(newName)
                .checkAlertMessageAndAccept(expectedMessage);

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasNullName();
    }
}
