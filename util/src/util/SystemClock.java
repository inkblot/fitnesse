package util;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 11:06 PM
 */
public class SystemClock extends Clock {
    @Override
    public long currentClockTimeInMillis() {
        return System.currentTimeMillis();
    }
}
