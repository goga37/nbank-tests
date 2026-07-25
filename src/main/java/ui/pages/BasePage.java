package ui.pages;

import api.models.CreateUserRequest;
import api.specs.RequestSpecs;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.Selenide;
import com.codeborne.selenide.SelenideElement;
import org.openqa.selenium.Alert;
import ui.elements.BaseElement;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.codeborne.selenide.Selenide.*;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public abstract class BasePage<T extends BasePage> {
    protected SelenideElement usernameInput = $(Selectors.byAttribute("placeholder", "Username"));
    protected SelenideElement passwordInput = $(Selectors.byAttribute("placeholder", "Password"));

    public abstract String url();

    public T open() {
        return Selenide.open(url(), (Class<T>) this.getClass());
    }

    public <T extends BasePage> T getPage(Class<T> pageClass) {
        return Selenide.page(pageClass);
    }

    public T checkAlertMessageAndAccept(BankAlert bankAlert) {
        return checkAlertMessageAndAccept(bankAlert.getMessage());
    }

    // Некоторые проверки на фронте гоняются в гонке между клиентской валидацией
    // и ответом сервера, поэтому alert может показать любое из нескольких
    // легитимных сообщений — тест должен принимать любое совпадение, а не одно конкретное.
    public T checkAlertMessageAndAccept(String... possibleMessages) {
        Alert alert = switchTo().alert();
        try {
            String actual = alert.getText();
            boolean matchesAny = Arrays.stream(possibleMessages).anyMatch(actual::contains);
            assertThat(matchesAny)
                    .as("Alert text '%s' should contain one of: %s", actual, Arrays.toString(possibleMessages))
                    .isTrue();
        } finally {
            alert.accept();
        }
        return (T) this;
    }

    public static void authAsUser(String username, String password) {
        Selenide.open("/");
        String userAuthHeader = RequestSpecs.getUserAuthHeader(username, password);
        executeJavaScript("localStorage.setItem('authToken', arguments[0]);", userAuthHeader);
    }

    public static void authAsUser(CreateUserRequest createUserRequest) {
        authAsUser(createUserRequest.getUsername(), createUserRequest.getPassword());
    }

    // ElementCollection -> List<BaseElement>
    protected <T extends BaseElement> List<T> generatePageElements(ElementsCollection elementsCollection, Function<SelenideElement, T> constructor) {
        return elementsCollection.stream().map(constructor).toList();
    }
}
