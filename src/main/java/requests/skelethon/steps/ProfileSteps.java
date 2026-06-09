package requests.skelethon.steps;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import models.Customer;
import models.CustomerProfileRequest;
import models.CustomerProfileResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.CrudRequester;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.ResponseSpecs;

public class ProfileSteps {

    // GET /customer/profile — проверяем что изменения сохранились
    // Возвращает Customer напрямую: GET возвращает плоский объект {id, username, name, ...}
    // в отличие от PUT, который оборачивает в {customer: {...}, message: "..."}
    public static Customer getProfile(AccountSteps.UserContext user) {
        return new ValidatedCrudRequester<Customer>(
                user.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE_GET)
                .get();
    }

    // Happy path — возвращает десериализованный ответ для проверки в тесте
    public static CustomerProfileResponse updateProfile(AccountSteps.UserContext user,
                                                        CustomerProfileRequest request) {
        return new ValidatedCrudRequester<CustomerProfileResponse>(
                user.spec(),
                ResponseSpecs.requestReturnsOK(),
                Endpoint.CUSTOMER_PROFILE)
                .put(request);
    }

    // Для негативных сценариев — произвольный spec и ожидаемый статус
    public static void updateProfile(RequestSpecification spec,
                                     CustomerProfileRequest request,
                                     ResponseSpecification expectedResponse) {
        new CrudRequester(spec, expectedResponse, Endpoint.CUSTOMER_PROFILE)
                .put(request);
    }
}