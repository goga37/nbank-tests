package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class ProfilePage extends BasePage<ProfilePage> {
    @Override
    public String url() {
        return "/edit-profile";
    }

    private SelenideElement nameInput = $(Selectors.byAttribute("placeholder", "Enter new name"));
    private SelenideElement saveButton = $(Selectors.byText("💾 Save Changes"));

    public ProfilePage changeName(String name) {
        enterName(name);
        clickSave();
        return this;
    }

    public ProfilePage enterName(String name) {
        nameInput.setValue(name);
        return this;
    }

    public ProfilePage clickSave() {
        saveButton.click();
        return this;
    }
}