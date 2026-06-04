package requests.skelethon.steps;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.AccountsTransferRequest;
import models.AccountsTransferResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class TransferSteps {

    // Успешный перевод — десериализует ответ в AccountsTransferResponse
    public static AccountsTransferResponse transfer(AccountSteps.UserContext from,
                                                    AccountSteps.UserContext to,
                                                    double amount) {
        return new ValidatedCrudRequester<AccountsTransferResponse>(
                from.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.TRANSFER)
                .post(buildRequest(from.accountId(), to.accountId(), amount));
    }

    // Перевод с произвольными спецификациями — для негативных сценариев
    public static void transfer(RequestSpecification reqSpec, long fromId, long toId,
                                double amount, ResponseSpecification respSpec) {
        new CrudRequester(reqSpec, respSpec, Endpoint.TRANSFER)
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