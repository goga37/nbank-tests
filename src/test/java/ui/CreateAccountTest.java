package ui;

import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.skelethon.steps.UserSteps;
import org.junit.jupiter.api.Test;
import api.skelethon.steps.AdminSteps;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class CreateAccountTest extends BaseUiTest {
    @Test
    public void userCanCreateAccountTest() {
        // ШАГИ ПО НАСТРОЙКЕ ОКРУЖЕНИЯ
        // ШАГ 1: админ логинится в банке
        // ШАГ 2: админ создает юзера
        // ШАГ 3: юзер логинится в банке

        CreateUserRequest user = AdminSteps.createUser();
        authAsUser(user);

        // ШАГ 4: юзер создает аккаунт
        new UserDashboard().open().createNewAccount();

        List<AccountResponse> createdAccounts = new UserSteps(user.getUsername(), user.getPassword())
                .getAllAccounts();
        assertThat(createdAccounts).hasSize(1);

        new UserDashboard().checkAlertMessageAndAccept
                (BankAlert.NEW_ACCOUNT_CREATED.getMessage() + createdAccounts.getFirst().getAccountNumber());

        assertThat(createdAccounts.getFirst().getBalance()).isZero();
    }
}
