package ui.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;
import lombok.Getter;
import ui.elements.UserBage;

import java.util.List;

import static com.codeborne.selenide.Selenide.$;

@Getter
public class AdminPanel extends BasePage<AdminPanel> {
    private SelenideElement adminPanelText =  $(Selectors.byText("Admin Panel"));
    private SelenideElement addUserButton = $(Selectors.byText("Add User"));

    @Override
    public String url() {
        return "/admin";
    }

    public AdminPanel createUser(String username, String password) {
        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        addUserButton.click();
        return this;
    }

    // findBy + should* заставляет Selenide поллить DOM до таймаута,
    // а не смотреть на список один раз — иначе ловим гонку с перерисовкой списка после создания юзера
    public AdminPanel waitUntilUserIsVisible(String username) {
        getUsersList().findBy(Condition.text(username)).shouldBe(Condition.visible);
        return this;
    }

    public List<UserBage> getAllUsers() {
        return generatePageElements(getUsersList(), UserBage::new);
    }

    private ElementsCollection getUsersList() {
        return $(Selectors.byText("All Users")).parent().findAll("li");
    }
}
