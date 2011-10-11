package util;

/**
 * TODO: replace occurrences of this exception with a more appropriate error type
 * User: inkblot
 * Date: 9/10/11
 * Time: 8:16 AM
 */
public class TodoException extends RuntimeException {
    public TodoException(Throwable cause) {
        super("TODO: figure out how to handle this exception", cause);
    }
}
