package masr;

import java.io.Serial;

public class LifecycleException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String ILLEGAL_TRANSITION = "ILLEGAL_TRANSITION";
    public static final String ALREADY_CANCELLED = "ALREADY_CANCELLED";
    public static final String ALREADY_DELIVERED = "ALREADY_DELIVERED";
    public static final String CANNOT_CANCEL = "CANNOT_CANCEL";
    public static final String ORDER_NOT_READY = "ORDER_NOT_READY";
    public static final String RIDER_NOT_ASSIGNED = "RIDER_NOT_ASSIGNED";

    public LifecycleException(String code, String message) {
        super(code, message);
    }
}
