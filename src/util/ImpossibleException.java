package util;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/4/11
 * Time: 3:23 PM
 */
public class ImpossibleException extends RuntimeException {
    public ImpossibleException(String message) {
        super(message);
    }

    public ImpossibleException(String message, Throwable cause) {
        super(message, cause);
    }
}
