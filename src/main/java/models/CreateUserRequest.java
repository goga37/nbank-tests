package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateUserRequest extends BaseModel {
    @GeneratingRule(regex = "[A-Z]{3}[a-z]{5}\\d{2}")
    private String username;
    @GeneratingRule(regex = "[A-Z]{3}[a-z]{3}!\\d{2}")
    private String password;
    @GeneratingRule(regex = "USER")
    private String role;
}
