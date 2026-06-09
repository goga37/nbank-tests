package requests.skelethon.interfaces;

import models.BaseModel;

public interface CrudEndpointInterface {
    Object post(BaseModel model);

    default Object post() {
        return post(null);
    }
    Object get();
    Object put(BaseModel model);
    Object delete(long id);
}
