package api.specs;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ApiError {
    INVALID_PROFILE_NAME("Name must contain two words with letters only"),
    DEPOSIT_AMOUNT_TOO_SMALL("Deposit amount must be at least 0.01"),
    DEPOSIT_AMOUNT_TOO_LARGE("Deposit amount cannot exceed 5000"),
    TRANSFER_AMOUNT_TOO_SMALL("Transfer amount must be at least 0.01"),
    TRANSFER_AMOUNT_TOO_LARGE("Transfer amount cannot exceed 10000"),
    TRANSFER_INSUFFICIENT_FUNDS("Invalid transfer: insufficient funds or invalid accounts");

    private final String message;
}
