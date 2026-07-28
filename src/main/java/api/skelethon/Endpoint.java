package api.skelethon;

import api.models.*;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Endpoint {
    ADMIN_USER(
            "/admin/users",
            CreateUserRequest.class,
            CreateUserResponse.class
    ),
    LOGIN(
            "/auth/login",
            LoginUserRequest.class,
            LoginUserResponse.class
    ),
    ACCOUNTS(
            "/accounts",
            BaseModel.class,
            AccountResponse.class
    ),
    DEPOSIT(
            "/accounts/deposit",
            AccountsDepositRequest.class,
            AccountResponse.class),
    CUSTOMER_ACCOUNTS(
            "/customer/accounts",
            BaseModel.class,
            AccountResponse.class
    ),
    TRANSFER(
            "/accounts/transfer",
            AccountsTransferRequest.class,
            AccountsTransferResponse.class
    ),
    CUSTOMER_PROFILE(
            "/customer/profile",
            CustomerProfileRequest.class,
            CustomerProfileResponse.class
    ),
    CUSTOMER_PROFILE_GET(
            "/customer/profile",
            BaseModel.class,
            Customer.class
    ),

    TRANSFER_WITH_FRAUD_CHECK(
            "/accounts/transfer-with-fraud-check",
            TransferRequest.class,
            TransferResponse.class
    ),

    FRAUD_CHECK_STATUS(
            "/api/v1/accounts/fraud-check/{transactionId}",
            BaseModel.class,
            FraudCheckResponse.class
    );
    private final String url;
    private final Class<? extends BaseModel> requestModel;
    private final Class<? extends BaseModel> responseModel;
}
