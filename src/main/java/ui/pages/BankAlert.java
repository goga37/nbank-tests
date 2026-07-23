package ui.pages;

import lombok.Getter;

@Getter
public enum BankAlert {
    USER_CREATED_SUCCESSFULLY("✅ User created successfully!"),
    USERNAME_MUST_BE_BETWEEN_3_AND_15_CHARACTERS("Username must be between 3 and 15 characters"),
    NEW_ACCOUNT_CREATED("✅ New Account Created! Account Number: "),
    DEPOSIT_SELECT_ACCOUNT_REQUIRED("❌ Please select an account."),
    DEPOSIT_INVALID_AMOUNT("❌ Please enter a valid amount."),
    DEPOSIT_AMOUNT_EXCEEDS_MAX("❌ Please deposit less or equal to 5000$."),
    DEPOSIT_SUCCESS("✅ Successfully deposited $"),
    TRANSFER_FILL_ALL_FIELDS("❌ Please fill all fields and confirm."),
    TRANSFER_RECIPIENT_NOT_FOUND("❌ No user found with this account number."),
    TRANSFER_SUCCESS("✅ Successfully transferred $"),
    TRANSFER_ERROR_PREFIX("❌ Error: "),
    PROFILE_UPDATE_SUCCESS("✅ Name updated successfully!"),
    PROFILE_INVALID_NAME("❌ Please enter a valid name.");

    private final String message;

    BankAlert(String message) {
        this.message = message;
    }
}
