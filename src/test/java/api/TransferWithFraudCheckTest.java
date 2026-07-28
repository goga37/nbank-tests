package api;

import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.models.TransferResponse;
import api.models.comparison.ModelAssertions;
import api.skelethon.steps.AccountSteps;
import api.skelethon.steps.AdminSteps;
import api.specs.ResponseSpecs;
import common.annotations.FraudCheckMock;
import common.extensions.TimingExtension;
import iteration1.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

@ExtendWith({TimingExtension.class, FraudCheckWireMockExtension.class})
@ResourceLock(value = "fraud-check-wiremock-port", mode = ResourceAccessMode.READ_WRITE)
public class TransferWithFraudCheckTest extends BaseTest {

    public record TwoAccountsForTransferContext(AccountSteps senderSteps, AccountResponse senderAccount,
                                                  AccountResponse receiverAccount, double transferAmount) {
    }

    private TwoAccountsForTransferContext prepareTwoAccountsForTransfer() {
        CreateUserRequest user1 = AdminSteps.createUser();
        AccountSteps senderSteps = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountResponse senderAccount = senderSteps.createAccount();

        double depositAmount = Math.random() * 4999.9 + 0.1;
        senderSteps.depositToAccount(senderAccount.getId(), depositAmount);

        CreateUserRequest user2 = AdminSteps.createUser();
        AccountSteps receiverSteps = new AccountSteps(user2.getUsername(), user2.getPassword());
        AccountResponse receiverAccount = receiverSteps.createAccount();

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;
        return new TwoAccountsForTransferContext(senderSteps, senderAccount, receiverAccount, transferAmount);
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferApprovedByFraudCheck() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("APPROVED")
                .message("Transfer approved and processed immediately")
                .amount(ctx.transferAmount())
                .senderAccountId(ctx.senderAccount().getId())
                .receiverAccountId(ctx.receiverAccount().getId())
                .fraudRiskScore(0.2)
                .fraudReason("Low risk transaction")
                .requiresManualReview(false)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "REJECTED",
            riskScore = 0.9,
            reason = "High risk transaction pattern detected",
            requiresManualReview = false,
            additionalVerificationRequired = true
    )
    public void testTransferRequiresVerificationByFraudCheck() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("VERIFICATION_REQUIRED")
                .message("Additional verification required")
                .amount(ctx.transferAmount())
                .senderAccountId(ctx.senderAccount().getId())
                .receiverAccountId(ctx.receiverAccount().getId())
                .fraudRiskScore(0.9)
                .fraudReason("High risk transaction pattern detected")
                .requiresManualReview(false)
                .requiresVerification(true)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "MANUAL_REVIEW",
            riskScore = 0.6,
            reason = "Transaction pattern requires human review",
            requiresManualReview = true,
            additionalVerificationRequired = false
    )
    public void testTransferRequiresManualReviewByFraudCheck() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = TransferResponse.builder()
                .status("MANUAL_REVIEW_REQUIRED")
                .message("Transfer requires manual review")
                .amount(ctx.transferAmount())
                .senderAccountId(ctx.senderAccount().getId())
                .receiverAccountId(ctx.receiverAccount().getId())
                .fraudRiskScore(0.6)
                .fraudReason("Transaction pattern requires human review")
                .requiresManualReview(true)
                .requiresVerification(false)
                .build();

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    // Фрод-сервис недоступен/ломается (500) — банк не должен падать, а должен деградировать в ручную проверку
    @Test
    @FraudCheckMock(httpStatus = 500)
    public void testTransferFallsBackToManualReviewWhenFraudServiceIsDown() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();
        softly.assertThat(transferResponse.getStatus()).isEqualTo("MANUAL_REVIEW_REQUIRED");
        softly.assertThat(transferResponse.isRequiresManualReview()).isTrue();
        softly.assertThat(transferResponse.getMessage()).isEqualTo("Transfer requires manual review");
    }

    @Test
    @FraudCheckMock
    public void testTransferFailsWhenInsufficientFunds() {
        CreateUserRequest user1 = AdminSteps.createUser();
        AccountSteps sender = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountResponse senderAccount = sender.createAccount();
        sender.depositToAccount(senderAccount.getId(), 10.0);

        CreateUserRequest user2 = AdminSteps.createUser();
        AccountSteps receiver = new AccountSteps(user2.getUsername(), user2.getPassword());
        AccountResponse receiverAccount = receiver.createAccount();

        sender.transferWithFraudCheckRaw(
                senderAccount.getId(),
                receiverAccount.getId(),
                1000.0,
                ResponseSpecs.responseStatus400()
        );
    }

    // Backend не валидирует senderAccountId == receiverAccountId и пропускает перевод самому себе (200 APPROVED).
    // Это может быть незамеченным пробелом в бизнес-валидации — стоит обсудить с командой, баг это или нет.
    // Тест фиксирует фактическое поведение системы, а не предположение о том, как "должно быть".
    @Test
    @FraudCheckMock(
            status = "SUCCESS",
            decision = "APPROVED",
            riskScore = 0.2,
            reason = "Low risk transaction",
            requiresManualReview = false,
            additionalVerificationRequired = false
    )
    public void testTransferToSameAccountIsCurrentlyAllowed() {
        CreateUserRequest user = AdminSteps.createUser();
        AccountSteps steps = new AccountSteps(user.getUsername(), user.getPassword());
        AccountResponse account = steps.createAccount();
        steps.depositToAccount(account.getId(), 100.0);

        TransferResponse transferResponse = steps.transferWithFraudCheck(account.getId(), account.getId(), 50.0);

        softly.assertThat(transferResponse).isNotNull();
        softly.assertThat(transferResponse.getStatus()).isEqualTo("APPROVED");
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(account.getId());
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(account.getId());
    }
}