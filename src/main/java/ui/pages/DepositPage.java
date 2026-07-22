package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class DepositPage extends BasePage<DepositPage> {
    @Override
    public String url() {
        return "/deposit";
    }

    private SelenideElement accountSelect = $(Selectors.byClassName("account-selector"));
    private SelenideElement amountInput = $(Selectors.byClassName("deposit-input"));
    private SelenideElement depositButton = $(Selectors.byText("\uD83D\uDCB5 Deposit"));


    public DepositPage addDeposit(String accountNumber, String amount) {
        selectAccount(accountNumber);
        enterAmount(amount);
        clickDeposit();
        return this;
    }

    public DepositPage selectAccount(String accountNumber) {
        accountSelect.selectOptionContainingText(accountNumber);
        return this;
    }

    public DepositPage enterAmount(String amount) {
        amountInput.setValue(amount);
        return this;
    }

    public DepositPage clickDeposit() {
        depositButton.click();
        return this;
    }
}
