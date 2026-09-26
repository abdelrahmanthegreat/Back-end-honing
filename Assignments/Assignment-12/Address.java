package masr;

import java.util.Objects;

public record Address(String customerId, District district, String detail) {

    public Address {
        Objects.requireNonNull(customerId, "customerId must not be null");
        Objects.requireNonNull(district, "district must not be null");
        detail = detail == null ? "" : detail.trim();
        if (detail.isEmpty()) {
            throw new IllegalArgumentException("an address needs a detail line");
        }
    }

    public static Address of(String customerId, District district, String detail) throws ValidationException {
        if (customerId == null || customerId.isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "a customer id is required for an address");
        }
        if (district == null) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "a district is required for an address");
        }
        if (detail == null || detail.isBlank()) {
            throw new ValidationException(ValidationException.INVALID_INPUT, "an address needs a detail line");
        }
        return new Address(customerId.trim(), district, detail.trim());
    }

    public String shortLine() {
        return district.label() + " - " + detail;
    }

    @Override
    public String toString() {
        return shortLine();
    }
}
