package ui;

import api.models.AccountResponse;
import api.models.Transaction;
import api.models.TransactionType;
import api.skelethon.steps.UserSteps;
import common.annotations.UserSession;
import api.specs.ApiError;
import common.annotations.APIVersion;
import common.storage.SessionStorage;
import org.junit.jupiter.api.Test;
import ui.pages.BankAlert;
import ui.pages.TransferPage;

import java.util.List;

import static api.generators.RandomData.randomDeposit;
import static api.models.assertions.AccountAssert.assertThatAccount;

public class TransferTest extends BaseUiTest {
    @Test
    @UserSession
    public void userTransferBetweenOwnAccountsTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);

        UserSteps.createAccount(SessionStorage.getUser());
        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        AccountResponse depositedAccount1 = SessionStorage.getSteps().deposit(accounts.getFirst().getId(), amount);

        String accountNumber1 = accounts.getFirst().getAccountNumber();
        String accountNumber2 = accounts.get(1).getAccountNumber();
        double balance1 = depositedAccount1.getBalance();
        double balance2 = accounts.get(1).getBalance();

        String recipientName = SessionStorage.getSteps().getProfile().getName();

        new TransferPage().open().transfer(accountNumber1, recipientName, accountNumber2, amountAsText)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESS.getMessage() + amountAsText + " to account " + accountNumber2);

        AccountResponse updatedAccount1 = SessionStorage.getSteps().getAllAccounts().getFirst();
        AccountResponse updatedAccount2 = SessionStorage.getSteps().getAllAccounts().get(1);
        double expectedBalance1 = balance1 - amount;
        double expectedBalance2 = balance2 + amount;

        assertThatAccount(updatedAccount1)
                .hasBalance(expectedBalance1)
                .hasTransactionCount(2)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_OUT)
                        .amount(amount)
                        .relatedAccountId(accounts.get(1).getId())
                        .build());

        assertThatAccount(updatedAccount2)
                .hasBalance(expectedBalance2)
                .hasTransactionCount(1)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_IN)
                        .amount(amount)
                        .relatedAccountId(accounts.getFirst().getId())
                        .build());
    }

    @Test
    @UserSession
    public void userCannotTransferWithInsufficientFundsTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);

        UserSteps.createAccount(SessionStorage.getUser());
        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber1 = accounts.getFirst().getAccountNumber();
        String accountNumber2 = accounts.get(1).getAccountNumber();

        String recipientName = SessionStorage.getSteps().getProfile().getName();

        new TransferPage().open().transfer(accountNumber1, recipientName, accountNumber2, amountAsText)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_ERROR_PREFIX.getMessage() + ApiError.TRANSFER_INSUFFICIENT_FUNDS.getMessage());

        AccountResponse account1 = SessionStorage.getSteps().getAllAccounts().getFirst();
        AccountResponse account2 = SessionStorage.getSteps().getAllAccounts().get(1);

        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    @Test
    @UserSession
    @APIVersion("with_validation_fix")
    public void userCannotTransferZeroAmountTest() {
        String amount = "0";

        UserSteps.createAccount(SessionStorage.getUser());
        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        AccountResponse depositedAccount1 = SessionStorage.getSteps().deposit(accounts.getFirst().getId(), 100.0);

        String accountNumber1 = accounts.getFirst().getAccountNumber();
        String accountNumber2 = accounts.get(1).getAccountNumber();

        String recipientName = SessionStorage.getSteps().getProfile().getName();

        new TransferPage().open().transfer(accountNumber1, recipientName, accountNumber2, amount)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_ERROR_PREFIX.getMessage() + ApiError.TRANSFER_AMOUNT_TOO_SMALL.getMessage());

        AccountResponse account1 = SessionStorage.getSteps().getAllAccounts().getFirst();
        AccountResponse account2 = SessionStorage.getSteps().getAllAccounts().get(1);

        assertThatAccount(account1).hasBalance(depositedAccount1.getBalance()).hasTransactionCount(1);
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    @Test
    @UserSession
    public void userCannotTransferToNonExistentAccountTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);
        String nonExistentAccountNumber = "ACC999999";

        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        AccountResponse depositedAccount1 = SessionStorage.getSteps().deposit(accounts.getFirst().getId(), amount);

        String accountNumber1 = accounts.getFirst().getAccountNumber();
        String recipientName = SessionStorage.getSteps().getProfile().getName();

        new TransferPage().open().transfer(accountNumber1, recipientName, nonExistentAccountNumber, amountAsText)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_RECIPIENT_NOT_FOUND.getMessage());

        AccountResponse account1 = SessionStorage.getSteps().getAllAccounts().getFirst();

        assertThatAccount(account1).hasBalance(depositedAccount1.getBalance()).hasTransactionCount(1);
    }

    @Test
    @UserSession
    public void userCannotTransferWithoutSelectingAccountTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);

        UserSteps.createAccount(SessionStorage.getUser());
        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber2 = accounts.get(1).getAccountNumber();
        String recipientName = SessionStorage.getSteps().getProfile().getName();

        new TransferPage().open()
                .enterRecipientName(recipientName)
                .enterRecipientAccount(accountNumber2)
                .enterAmount(amountAsText)
                .confirm()
                .clickSendTransfer()
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_FILL_ALL_FIELDS.getMessage());

        AccountResponse account1 = SessionStorage.getSteps().getAllAccounts().getFirst();
        AccountResponse account2 = SessionStorage.getSteps().getAllAccounts().get(1);

        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    @Test
    @UserSession
    public void userCannotTransferAmountAboveMaxTest() {
        String amount = "10000.01";

        UserSteps.createAccount(SessionStorage.getUser());
        UserSteps.createAccount(SessionStorage.getUser());

        List<AccountResponse> accounts = SessionStorage.getSteps().getAllAccounts();
        String accountNumber1 = accounts.getFirst().getAccountNumber();
        String accountNumber2 = accounts.get(1).getAccountNumber();

        String recipientName = SessionStorage.getSteps().getProfile().getName();

        new TransferPage().open().transfer(accountNumber1, recipientName, accountNumber2, amount)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_ERROR_PREFIX.getMessage() + ApiError.TRANSFER_AMOUNT_TOO_LARGE.getMessage());

        AccountResponse account1 = SessionStorage.getSteps().getAllAccounts().getFirst();
        AccountResponse account2 = SessionStorage.getSteps().getAllAccounts().get(1);

        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    @Test
    @UserSession(2)
    public void userCanTransferToAnotherUserTest() {
        double amount = randomDeposit();
        String amountAsText = String.valueOf(amount);

        UserSteps.createAccount(SessionStorage.getUser(1));
        UserSteps.createAccount(SessionStorage.getUser(2));

        AccountResponse senderAccount = SessionStorage.getSteps(1).getAllAccounts().getFirst();
        AccountResponse recipientAccount = SessionStorage.getSteps(2).getAllAccounts().getFirst();
        AccountResponse depositedSenderAccount = SessionStorage.getSteps(1).deposit(senderAccount.getId(), amount);

        String recipientName = SessionStorage.getSteps(2).getProfile().getName();

        new TransferPage().open().transfer(senderAccount.getAccountNumber(), recipientName,
                        recipientAccount.getAccountNumber(), amountAsText)
                .checkAlertMessageAndAccept(BankAlert.TRANSFER_SUCCESS.getMessage() + amountAsText
                        + " to account " + recipientAccount.getAccountNumber());

        AccountResponse updatedSenderAccount = SessionStorage.getSteps(1).getAllAccounts().getFirst();
        AccountResponse updatedRecipientAccount = SessionStorage.getSteps(2).getAllAccounts().getFirst();

        assertThatAccount(updatedSenderAccount)
                .hasBalance(depositedSenderAccount.getBalance() - amount)
                .hasTransactionCount(2)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_OUT)
                        .amount(amount)
                        .relatedAccountId(recipientAccount.getId())
                        .build());

        assertThatAccount(updatedRecipientAccount)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_IN)
                        .amount(amount)
                        .relatedAccountId(senderAccount.getId())
                        .build());
    }
}
