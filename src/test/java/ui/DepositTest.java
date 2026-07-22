package ui;

import api.models.AccountResponse;
import api.skelethon.steps.UserSteps;
import common.annotations.UserSession;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.DepositPage;

import java.util.List;

import static api.generators.RandomData.randomDeposit;
import static api.models.assertions.AccountAssert.assertThatAccount;
import static org.assertj.core.api.Assertions.assertThat;

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
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_SUCCESS.getMessage() + amountAsText + " to account " + accountNumber);

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(amount)
                .hasTransactionCount(1);
    }

    @Test
    @UserSession
    public void userCannotDepositNegativeAmountTest() {
        String amount = "-1";

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber = accounts.getFirst().getAccountNumber();
        double balance = accounts.getFirst().getBalance();

        new DepositPage().open().addDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_INVALID_AMOUNT.getMessage());

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(balance)
                .hasNoTransactions();
    }

    @Test
    @UserSession
    public void userCannotDepositZeroAmountTest() {
        String amount = "0";

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber = accounts.getFirst().getAccountNumber();
        double balance = accounts.getFirst().getBalance();

        new DepositPage().open().addDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_INVALID_AMOUNT.getMessage());

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(balance)
                .hasNoTransactions();
    }

    @Test
    @UserSession
    public void userCannotDepositAmountAboveMaxTest() {
        String amount = "5000.01";

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber = accounts.getFirst().getAccountNumber();
        double balance = accounts.getFirst().getBalance();

        new DepositPage().open().addDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_AMOUNT_EXCEEDS_MAX.getMessage());

        AccountResponse updatedAccount = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(updatedAccount)
                .hasBalance(balance)
                .hasNoTransactions();
    }

    @Test
    @UserSession
    public void userCannotDepositWithEmptyAmountTest() {
        String amount = "";

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber = accounts.getFirst().getAccountNumber();
        double balance = accounts.getFirst().getBalance();

        new DepositPage().open().addDeposit(accountNumber, amount)
                .checkAlertMessageAndAccept(BankAlert.DEPOSIT_INVALID_AMOUNT.getMessage());

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
