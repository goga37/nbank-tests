package models.assertions;

import models.AccountsTransferResponse;
import org.assertj.core.api.AbstractAssert;
import org.assertj.core.data.Offset;

// Custom Assertion — инкапсулирует группу связанных проверок ответа на перевод.
// Расширяет AbstractAssert<SELF, ACTUAL> — стандартный способ писать кастомные ассерты в AssertJ.
public class TransferAssert extends AbstractAssert<TransferAssert, AccountsTransferResponse> {

    private static final Offset<Double> OFFSET = Offset.offset(0.001);

    protected TransferAssert(AccountsTransferResponse actual) {
        super(actual, TransferAssert.class);
    }

    public static TransferAssert assertThatTransfer(AccountsTransferResponse actual) {
        return new TransferAssert(actual);
    }

    // Полная проверка успешного перевода: ID отправителя, ID получателя, сумма, сообщение
    public TransferAssert isSuccessful(long expectedSenderId, long expectedReceiverId, double expectedAmount) {
        isNotNull();
        hasSenderAccountId(expectedSenderId);
        hasReceiverAccountId(expectedReceiverId);
        hasAmount(expectedAmount);
        hasMessage("Transfer successful");
        return this;
    }

    public TransferAssert hasSenderAccountId(long expectedId) {
        isNotNull();
        if (actual.getSenderAccountId() != expectedId) {
            failWithMessage("Expected senderAccountId to be <%d> but was <%d>",
                    expectedId, actual.getSenderAccountId());
        }
        return this;
    }

    public TransferAssert hasReceiverAccountId(long expectedId) {
        isNotNull();
        if (actual.getReceiverAccountId() != expectedId) {
            failWithMessage("Expected receiverAccountId to be <%d> but was <%d>",
                    expectedId, actual.getReceiverAccountId());
        }
        return this;
    }

    public TransferAssert hasAmount(double expectedAmount) {
        isNotNull();
        if (Math.abs(actual.getAmount() - expectedAmount) > OFFSET.value) {
            failWithMessage("Expected amount to be close to <%f> (within %f) but was <%f>",
                    expectedAmount, OFFSET.value, actual.getAmount());
        }
        return this;
    }

    public TransferAssert hasMessage(String expectedMessage) {
        isNotNull();
        if (!expectedMessage.equals(actual.getMessage())) {
            failWithMessage("Expected message to be <%s> but was <%s>",
                    expectedMessage, actual.getMessage());
        }
        return this;
    }
}