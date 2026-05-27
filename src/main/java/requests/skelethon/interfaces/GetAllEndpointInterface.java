package requests.skelethon.interfaces;

import io.restassured.response.ValidatableResponse;

public interface GetAllEndpointInterface {
    Object getAll(Class<?> clazz);
}
