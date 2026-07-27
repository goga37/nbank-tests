package iteration2;

import common.annotations.APIVersion;
import iteration1.BaseTest;
import api.models.AccountResponse;
import api.models.AccountsTransferResponse;
import api.models.Transaction;
import api.models.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import api.skelethon.steps.AccountSteps;
import api.skelethon.steps.TransferSteps;
import api.specs.ApiError;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;
import java.util.stream.Stream;

import static api.generators.RandomData.randomDeposit;
import static api.models.assertions.AccountAssert.assertThatAccount;
import static api.models.assertions.TransferAssert.assertThatTransfer;
import static org.assertj.core.api.Assertions.assertThat;
import static api.skelethon.steps.AccountSteps.createUserWithAccount;
import static api.skelethon.steps.DepositSteps.deposit;
import static api.skelethon.steps.TransferSteps.transfer;

public class AccountsTransferTest extends BaseTest {

    @Test
    public void transferSuccess() {
        double amount = randomDeposit();
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
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_OUT)
                        .amount(amount)
                        .relatedAccountId(user2.accountId())
                        .build());

        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account2)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_IN)
                        .amount(amount)
                        .relatedAccountId(user1.accountId())
                        .build());
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
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_OUT)
                        .amount(100.0)
                        .relatedAccountId(userSecondAccount.accountId())
                        .build());

        assertThatAccount(receiverAccount)
                .hasBalance(100.0)
                .hasTransactionCount(1)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_IN)
                        .amount(100.0)
                        .relatedAccountId(user.accountId())
                        .build());
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
                .hasTransactionCount(3)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_OUT)
                        .amount(amount)
                        .relatedAccountId(user2.accountId())
                        .build());

        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account2)
                .hasBalance(amount)
                .hasTransactionCount(1)
                .hasTransaction(Transaction.builder()
                        .type(TransactionType.TRANSFER_IN)
                        .amount(amount)
                        .relatedAccountId(user1.accountId())
                        .build());
    }

    @Test
    public void transferUnauthorizedReturns401() {
        TransferSteps.transfer(RequestSpecs.unAuthSpec(), 1L, 2L, 100.0, ResponseSpecs.responseStatus401());
    }

    @Test
    public void transferInvalidTokenReturns401() {
        TransferSteps.transfer(RequestSpecs.invalidTokenSpec(), 1L, 2L, 100.0, ResponseSpecs.responseStatus401());
    }

    @Test
    public void transferInsufficientFundsReturns400() {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();

        TransferSteps.transfer(user1.spec(), user1.accountId(), user2.accountId(), 100.0,
                ResponseSpecs.responseStatus400(ApiError.TRANSFER_INSUFFICIENT_FUNDS));

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    private static Stream<Arguments> invalidTransferAmounts() {
        return Stream.of(
                Arguments.of(-1.0, ApiError.TRANSFER_AMOUNT_TOO_SMALL),
                Arguments.of(0.0, ApiError.TRANSFER_AMOUNT_TOO_SMALL),
                Arguments.of(10000.01, ApiError.TRANSFER_AMOUNT_TOO_LARGE),
                Arguments.of(10001.0, ApiError.TRANSFER_AMOUNT_TOO_LARGE)
        );
    }

    @ParameterizedTest
    @MethodSource("invalidTransferAmounts")
    @APIVersion("with_validation_fix")
    public void transferInvalidAmountReturns400(double amount, ApiError expectedError) {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();

        TransferSteps.transfer(user1.spec(), user1.accountId(), user2.accountId(), amount,
                ResponseSpecs.responseStatus400(expectedError));

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasBalance(0).hasNoTransactions();
        assertThatAccount(account2).hasBalance(0).hasNoTransactions();
    }

    @Test
    public void transferFromAnotherUsersAccountReturns403() {
        AccountSteps.UserContext user1 = createUserWithAccount();
        AccountSteps.UserContext user2 = createUserWithAccount();

        TransferSteps.transfer(user2.spec(), user1.accountId(), user2.accountId(), 100.0,
                ResponseSpecs.responseStatus403());

        AccountResponse account1 = AccountSteps.getAccounts(user1).getFirst();
        AccountResponse account2 = AccountSteps.getAccounts(user2).getFirst();
        assertThatAccount(account1).hasNoTransactions();
        assertThatAccount(account2).hasNoTransactions();
    }

    @Test
    public void transferToNonExistentAccountReturns400() {
        AccountSteps.UserContext user = createUserWithAccount();
        deposit(user, 100.0);

        TransferSteps.transfer(user.spec(), user.accountId(), 9999312L, 100.0,
                ResponseSpecs.responseStatus400(ApiError.TRANSFER_INSUFFICIENT_FUNDS));

        AccountResponse account = AccountSteps.getAccounts(user).getFirst();
        assertThatAccount(account)
                .hasBalance(100.0)
                .hasTransactionCount(1);
    }
}