package fitnesse;

import com.google.inject.*;
import com.google.inject.util.Modules;
import org.junit.Before;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/13/11
 * Time: 8:10 PM
 */
public abstract class BaseInjectedTestCase {
    protected static final AbstractModule NOOP_MODULE =
            new AbstractModule() {
                @Override
                protected void configure() {
                }
            };
    @Inject
    public Injector injector;

    protected abstract Module[] getBaseModules();

    protected Module getOverrideModule() {
        return NOOP_MODULE;
    }

    protected void inject() {
        inject(getOverrideModule());
    }

    protected final void inject(Module... testModules) {
        Guice.createInjector(
                Modules.override(getBaseModules())
                        .with(testModules))
                .injectMembers(this);
    }

    @Before
    public void beforeAll() {
        inject();
    }
}
