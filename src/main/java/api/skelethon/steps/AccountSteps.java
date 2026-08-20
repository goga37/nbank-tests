package api.skelethon.steps;

import api.generators.RandomModelGenerator;
import io.restassured.response.ValidatableResponse;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.models.TransferRequest;
import api.models.TransferResponse;
import api.skelethon.Endpoint;
import api.skelethon.requests.CrudRequester;
import api.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AccountSteps {
    private String username;
    private String password;

    public AccountSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // userId и username нужны для проверок профиля и других user-level операций
    public record UserContext(RequestSpecification spec, long accountId, long userId, String username) {
    }

    public AccountResponse createAccount() {
        return StepLogger.log("User " + username + " creates account", () ->
                postAccount(RequestSpecs.authAsUser(username, password)));
    }

    // общий POST /accounts — переиспользуется UserSteps.createAccount(CreateUserRequest), createAccount() и addAccount()
    static AccountResponse postAccount(RequestSpecification spec) {
        return new ValidatedCrudRequester<AccountResponse>(
                spec,
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS)
                .post(null);
    }

    public AccountResponse depositToAccount(long accountId, double amount) {
        return StepLogger.log("User " + username + " deposits " + amount + " to account " + accountId, () ->
                DepositSteps.postDeposit(RequestSpecs.authAsUser(username, password), accountId, amount));
    }

    public TransferResponse transferWithFraudCheck(long senderAccountId, long receiverAccountId, double amount) {
        return StepLogger.log("User " + username + " transfers " + amount
                        + " from account " + senderAccountId + " to account " + receiverAccountId + " with fraud check", () ->
                new ValidatedCrudRequester<TransferResponse>(
                        RequestSpecs.authAsUser(username, password),
                        ResponseSpecs.requestReturnsOK(),
                        Endpoint.TRANSFER_WITH_FRAUD_CHECK)
                        .post(buildTransferRequest(senderAccountId, receiverAccountId, amount)));
    }

    // сырой вариант для негативных сценариев — сам решаешь, какой статус/тело ожидать
    public ValidatableResponse transferWithFraudCheckRaw(long senderAccountId, long receiverAccountId, double amount,
                                                           ResponseSpecification respSpec) {
        return StepLogger.log("User " + username + " transfers " + amount
                        + " from account " + senderAccountId + " to account " + receiverAccountId + " with fraud check (raw)", () ->
                new CrudRequester(RequestSpecs.authAsUser(username, password), respSpec, Endpoint.TRANSFER_WITH_FRAUD_CHECK)
                        .post(buildTransferRequest(senderAccountId, receiverAccountId, amount)));
    }

    private static TransferRequest buildTransferRequest(long senderAccountId, long receiverAccountId, double amount) {
        return TransferRequest.builder()
                .senderAccountId(senderAccountId)
                .receiverAccountId(receiverAccountId)
                .amount(amount)
                .build();
    }

    public static UserContext createUserWithAccount() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);
        CreateUserResponse userResponse = AdminSteps.createUser(userRequest); // один запрос, получаем userId
        AccountResponse accountResponse = UserSteps.createAccount(userRequest);
        RequestSpecification spec = RequestSpecs.authAsUser(
                userRequest.getUsername(),
                userRequest.getPassword()
        );
        return new UserContext(spec, accountResponse.getId(), userResponse.getId(), userRequest.getUsername());
    }

    public static UserContext addAccount(UserContext user) {
        AccountResponse newAccount = postAccount(user.spec());
        // userId и username наследуем от исходного пользователя — новый счёт, тот же юзер
        return new UserContext(user.spec(), newAccount.getId(), user.userId(), user.username());
    }

    public static List<AccountResponse> getAccounts(UserContext user) {
        return new ValidatedCrudRequester<AccountResponse>(
                user.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_ACCOUNTS)
                .getAll(AccountResponse[].class);
    }
}