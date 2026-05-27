package iteration2;

import iteration1.BaseTest;
import models.AccountResponse;
import models.AccountsTransferResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skelethon.steps.AccountSteps;
import requests.skelethon.steps.TransferSteps;

import java.util.List;
import java.util.stream.Stream;

import static generators.RandomData.randomDeposit;
import static models.assertions.AccountAssert.assertThatAccount;
import static models.assertions.TransferAssert.assertThatTransfer;
import static org.assertj.core.api.Assertions.assertThat;
import static requests.skelethon.steps.AccountSteps.createUserWithAccount;
import static requests.skelethon.steps.DepositSteps.deposit;
import static requests.skelethon.steps.TransferSteps.transfer;

public class AccountsTransferTest extends BaseTest {

    @Test
    public void transferSuccess() {
        double amount = randomDeposit(); // диапазон randomDeposit ≤ 5000 — один deposit() покрывает всю сумму
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();
        deposit(user1, amount);

        AccountsTransferResponse response = transfer(user1, user2, amount);

        assertThatTransfer(response)
                .isSuccessful(user1.accountId(), user2.accountId(), amount);

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        assertThatAccount(account1)
                .hasBalance(0)
                .hasTransactionCount(2)
                .hasTransaction("TRANSFER_OUT", amount, user2.accountId());

        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account2)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction("TRANSFER_IN", amount, user1.accountId());
    }

    @Test
    public void transferBetweenOwnAccountsSuccess() {
        AccountSteps.UserContext user = createUserWithAccount();
        AccountSteps.UserContext userSecondAccount = AccountSteps.addAccount(user);
        deposit(user, 100.0);

        AccountsTransferResponse response = transfer(user, userSecondAccount, 100.0);

        assertThatTransfer(response)
                .isSuccessful(user.accountId(), userSecondAccount.accountId(), 100.0);

        List<AccountResponse> accounts = AccountSteps.getAccounts(user);
        assertThat(accounts).hasSize(2);

        AccountResponse senderAccount = accounts.stream()
                .filter(a -> a.getId() == user.accountId())
                .findFirst()
                .orElseThrow();
        AccountResponse receiverAccount = accounts.stream()
                .filter(a -> a.getId() == userSecondAccount.accountId())
                .findFirst()
                .orElseThrow();

        assertThatAccount(senderAccount)
                .hasBalance(0)
                .hasTransactionCount(2)
                .hasTransaction("TRANSFER_OUT", 100.0, userSecondAccount.accountId());

        assertThatAccount(receiverAccount)
                .hasBalance(100.0)
                .hasTransactionCount(1)
                .hasTransaction("TRANSFER_IN", 100.0, user.accountId());
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 10000.0})
    public void transferBoundaryAmountSuccess(double amount) {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();
        deposit(user1, 5000.0);
        deposit(user1, 5000.0);

        AccountsTransferResponse response = transfer(user1, user2, amount);

        assertThatTransfer(response)
                .hasAmount(amount)
                .hasMessage("Transfer successful");

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        assertThatAccount(account1)
                .hasBalance(10000.0 - amount)
                .hasTransactionCount(3) // 2×DEPOSIT + 1×TRANSFER_OUT
                .hasTransaction("TRANSFER_OUT", amount, user2.accountId());

        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account2)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction("TRANSFER_IN", amount, user1.accountId());
    }

    @Test
    public void transferUnauthorizedReturns401() {
        TransferSteps.transferWithoutAuth(1L, 2L, 100.0);
    }

    @Test
    public void transferInvalidTokenReturns401() {
        TransferSteps.transferWithInvalidToken(1L, 2L, 100.0);
    }

    @Test
    public void transferInsufficientFundsReturns400() {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();
        TransferSteps.transferWithInsufficientFunds(user1, user2, 100.0);

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    private static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(-1.0, "Transfer amount must be at least 0.01"),
                Arguments.of(0.0, "Transfer amount must be at least 0.01"),
                Arguments.of(10000.01, "Transfer amount cannot exceed 10000"),
                Arguments.of(10001.0, "Transfer amount cannot exceed 10000")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferAmounts")
    public void transferInvalidAmountReturns400(double amount, String expectedMessage) {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();
        TransferSteps.transferInvalidAmount(user1, user2, amount, expectedMessage);

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    @Test
    public void transferFromAnotherUsersAccountReturns403() {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();
        TransferSteps.transferFromForeignAccount(user2, user1.accountId(), user2.accountId(), 100.0);

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasNoTransactions();
        assertThatAccount(account2).hasNoTransactions();
    }

    @Test
    public void transferToNonExistentAccountReturns400() {
        AccountSteps.UserContext user = createUserWithAccount();
        deposit(user, 100.0);
        TransferSteps.transferToNonExistentAccount(user, 9999312L, 100.0);

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(100.0)
                .hasTransactionCount(1); // только DEPOSIT, TRANSFER_OUT не должно быть
    }
}