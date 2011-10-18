package fitnesse.responders;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import fitnesse.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;

import java.util.Properties;

import static com.google.inject.util.Modules.override;
import static util.FileUtil.deleteFileSystemDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/11/11
 * Time: 8:55 PM
 */
public class ImporterTestCase {

    private final String TEST_ROOT_PATH = TestCaseHelper.getRootPath(getClass().getSimpleName());

    protected FitNesseUtil fitNesseUtil = new FitNesseUtil();
    protected WikiPage pageOne;
    protected Injector remoteInjector;
    protected FitNesseContext remoteContext;
    protected WikiPage remoteRoot;
    protected FitNesseContext localContext;
    protected WikiPage localRoot;
    protected WikiPage childPageOne;
    protected WikiPage pageTwo;
    protected Injector localInjector;

    protected Properties getFitNesseProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
    }

    @Before
    public void beforeImportTest() throws Exception {
        Injector injector = Guice.createInjector(override(new FitNesseModule(getFitNesseProperties(), null)).with(getOverrideModule()));

        remoteInjector = FitNesseContextModule.makeContext(injector, getFitNesseProperties(), null, TEST_ROOT_PATH + "/remote", "RooT", 1999, true);
        remoteContext = remoteInjector.getInstance(FitNesseContext.class);
        remoteRoot = remoteContext.root;
        PageCrawler crawler = remoteRoot.getPageCrawler();
        crawler.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
        crawler.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
        crawler.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");

        localInjector = FitNesseContextModule.makeContext(injector, getFitNesseProperties(), null, TEST_ROOT_PATH + "/local", "local", FitNesseContext.DEFAULT_PORT, true);
        localContext = localInjector.getInstance(FitNesseContext.class);
        localRoot = localContext.root;
        pageOne = localRoot.addChildPage("PageOne");
        childPageOne = pageOne.addChildPage("ChildOne");
        pageTwo = localRoot.addChildPage("PageTwo");
    }

    @After
    public void afterImportTest() {
        deleteFileSystemDirectory(TEST_ROOT_PATH);
    }

    protected Module getOverrideModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
            }
        };
    }
}
