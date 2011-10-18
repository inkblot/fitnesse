package fitnesse;

import com.google.inject.*;
import com.google.inject.name.Names;
import fitnesse.responders.files.SampleFileUtility;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import org.junit.After;
import org.junit.Before;
import util.Clock;
import util.ClockUtil;

import java.io.File;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static util.FileUtil.deleteFileSystemDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/22/11
 * Time: 10:24 PM
 */
public class FitnesseBaseTestCase extends BaseInjectedTestCase {

    public static final int DEFAULT_PORT = 1999;

    private FitNesseContext context;
    protected SampleFileUtility samples;

    protected final FitNesseContext makeContext() throws Exception {
        return makeContext(DEFAULT_PORT);
    }

    protected final FitNesseContext makeContext(int port) throws Exception {
        return makeContext(InMemoryPage.class, port);
    }

    protected final FitNesseContext makeContext(Class<? extends WikiPage> wikiPageClass) throws Exception {
        return makeContext(wikiPageClass, DEFAULT_PORT);
    }

    protected final FitNesseContext makeContext(Class<? extends WikiPage> wikiPageClass, int port) throws Exception {
        assertNull(context);
        context = FitNesseContextModule.makeContext(injector, getRootPath(), "RooT", port, true).getInstance(FitNesseContext.class);
        assertThat(context.root, instanceOf(wikiPageClass));
        return context;
    }

    protected final String getRootPath() {
        File rootPath = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName());
        assertTrue(rootPath.exists() || rootPath.mkdirs());
        return rootPath.getAbsolutePath();
    }

    protected final String getRootPagePath() {
        assertNotNull("A context must have already been made", context);
        return context.getInjector().getInstance(Key.get(String.class, Names.named(FitNesseContextModule.ROOT_PAGE_PATH)));
    }

    protected final void makeSampleFiles() {
        assertNotNull("A context must have already been made", context);
        samples = new SampleFileUtility(getRootPagePath());
        samples.makeSampleFiles();
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
        context = null;
        samples = null;
        inject(NOOP_MODULE);
        ClockUtil.inject((Clock) null);
    }

}
