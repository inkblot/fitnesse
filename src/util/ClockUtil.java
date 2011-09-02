package util;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 10:50 PM
 */
public class ClockUtil {
    private static final Logger logger = LoggerFactory.getLogger(ClockUtil.class);
    protected static final SystemClock SYSTEM_CLOCK = new SystemClock();
    protected static Provider<Clock> instance;

    public static Clock getInstance() {
        if (instance == null) {
            NullPointerException npe = new NullPointerException("instance");
            logger.error("Global clock is null", npe);
            throw npe;
        }
        return instance.get();
    }

    public static long currentTimeInMillis() {
        return instance.get().currentClockTimeInMillis();
    }

    public static Date currentDate() {
        return instance.get().currentClockDate();
    }

    public static void restoreDefaultClock() {
        throw new UnsupportedOperationException("AAAAAAIEEEEEEEEEEEEEEEEEEEEE!");
    }

    @Inject
    public static void inject(Provider<Clock> clockProvider) {
        instance = clockProvider;
    }

    public static void inject(final Clock clock) {
        inject(new Provider<Clock>() {
            @Override
            public Clock get() {
                return clock;
            }
        });
    }
}
