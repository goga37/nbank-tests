package api.models.assertions;

import api.models.Customer;
import org.assertj.core.api.AbstractAssert;

import java.util.Objects;

public class CustomerAssert extends AbstractAssert<CustomerAssert, Customer> {

    protected CustomerAssert(Customer actual) {
        super(actual, CustomerAssert.class);
    }

    public static CustomerAssert assertThatCustomer(Customer actual) {
        return new CustomerAssert(actual);
    }

    public CustomerAssert hasId(long expectedId) {
        isNotNull();
        if (actual.getId() != expectedId) {
            failWithMessage("Expected customer id to be <%d> but was <%d>", expectedId, actual.getId());
        }
        return this;
    }

    public CustomerAssert hasUsername(String expectedUsername) {
        isNotNull();
        if (!expectedUsername.equals(actual.getUsername())) {
            failWithMessage("Expected customer username to be <%s> but was <%s>",
                    expectedUsername, actual.getUsername());
        }
        return this;
    }

    public CustomerAssert hasName(String expectedName) {
        isNotNull();
        if (!Objects.equals(expectedName, actual.getName())) {
            failWithMessage("Expected customer name to be <%s> but was <%s>",
                    expectedName, actual.getName());
        }
        return this;
    }

    public CustomerAssert hasNullName() {
        return hasName(null);
    }

    // Составная проверка всех полей — по аналогии с TransferAssert.isSuccessful()
    public CustomerAssert matches(long expectedId, String expectedUsername, String expectedName) {
        return hasId(expectedId)
                .hasUsername(expectedUsername)
                .hasName(expectedName);
    }
}