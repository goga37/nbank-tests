package api.skelethon.steps;

import api.generators.RandomModelGenerator;
import io.restassured.specification.RequestSpecification;
import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.models.CreateUserResponse;
import api.skelethon.Endpoint;
import api.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class AccountSteps {

    // userId и username нужны для проверок профиля и других user-level операций
    public record UserContext(RequestSpecification spec, long accountId, long userId, String username) {
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
        AccountResponse newAccount = new ValidatedCrudRequester<AccountResponse>(
                user.spec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS)
                .post(null);
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