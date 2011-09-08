package util;

import com.google.inject.AbstractModule;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/8/11
 * Time: 7:04 AM
 */
public class UtilModule extends AbstractModule {
    @Override
    protected void configure() {
        requestStaticInjection(ClockUtil.class);
    }
}
