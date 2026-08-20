package api;

import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.models.TransferResponse;
import api.models.comparison.ModelAssertions;
import api.skelethon.steps.AccountSteps;
import api.skelethon.steps.AdminSteps;
import api.specs.ResponseSpecs;
import common.annotations.FraudCheckMock;
import common.annotations.FraudCheckScenario;
import common.extensions.TimingExtension;
import iteration1.BaseTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLock;

import static api.generators.RandomData.randomDeposit;

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

        double depositAmount = randomDeposit();
        senderSteps.depositToAccount(senderAccount.getId(), depositAmount);

        CreateUserRequest user2 = AdminSteps.createUser();
        AccountSteps receiverSteps = new AccountSteps(user2.getUsername(), user2.getPassword());
        AccountResponse receiverAccount = receiverSteps.createAccount();

        double transferAmount = Math.random() * (depositAmount - 0.1) + 0.1;
        return new TwoAccountsForTransferContext(senderSteps, senderAccount, receiverAccount, transferAmount);
    }

    @Test
    @FraudCheckMock(scenario = FraudCheckScenario.LOW_RISK)
    public void testTransferApprovedByFraudCheck() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = FraudCheckScenario.LOW_RISK.expectedResponse(
                ctx.senderAccount().getId(), ctx.receiverAccount().getId(), ctx.transferAmount());

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(scenario = FraudCheckScenario.HIGH_RISK)
    public void testTransferRequiresVerificationByFraudCheck() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = FraudCheckScenario.HIGH_RISK.expectedResponse(
                ctx.senderAccount().getId(), ctx.receiverAccount().getId(), ctx.transferAmount());

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    @Test
    @FraudCheckMock(scenario = FraudCheckScenario.MEDIUM_RISK)
    public void testTransferRequiresManualReviewByFraudCheck() {
        TwoAccountsForTransferContext ctx = prepareTwoAccountsForTransfer();

        TransferResponse transferResponse = ctx.senderSteps().transferWithFraudCheck(
                ctx.senderAccount().getId(),
                ctx.receiverAccount().getId(),
                ctx.transferAmount()
        );

        softly.assertThat(transferResponse).isNotNull();

        TransferResponse expectedResponse = FraudCheckScenario.MEDIUM_RISK.expectedResponse(
                ctx.senderAccount().getId(), ctx.receiverAccount().getId(), ctx.transferAmount());

        ModelAssertions.assertThatModels(expectedResponse, transferResponse).match();
    }

    // Фрод-сервис недоступен/ломается (500) — банк не должен падать, а должен деградировать в ручную проверку.
    // Итоговый ответ банка в этом случае совпадает с тем, что банк отдаёт при явном решении MEDIUM_RISK
    // (см. testTransferRequiresManualReviewByFraudCheck) — переиспользуем эти ожидания, а не дублируем их.
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
        softly.assertThat(transferResponse.getStatus()).isEqualTo(FraudCheckScenario.MEDIUM_RISK.getExpectedTransferStatus());
        softly.assertThat(transferResponse.isRequiresManualReview()).isTrue();
        softly.assertThat(transferResponse.getMessage()).isEqualTo(FraudCheckScenario.MEDIUM_RISK.getExpectedTransferMessage());
    }

    @Test
    @FraudCheckMock
    public void testTransferFailsWhenInsufficientFunds() {
        CreateUserRequest user1 = AdminSteps.createUser();
        AccountSteps sender = new AccountSteps(user1.getUsername(), user1.getPassword());
        AccountResponse senderAccount = sender.createAccount();
        double depositAmount = randomDeposit();
        sender.depositToAccount(senderAccount.getId(), depositAmount);

        CreateUserRequest user2 = AdminSteps.createUser();
        AccountSteps receiver = new AccountSteps(user2.getUsername(), user2.getPassword());
        AccountResponse receiverAccount = receiver.createAccount();

        // заведомо больше баланса — не конкретное число важно, а то, что перевод превышает депозит
        double transferAmount = depositAmount + randomDeposit();

        sender.transferWithFraudCheckRaw(
                senderAccount.getId(),
                receiverAccount.getId(),
                transferAmount,
                ResponseSpecs.responseStatus400()
        );
    }

    // Backend не валидирует senderAccountId == receiverAccountId и пропускает перевод самому себе (200 APPROVED).
    // Это может быть незамеченным пробелом в бизнес-валидации — стоит обсудить с командой, баг это или нет.
    // Тест фиксирует фактическое поведение системы, а не предположение о том, как "должно быть".
    @Test
    @FraudCheckMock(scenario = FraudCheckScenario.LOW_RISK)
    public void testTransferToSameAccountIsCurrentlyAllowed() {
        CreateUserRequest user = AdminSteps.createUser();
        AccountSteps steps = new AccountSteps(user.getUsername(), user.getPassword());
        AccountResponse account = steps.createAccount();
        double depositAmount = randomDeposit();
        steps.depositToAccount(account.getId(), depositAmount);

        double transferAmount = depositAmount / 2; // заведомо меньше баланса — перевод должен пройти
        TransferResponse transferResponse = steps.transferWithFraudCheck(account.getId(), account.getId(), transferAmount);

        softly.assertThat(transferResponse).isNotNull();
        softly.assertThat(transferResponse.getStatus()).isEqualTo(FraudCheckScenario.LOW_RISK.getExpectedTransferStatus());
        softly.assertThat(transferResponse.getSenderAccountId()).isEqualTo(account.getId());
        softly.assertThat(transferResponse.getReceiverAccountId()).isEqualTo(account.getId());
    }
}