package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import fitnesse.slim.SlimService;
import util.ClockUtil;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 11:02 PM
 */
public class FitNesseModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(Boolean.class).annotatedWith(Names.named("inject")).toInstance(false);
        requestStaticInjection(SlimService.class);
        requestStaticInjection(ClockUtil.class);
    }
}
