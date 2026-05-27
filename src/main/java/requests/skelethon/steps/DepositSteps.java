package requests.skelethon.steps;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.AccountResponse;
import models.AccountsDepositRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class DepositSteps {

    public static AccountResponse deposit(AccountSteps.UserContext user, double amount) {
        return new ValidatedCrudRequester<AccountResponse>(
                user.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.DEPOSIT)
                .post(buildRequest(user.accountId(), amount));
    }

    public static void depositWithoutAuth(long accountId, double amount) {
        depositExpecting(RequestSpecs.unAuthSpec(), accountId, amount, ResponseSpecs.responseStatus401());
    }

    public static void depositWithInvalidToken(long accountId, double amount) {
        depositExpecting(RequestSpecs.invalidTokenSpec(), accountId, amount, ResponseSpecs.responseStatus401());
    }

    public static void depositToForeignAccount(AccountSteps.UserContext user, long foreignAccountId, double amount) {
        depositExpecting(user.spec(), foreignAccountId, amount, ResponseSpecs.responseStatus403());
    }

    public static void depositToNonExistentAccount(AccountSteps.UserContext user, long nonExistentAccountId, double amount) {
        depositExpecting(user.spec(), nonExistentAccountId, amount, ResponseSpecs.responseStatus403());
    }

    // Невалидная сумма → 400 с конкретным сообщением об ошибке
    public static void depositInvalidAmount(AccountSteps.UserContext user, double amount, String expectedMessage) {
        depositExpecting(user.spec(), user.accountId(), amount, ResponseSpecs.responseStatus400(expectedMessage));
    }

    private static void depositExpecting(RequestSpecification spec, long accountId,
                                         double amount, ResponseSpecification responseSpec) {
        new CrudRequester(spec, responseSpec, Endpoint.DEPOSIT)
                .post(buildRequest(accountId, amount));
    }

    private static AccountsDepositRequest buildRequest(long accountId, double amount) {
        return AccountsDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
    }
}