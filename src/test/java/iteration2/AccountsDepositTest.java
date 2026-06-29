package iteration2;

import iteration1.BaseTest;
import api.models.AccountResponse;
import api.models.Transaction;
import api.models.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.skelethon.steps.AccountSteps;
import api.skelethon.steps.DepositSteps;
import api.specs.ApiError;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.stream.Stream;

import static api.generators.RandomData.randomDeposit;
import static api.models.assertions.AccountAssert.assertThatAccount;
import static api.skelethon.steps.AccountSteps.createUserWithAccount;
import static api.skelethon.steps.DepositSteps.deposit;

public class AccountsDepositTest extends BaseTest {

    @Test
    public void depositSuccess() {
        double amount = randomDeposit();
        AccountSteps.UserContext user = createUserWithAccount();

        AccountResponse response = deposit(user, amount);

        assertThatAccount(response)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.DEPOSIT)
                        .amount(amount)
                        .relatedAccountId(user.accountId())
                        .build());

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

        assertThatAccount(response)
                .hasBalance(amount1 + amount2)
                .hasTransactionCount(2);

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(amount1 + amount2)
                .hasTransactionCount(2);
    }

    @Test
    public void depositToNonExistentAccountReturns403() {
        AccountSteps.UserContext user = createUserWithAccount();

        DepositSteps.deposit(user.spec(), 9999312L, 100.0, ResponseSpecs.responseStatus403());

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account).hasBalance(0).hasNoTransactions();
    }

    @Test
    public void depositUnauthorizedReturns401() {
        DepositSteps.deposit(RequestSpecs.unAuthSpec(), 1L, 100.0, ResponseSpecs.responseStatus401());
    }

    @Test
    public void depositInvalidTokenReturns401() {
        DepositSteps.deposit(RequestSpecs.invalidTokenSpec(), 1L, 100.0, ResponseSpecs.responseStatus401());
    }

    @Test
    public void depositToOtherUsersAccountReturns403() {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();

        DepositSteps.deposit(user1.spec(), user2.accountId(), 100.0, ResponseSpecs.responseStatus403());

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasNoTransactions();
        assertThatAccount(account2).hasNoTransactions();
    }

    private static Stream<Arguments> invalidAmounts() {
        return Stream.of(
                Arguments.of(-1.0, ApiError.DEPOSIT_AMOUNT_TOO_SMALL),
                Arguments.of(0.0, ApiError.DEPOSIT_AMOUNT_TOO_SMALL),
                Arguments.of(5000.01, ApiError.DEPOSIT_AMOUNT_TOO_LARGE),
                Arguments.of(5001.0, ApiError.DEPOSIT_AMOUNT_TOO_LARGE)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidAmounts")
    public void depositInvalidAmountReturns400(double amount, ApiError expectedError) {
        AccountSteps.UserContext user = createUserWithAccount();

        DepositSteps.deposit(user.spec(), user.accountId(), amount, ResponseSpecs.responseStatus400(expectedError));

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
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.DEPOSIT)
                        .amount(amount)
                        .relatedAccountId(user.accountId())
                        .build());

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(amount)
                .hasTransactionCount(1);
    }
}