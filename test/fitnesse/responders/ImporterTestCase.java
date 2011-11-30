package fitnesse.responders;

import com.google.inject.*;
import com.google.inject.name.Names;
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

    protected FitNesseUtil fitNesseUtil;
    protected WikiPage pageOne;
    protected Injector remoteInjector;
    protected WikiPage remoteRoot;
    protected WikiPage localRoot;
    protected WikiPage childPageOne;
    protected WikiPage pageTwo;
    protected Injector localInjector;

    protected Properties getFitNesseProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiModule.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
    }

    @Before
    public void beforeImportTest() throws Exception {
        remoteInjector = Guice.createInjector(override(
                new FitNesseModule(getFitNesseProperties(), null, TEST_ROOT_PATH + "/remote", "RooT", 1999, true))
                .with(getRemoteOverrides()));
        remoteRoot = remoteInjector.getInstance(Key.get(WikiPage.class, Names.named(WikiModule.ROOT_PAGE)));
        PageCrawler crawler = remoteRoot.getPageCrawler();
        crawler.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
        crawler.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
        crawler.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");

        localInjector = Guice.createInjector(
                new FitNesseModule(getFitNesseProperties(), null, TEST_ROOT_PATH + "/local", "local", FitNesseConstants.DEFAULT_PORT, true));
        localRoot = localInjector.getInstance(Key.get(WikiPage.class, Names.named(WikiModule.ROOT_PAGE)));
        pageOne = localRoot.addChildPage("PageOne");
        childPageOne = pageOne.addChildPage("ChildOne");
        pageTwo = localRoot.addChildPage("PageTwo");
    }

    @After
    public void afterImportTest() {
        deleteFileSystemDirectory(TEST_ROOT_PATH);
    }

    protected Module getRemoteOverrides() {
        return new AbstractModule() {
            @Override
            protected void configure() {
            }
        };
    }
}
