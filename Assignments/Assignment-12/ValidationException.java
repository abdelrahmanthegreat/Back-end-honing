package masr;

import java.io.Serial;

public class ValidationException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String INVALID_INPUT = "INVALID_INPUT";
    public static final String DUPLICATE_ID = "DUPLICATE_ID";
    public static final String NOT_FOUND = "NOT_FOUND";
    public static final String INVALID_MOBILE = "INVALID_MOBILE";
    public static final String INVALID_PRICE = "INVALID_PRICE";
    public static final String INVALID_QUANTITY = "INVALID_QUANTITY";
    public static final String INVALID_RATING = "INVALID_RATING";
    public static final String INVALID_VALUE = "INVALID_VALUE";
    public static final String EMPTY_ORDER = "EMPTY_ORDER";
    public static final String ADDRESS_NOT_OWNED = "ADDRESS_NOT_OWNED";
    public static final String NO_ADDRESS = "NO_ADDRESS";

    public ValidationException(String code, String message) {
        super(code, message);
    }

    public ValidationException(String message) {
        super(INVALID_INPUT, message);
    }

    public ValidationException(String message, Throwable cause) {
        super(INVALID_INPUT, message, cause);
    }
}
