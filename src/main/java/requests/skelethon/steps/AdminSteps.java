package requests.skelethon.steps;

import generators.RandomModelGenerator;
import models.CreateUserRequest;
import models.CreateUserResponse;
import requests.skelethon.Endpoint;
import requests.skelethon.requests.ValidatedCrudRequester;
import specs.RequestSpecs;
import specs.ResponseSpecs;

public class AdminSteps {

    // Генерирует пользователя и возвращает request (обратная совместимость)
    public static CreateUserRequest createUser() {
        CreateUserRequest userRequest = RandomModelGenerator.generate(CreateUserRequest.class);
        createUser(userRequest);
        return userRequest;
    }

    // Создаёт пользователя по готовому request, возвращает response с userId
    public static CreateUserResponse createUser(CreateUserRequest userRequest) {
        return new ValidatedCrudRequester<CreateUserResponse>(
                RequestSpecs.adminSpec(),
                ResponseSpecs.entityWasCreated(),
                Endpoint.ADMIN_USER)
                .post(userRequest);
    }
}
