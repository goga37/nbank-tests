package ui;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class LoginPage extends BasePage<LoginPage> {
    private SelenideElement buttonInput = $("button");

    @Override
    public String url() {
        return "/login";
    }

    public LoginPage login(String username, String password) {
        usernameInmput.sendKeys(username);
        passwordInput.sendKeys(password);
        buttonInput.click();
        return this;
    }
}
