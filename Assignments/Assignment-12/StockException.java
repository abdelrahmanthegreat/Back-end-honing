package masr;

import java.io.Serial;

public class StockException extends PlatformException {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final String OUT_OF_STOCK = "OUT_OF_STOCK";
    public static final String INVALID_STOCK = "INVALID_STOCK";

    public StockException(String code, String message) {
        super(code, message);
    }
}
