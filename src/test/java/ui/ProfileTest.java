package ui;

import api.generators.RandomModelGenerator;
import api.models.Customer;
import api.models.CustomerProfileRequest;
import api.specs.ApiError;
import com.codeborne.selenide.Condition;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.ProfilePage;
import ui.pages.UserDashboard;

import static api.models.assertions.CustomerAssert.assertThatCustomer;

public class ProfileTest extends BaseUiTest {
    @Test
    @UserSession
    public void userChangesProfileNameTest() {
        String newName = RandomModelGenerator.generate(CustomerProfileRequest.class).getName();

        new ProfilePage().open().changeName(newName)
                .checkAlertMessageAndAccept(BankAlert.PROFILE_UPDATE_SUCCESS.getMessage());

        new UserDashboard().open().getWelcomeText()
                .shouldHave(Condition.exactText("Welcome, " + newName + "!"));

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasName(newName);
    }

    // Пустое поле не долетает до API — фронт проверяет name.trim() на клиенте
    // и показывает свой alert, не сервера (см. main.js: if(r.trim()) ... else alert(...))
    @Test
    @UserSession
    public void userCannotChangeNameWithEmptyValueTest() {
        new ProfilePage().open().changeName("")
                .checkAlertMessageAndAccept(BankAlert.PROFILE_INVALID_NAME.getMessage());

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasNullName();
    }

    // Одно слово, цифры, слитное написание — фронт гоняет клиентскую regex-валидацию
    // параллельно с запросом к серверу, поэтому какая из двух сработает раньше и
    // покажет свой alert — не детерминировано (подтверждено повторными прогонами).
    // Принимаем оба легитимных варианта, а не гонимся за конкретным.
    @Test
    @UserSession
    public void userCannotChangeNameToSingleWordTest() {
        new ProfilePage().open().changeName("John")
                .checkAlertMessageAndAccept(BankAlert.PROFILE_INVALID_NAME.getMessage(), ApiError.INVALID_PROFILE_NAME.getMessage());

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasNullName();
    }

    @Test
    @UserSession
    public void userCannotChangeNameWithDigitsTest() {
        new ProfilePage().open().changeName("John123 Smith")
                .checkAlertMessageAndAccept(BankAlert.PROFILE_INVALID_NAME.getMessage(), ApiError.INVALID_PROFILE_NAME.getMessage());

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasNullName();
    }

    @Test
    @UserSession
    public void userCannotChangeNameWithoutSpaceTest() {
        new ProfilePage().open().changeName("JohnSmith")
                .checkAlertMessageAndAccept(BankAlert.PROFILE_INVALID_NAME.getMessage(), ApiError.INVALID_PROFILE_NAME.getMessage());

        Customer profile = SessionStorage.getSteps().getProfile();
        assertThatCustomer(profile).hasNullName();
    }
}
