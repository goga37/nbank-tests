package ui.pages;

import com.codeborne.selenide.Selectors;
import com.codeborne.selenide.SelenideElement;

import static com.codeborne.selenide.Selenide.$;

public class TransferPage extends BasePage<TransferPage> {
    @Override
    public String url() {
        return "/transfer";
    }

    private SelenideElement accountSelect = $(Selectors.byClassName("account-selector"));
    private SelenideElement recipientNameInput = $(Selectors.byAttribute("placeholder",
            "Enter recipient name"));
    private SelenideElement recipientAccountInput = $(Selectors.byAttribute("placeholder",
            "Enter recipient account number"));
    private SelenideElement amountInput = $(Selectors.byAttribute("placeholder",
            "Enter amount"));
    private SelenideElement confirmCheckbox = $(Selectors.byId("confirmCheck"));
    private SelenideElement sendTransferButton = $(Selectors.byText("🚀 Send Transfer"));

    public TransferPage transfer(String accountNumber, String recipientName, String recipientAccountNumber, String amount) {
        selectAccount(accountNumber);
        enterRecipientName(recipientName);
        enterRecipientAccount(recipientAccountNumber);
        enterAmount(amount);
        confirm();
        clickSendTransfer();
        return this;
    }

    public TransferPage selectAccount(String accountNumber) {
        accountSelect.selectOptionContainingText(accountNumber);
        return this;
    }

    public TransferPage enterRecipientName(String recipientName) {
        recipientNameInput.setValue(recipientName);
        return this;
    }

    public TransferPage enterRecipientAccount(String recipientAccountNumber) {
        recipientAccountInput.setValue(recipientAccountNumber);
        return this;
    }

    public TransferPage enterAmount(String amount) {
        amountInput.setValue(amount);
        return this;
    }

    public TransferPage confirm() {
        confirmCheckbox.click();
        return this;
    }

    public TransferPage clickSendTransfer() {
        sendTransferButton.click();
        return this;
    }
}
