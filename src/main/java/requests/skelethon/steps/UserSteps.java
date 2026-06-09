package requests.skelethon.steps;

import models.*;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class UserSteps {
    public static AccountResponse createAccount(CreateUserRequest userRequest) {
        return new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS)
                .post(null);
    }
}
