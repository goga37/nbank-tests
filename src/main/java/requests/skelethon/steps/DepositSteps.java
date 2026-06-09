package requests.skelethon.steps;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.AccountResponse;
import models.AccountsDepositRequest;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class DepositSteps {

    // Успешный депозит — десериализует ответ в AccountResponse
    public static AccountResponse deposit(AccountSteps.UserContext user, double amount) {
        return new ValidatedCrudRequester<AccountResponse>(
                user.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.DEPOSIT)
                .post(buildRequest(user.accountId(), amount));
    }

    // Депозит с произвольными спецификациями — для негативных сценариев
    public static void deposit(RequestSpecification reqSpec, long accountId,
                               double amount, ResponseSpecification respSpec) {
        new CrudRequester(reqSpec, respSpec, Endpoint.DEPOSIT)
                .post(buildRequest(accountId, amount));
    }

    private static AccountsDepositRequest buildRequest(long accountId, double amount) {
        return AccountsDepositRequest.builder()
                .id(accountId)
                .balance(amount)
                .build();
    }
}