package requests.skelethon.steps;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.AccountsTransferRequest;
import models.AccountsTransferResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class TransferSteps {

    public static AccountsTransferResponse transfer(AccountSteps.UserContext from,
                                                    AccountSteps.UserContext to,
                                                    double amount) {
        return new ValidatedCrudRequester<AccountsTransferResponse>(
                from.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.TRANSFER)
                .post(buildRequest(from.accountId(), to.accountId(), amount));
    }

    // Запрос без токена → 401
    public static void transferWithoutAuth(long fromId, long toId, double amount) {
        transferExpecting(RequestSpecs.unAuthSpec(), fromId, toId, amount, ResponseSpecs.responseStatus401());
    }

    // Запрос с невалидным токеном → 401
    public static void transferWithInvalidToken(long fromId, long toId, double amount) {
        transferExpecting(RequestSpecs.invalidTokenSpec(), fromId, toId, amount, ResponseSpecs.responseStatus401());
    }

    // Перевод с чужого счёта → 403
    public static void transferFromForeignAccount(AccountSteps.UserContext attacker,
                                                  long fromId, long toId, double amount) {
        transferExpecting(attacker.spec(), fromId, toId, amount, ResponseSpecs.responseStatus403());
    }

    // Перевод на несуществующий счёт → 400 (API не различает "не найден" и "нет средств")
    public static void transferToNonExistentAccount(AccountSteps.UserContext user,
                                                    long nonExistentToId, double amount) {
        transferExpecting(user.spec(), user.accountId(), nonExistentToId, amount,
                ResponseSpecs.responseStatus400("Invalid transfer: insufficient funds or invalid accounts"));
    }

    // Перевод при недостатке средств → 400
    public static void transferWithInsufficientFunds(AccountSteps.UserContext from,
                                                     AccountSteps.UserContext to,
                                                     double amount) {
        transferExpecting(from.spec(), from.accountId(), to.accountId(), amount,
                ResponseSpecs.responseStatus400("Invalid transfer: insufficient funds or invalid accounts"));
    }

    // Невалидная сумма → 400 с конкретным сообщением
    public static void transferInvalidAmount(AccountSteps.UserContext from,
                                             AccountSteps.UserContext to,
                                             double amount, String expectedMessage) {
        transferExpecting(from.spec(), from.accountId(), to.accountId(), amount,
                ResponseSpecs.responseStatus400(expectedMessage));
    }

    // ─── Приватные хелперы ────────────────────────────────────────────────────

    private static void transferExpecting(RequestSpecification spec, long fromId, long toId,
                                          double amount, ResponseSpecification responseSpec) {
        new CrudRequester(spec, responseSpec, Endpoint.TRANSFER)
                .post(buildRequest(fromId, toId, amount));
    }

    private static AccountsTransferRequest buildRequest(long fromId, long toId, double amount) {
        return AccountsTransferRequest.builder()
                .senderAccountId(fromId)
                .receiverAccountId(toId)
                .amount(amount)
                .build();
    }
}