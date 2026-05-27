package iteration2;

import iteration1.BaseTest;
import models.AccountResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import requests.skelethon.steps.AccountSteps;
import requests.skelethon.steps.DepositSteps;

import java.util.stream.Stream;

import static generators.RandomData.randomDeposit;
import static models.assertions.AccountAssert.assertThatAccount;
import static requests.skelethon.steps.AccountSteps.createUserWithAccount;
import static requests.skelethon.steps.DepositSteps.deposit;

public class AccountsDepositTest extends BaseTest {

    @Test
    public void depositSuccess() {
        double amount = randomDeposit();
        AccountSteps.UserContext user = createUserWithAccount();

        AccountResponse response = deposit(user, amount);

        assertThatAccount(response)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction("DEPOSIT", amount, user.accountId());

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(amount)
                .hasTransactionCount(1);
    }

    @Test
    public void depositAccumulationSuccess() {
        double amount1 = randomDeposit();
        double amount2 = randomDeposit();
        AccountSteps.UserContext user = createUserWithAccount();

        deposit(user, amount1);
        AccountResponse response = deposit(user, amount2);

        // После второго депозита: баланс суммируется, транзакций стало 2
        assertThatAccount(response)
                .hasBalance(amount1 + amount2)
                .hasTransactionCount(2);

        // GET подтверждает
        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(amount1 + amount2)
                .hasTransactionCount(2);
    }

    @Test
    public void depositToNonExistentAccountReturns403() {
        AccountSteps.UserContext user = createUserWithAccount();

        DepositSteps.depositToNonExistentAccount(user, 9999312L, 100.0);

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account).hasBalance(0).hasNoTransactions();
    }

    @Test
    public void depositUnauthorizedReturns401() {
        DepositSteps.depositWithoutAuth(1L, 100.0);
    }

    @Test
    public void depositInvalidTokenReturns401() {
        DepositSteps.depositWithInvalidToken(1L, 100.0);
    }

    @Test
    public void depositToOtherUsersAccountReturns403() {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();

        DepositSteps.depositToForeignAccount(user1, user2.accountId(), 100.0);

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasNoTransactions();
        assertThatAccount(account2).hasNoTransactions();
    }

    private static Stream<Arguments> invalidAmounts() {
        return Stream.of(
                Arguments.of(-1.0, "Deposit amount must be at least 0.01"),
                Arguments.of(0.0, "Deposit amount must be at least 0.01"),
                Arguments.of(5000.01, "Deposit amount cannot exceed 5000"),
                Arguments.of(5001.0, "Deposit amount cannot exceed 5000")
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    public void depositInvalidAmountReturns400(double amount, String expectedMessage) {
        AccountSteps.UserContext user = createUserWithAccount();

        DepositSteps.depositInvalidAmount(user, amount, expectedMessage);

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account).hasBalance(0).hasNoTransactions();
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.01, 5000.0})
    public void depositBoundaryAmountSuccess(double amount) {
        AccountSteps.UserContext user = createUserWithAccount();

        AccountResponse response = deposit(user, amount);

        assertThatAccount(response)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction("DEPOSIT", amount, user.accountId());

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(amount)
                .hasTransactionCount(1);
    }
}