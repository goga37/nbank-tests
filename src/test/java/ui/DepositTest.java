package ui;

import api.models.AccountResponse;
import api.skelethon.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.BankAlert;
import ui.pages.DepositPage;

import java.util.List;
import java.util.stream.Stream;

import static api.generators.RandomData.randomDeposit;
import static api.models.assertions.AccountAssert.assertThatAccount;

public class DepositTest extends BaseUiTest {
    @Test
    @UserSession
    public void userAddDepositTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber = accounts.getFirst().getAccountNumber();

        new DepositPage().open().addDeposit(accountNumber, amountAsText)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESS.withAmountToAccount(amountAsText, accountNumber));

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(amount)
                .hasTransactionCount(1);
    }

    private static Stream<Arguments> invalidDepositAmounts() {
        return Stream.of(
                Arguments.of(String.valueOf(-randomDeposit()), BankAlert.DEPOSIT_INVALID_AMOUNT),
                Arguments.of("5000.01", BankAlert.DEPOSIT_AMOUNT_EXCEEDS_MAX)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidDepositAmounts")
    @UserSession
    public void userCannotDepositWithInvalidAmountTest(String amount, BankAlert expectedAlert) {
        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber = accounts.getFirst().getAccountNumber();
        double balance = accounts.getFirst().getBalance();

        new DepositPage().open().addDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(expectedAlert.getMessage());

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(balance)
                .hasNoTransactions();
    }

    @Test
    @UserSession
    public void userCannotDepositWithoutSelectingAccountTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        double balance = accounts.getFirst().getBalance();


        new DepositPage().open().enterAmount(amountAsText).clickDeposit()
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SELECT_ACCOUNT_REQUIRED.getMessage());

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(balance)
                .hasNoTransactions();
    }
}
