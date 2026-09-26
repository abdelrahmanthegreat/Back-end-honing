package masr;

import java.io.Serial;

public abstract class PlatformException extends Exception {

    @Serial
    private static final long serialVersionUID = 1L;

    private final String code;

    protected PlatformException(String code, String message) {
        super(message);
        this.code = code;
    }

    protected PlatformException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public String code() {
        return code;
    }

    protected String prefix() {
        return getClass().getSimpleName();
    }

    @Override
    public String getMessage() {
        return "[" + prefix() + "/" + code + "] " + super.getMessage();
    }
}
