package api.skelethon.steps;

import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.models.Customer;
import api.skelethon.Endpoint;
import api.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

import static api.skelethon.steps.DepositSteps.buildRequest;

public class UserSteps {
    private String username;
    private String password;

    public static AccountResponse createAccount(CreateUserRequest userRequest) {
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS)
                .post(null);
    }

    public UserSteps(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public List<AccountResponse> getAllAccounts() {
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_ACCOUNTS).getAll(AccountResponse[].class);
    }

    public Customer getProfile() {
        return new ValidatedCrudRequester<Customer>(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_GET)
                .get();
    }
    public AccountResponse deposit(long accountId, double amount) {
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(username, password),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.DEPOSIT)
                .post(buildRequest(accountId, amount));
    }
}
