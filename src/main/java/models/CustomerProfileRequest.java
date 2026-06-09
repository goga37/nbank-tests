package models;

import generators.GeneratingRule;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CustomerProfileRequest extends BaseModel {

    // Имя: два слова через пробел, только буквы, каждое от 2 до 10 символов
    @GeneratingRule(regex = "[A-Z][a-z]{1,9} [A-Z][a-z]{1,9}")
    private String name;
}