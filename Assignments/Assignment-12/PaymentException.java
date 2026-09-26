package masr;

import java.io.Serial;

public class PaymentException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String INSUFFICIENT_FUNDS = "INSUFFICIENT_FUNDS";
    public static final String ALREADY_PAID = "ALREADY_PAID";
    public static final String NOT_PAID = "NOT_PAID";
    public static final String ORDER_NOT_PAYABLE = "ORDER_NOT_PAYABLE";
    public static final String INVALID_AMOUNT = "INVALID_AMOUNT";

    public PaymentException(String code, String message) {
        super(code, message);
    }
}
