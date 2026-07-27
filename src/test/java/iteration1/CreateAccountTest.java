package iteration1;

import api.dao.AccountDao;
import api.dao.comparison.DaoAndModelAssertions;
import api.models.AccountResponse;
import api.models.CreateUserRequest;
import api.skelethon.requests.ValidatedCrudRequester;
import api.skelethon.steps.DataBaseSteps;
import org.junit.jupiter.api.Test;
import api.skelethon.Endpoint;
import api.skelethon.steps.AdminSteps;
import api.specs.RequestSpecs;
import api.specs.ResponseSpecs;

public class CreateAccountTest extends BaseTest {

    @Test
    public void userCanCreateAccountTest() {
        CreateUserRequest userRequest = AdminSteps.createUser();

        AccountResponse accountResponse = new ValidatedCrudRequester<AccountResponse>(
                RequestSpecs.authAsUser(userRequest.getUsername(), userRequest.getPassword()),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ACCOUNTS)
                .post(null);

        AccountDao accountDao = DataBaseSteps.getAccountByAccountNumber(accountResponse.getAccountNumber());
        DaoAndModelAssertions.assertThat(accountResponse, accountDao).match();
    }
}
