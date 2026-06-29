package api.models;

import lombok.Data;

@Data
public class LoginUserResponse extends BaseModel {
    private String username;
    private String role;
}
