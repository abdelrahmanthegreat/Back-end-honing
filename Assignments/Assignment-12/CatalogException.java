package masr;

import java.io.Serial;

public class CatalogException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String RESTAURANT_CLOSED = "RESTAURANT_CLOSED";
    public static final String ITEM_UNAVAILABLE = "ITEM_UNAVAILABLE";
    public static final String ITEM_NOT_ON_MENU = "ITEM_NOT_ON_MENU";
    public static final String NO_SUCH_ITEM = "NO_SUCH_ITEM";

    public CatalogException(String code, String message) {
        super(code, message);
    }
}
