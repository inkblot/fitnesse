package fitnesse;

import com.google.inject.*;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPageFactory;
import org.junit.After;
import org.junit.Before;
import util.Clock;
import util.ClockUtil;

import java.util.Properties;

import static com.google.inject.util.Modules.override;
import static util.FileUtil.deleteFileSystemDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/22/11
 * Time: 10:24 PM
 */
public class FitnesseBaseTestCase extends BaseInjectedTestCase {

    public static final int DEFAULT_PORT = 1999;

    private Injector contextInjector;

    protected final FitNesseContext getContext() {
        return getContextInjector().getInstance(FitNesseContext.class);
    }

    protected final Injector getContextInjector() {
        if (contextInjector == null) {
            contextInjector = Guice.createInjector(override(
                    new FitNesseModule(getFitNesseProperties(), getUserpass()),
                    new FitNesseContextModule(getFitNesseProperties(), getUserpass(), getRootPath(), "RooT", DEFAULT_PORT, true))
                    .with(getOverrideModule()));
        }
        return contextInjector;
    }

    protected final String getRootPath() {
        return TestCaseHelper.getRootPath(getClass().getSimpleName());
    }

    protected final Module[] getBaseModules() {
        return new Module[]{new FitNesseModule(getFitNesseProperties(), getUserpass())};
    }

    protected String getUserpass() {
        return null;
    }

    protected Properties getFitNesseProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
    }

    @Before
    public final void beforeAllFitNesseTests() {
        inject();
    }

    @After
    public final void afterAllFitNesseTests() {
        deleteFileSystemDirectory(getRootPath());
        contextInjector = null;
        inject(NOOP_MODULE);
        ClockUtil.inject((Clock) null);
    }

}
