package masr;

import java.io.Serial;

public class UncheckedPlatformException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final PlatformException platformCause;

    public UncheckedPlatformException(PlatformException cause) {
        super(cause.getMessage(), cause);
        this.platformCause = cause;
    }

    public PlatformException platformCause() {
        return platformCause;
    }

    public static RuntimeException wrap(PlatformException cause) {
        return new UncheckedPlatformException(cause);
    }

    public static PlatformException unwrap(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof PlatformException platformException) {
                return platformException;
            }
            if (current instanceof UncheckedPlatformException unchecked) {
                return unchecked.platformCause();
            }
            current = current.getCause();
        }
        return null;
    }
}
