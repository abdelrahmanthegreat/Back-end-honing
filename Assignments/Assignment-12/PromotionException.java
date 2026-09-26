package masr;

import java.io.Serial;

public class PromotionException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String UNKNOWN_CODE = "UNKNOWN_CODE";
    public static final String EXPIRED = "EXPIRED";
    public static final String MINIMUM_SUBTOTAL_NOT_MET = "MINIMUM_SUBTOTAL_NOT_MET";
    public static final String DISTRICT_NOT_ALLOWED = "DISTRICT_NOT_ALLOWED";
    public static final String NOT_FIRST_TIME = "NOT_FIRST_TIME";
    public static final String ALREADY_APPLIED = "ALREADY_APPLIED";
    public static final String NOT_APPLICABLE = "NOT_APPLICABLE";
    public static final String DUPLICATE_CODE = "DUPLICATE_CODE";

    public PromotionException(String code, String message) {
        super(code, message);
    }
}
