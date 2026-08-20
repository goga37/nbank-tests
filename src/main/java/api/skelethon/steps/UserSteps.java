package api.skelethon.steps;

import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.models.Customer;
import api.skelethon.Endpoint;
import api.skelethon.requests.ValidatedCrudRequester;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

import java.util.List;

public class UserSteps {
    private String username;
    private String password;

    public static AccountResponse createAccount(CreateUserRequest userRequest) {
        return AccountSteps.postAccount(RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()));
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
        return DepositSteps.postDeposit(RequestSpecs.authAsUser(username, password), accountId, amount);
    }
}
