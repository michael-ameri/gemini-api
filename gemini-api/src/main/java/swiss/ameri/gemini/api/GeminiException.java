package swiss.ameri.gemini.api;

import java.util.OptionalInt;

/**
 * Thrown if we get an unexpected body or response code from Gemini API.
 */
public class GeminiException extends RuntimeException {

    private final Integer code;


    /**
     * Create a new exception.
     *
     * @param message of the exception
     */
    public GeminiException(String message) {
        this(message, null, null);
    }

    /**
     * Create a new exception.
     *
     * @param message of the exception
     * @param code    optional http response code
     */
    public GeminiException(String message, int code) {
        this(message, code, null);
    }

    /**
     * Create a new exception.
     *
     * @param message of the exception
     * @param cause   the cause of the exception
     */
    public GeminiException(String message, Throwable cause) {
        this(message, null, cause);
    }

    /**
     * Create a new exception.
     *
     * @param message of the exception
     * @param code    optional http response code
     * @param cause   the cause of the exception
     */
    public GeminiException(String message, Integer code, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    /**
     * Get the optional http response code.
     *
     * @return the response code, if present
     */
    public OptionalInt getCode() {
        return code == null ? OptionalInt.empty() : OptionalInt.of(code);
    }

}
