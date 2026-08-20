package api.skelethon.steps;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.AccountResponse;
import api.models.AccountsDepositRequest;
import api.skelethon.Endpoint;
import api.skelethon.requests.CrudRequester;
import api.skelethon.requests.ValidatedCrudRequester;
import api.specs.ResponseSpecs;

public class DepositSteps {

    // Успешный депозит — десериализует ответ в AccountResponse
    public static AccountResponse deposit(AccountSteps.UserContext user, double amount) {
        return postDeposit(user.spec(), user.accountId(), amount);
    }

    // общий POST /accounts/deposit — переиспользуется deposit(UserContext, amount) и UserSteps.deposit(accountId, amount)
    static AccountResponse postDeposit(RequestSpecification spec, long accountId, double amount) {
        return new ValidatedCrudRequester<AccountResponse>(
                spec,
                ResponseSpecs.requestReturnsOK(),
                Endpoint.DEPOSIT)
                .post(buildRequest(accountId, amount));
    }

    // Депозит с произвольными спецификациями — для негативных сценариев
    public static void deposit(RequestSpecification reqSpec, long accountId,
                               double amount, ResponseSpecification respSpec) {
        new CrudRequester(reqSpec, respSpec, Endpoint.DEPOSIT)
                .post(buildRequest(accountId, amount));
    }

    static AccountsDepositRequest buildRequest(long accountId, double amount) {
        return AccountsDepositRequest.builder()
                .accountId(accountId)
                .amount(amount)
                .build();
    }
}