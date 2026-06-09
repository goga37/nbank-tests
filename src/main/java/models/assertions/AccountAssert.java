package models.assertions;

import models.AccountResponse;
import models.Transaction;
import org.assertj.core.api.AbstractAssert;

import java.util.List;

// Custom Assertion для AccountResponse.
// Инкапсулирует проверки баланса и транзакций — тест описывает ЧТО, не КАК.
public class AccountAssert extends AbstractAssert<AccountAssert, AccountResponse> {

    private static final double AMOUNT_TOLERANCE = 0.001;

    protected AccountAssert(AccountResponse actual) {
        super(actual, AccountAssert.class);
    }

    // Точка входа — статический фабричный метод, как в TransferAssert
    public static AccountAssert assertThatAccount(AccountResponse actual) {
        return new AccountAssert(actual);
    }

    // Проверка баланса с допуском для double-арифметики
    public AccountAssert hasBalance(double expected) {
        isNotNull();
        if (Math.abs(actual.getBalance() - expected) > AMOUNT_TOLERANCE) {
            failWithMessage(
                    "Expected balance to be <%s> but was <%s>",
                    expected, actual.getBalance()
            );
        }
        return this;
    }

    // Проверка точного количества транзакций на счёте
    public AccountAssert hasTransactionCount(int expected) {
        isNotNull();
        int actualCount = actual.getTransactions().size();
        if (actualCount != expected) {
            failWithMessage(
                    "Expected <%d> transaction(s) but found <%d>.\nActual transactions: %s",
                    expected, actualCount, actual.getTransactions()
            );
        }
        return this;
    }

    // Удобный алиас для hasTransactionCount(0) — выражает намерение явно
    public AccountAssert hasNoTransactions() {
        return hasTransactionCount(0);
    }

    // Ключевой метод: проверяет что существует ровно одна транзакция,
    // совпадающая по type, amount и relatedAccountId.
    // id и timestamp — серверные поля, не сравниваем.
    // "Ровно одна" — потому что дубль транзакции тоже был бы багом.
    public AccountAssert hasTransaction(Transaction expected) {
        isNotNull();

        List<Transaction> matching = actual.getTransactions().stream()
                .filter(tx -> expected.getType().equals(tx.getType()))
                .filter(tx -> Math.abs(tx.getAmount() - expected.getAmount()) <= AMOUNT_TOLERANCE)
                .filter(tx -> tx.getRelatedAccountId() == expected.getRelatedAccountId())
                .toList();

        if (matching.isEmpty()) {
            failWithMessage(
                    "Expected transaction [type=%s, amount=%s, relatedAccountId=%d] not found.\nActual transactions: %s",
                    expected.getType(), expected.getAmount(), expected.getRelatedAccountId(), actual.getTransactions()
            );
        } else if (matching.size() > 1) {
            failWithMessage(
                    "Expected exactly 1 transaction [type=%s, amount=%s, relatedAccountId=%d] but found <%d>.\nMatches: %s",
                    expected.getType(), expected.getAmount(), expected.getRelatedAccountId(), matching.size(), matching
            );
        }

        return this;
    }
}