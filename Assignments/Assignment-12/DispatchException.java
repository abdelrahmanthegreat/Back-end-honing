package masr;

import java.io.Serial;

public class DispatchException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String RIDER_BUSY = "RIDER_BUSY";
    public static final String RIDER_OFF_DUTY = "RIDER_OFF_DUTY";
    public static final String NO_RIDER_AVAILABLE = "NO_RIDER_AVAILABLE";
    public static final String RIDER_INELIGIBLE = "RIDER_INELIGIBLE";
    public static final String NO_READY_ORDER = "NO_READY_ORDER";
    public static final String ORDER_NOT_READY = "ORDER_NOT_READY";
    public static final String ALREADY_ASSIGNED = "ALREADY_ASSIGNED";

    public DispatchException(String code, String message) {
        super(code, message);
    }
}
