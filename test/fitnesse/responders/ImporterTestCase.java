package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/11/11
 * Time: 8:55 PM
 */
public class ImporterTestCase extends FitnesseBaseTestCase {

    protected FitNesseUtil fitNesseUtil = new FitNesseUtil();
    public WikiPage pageOne;
    public FitNesseContext remoteContext;
    public WikiPage remoteRoot;
    public FitNesseContext localContext;
    public WikiPage localRoot;
    public WikiPage childPageOne;
    public WikiPage pageTwo;

    @Override
    protected Properties getFitNesseProperties() {
        Properties properties = super.getFitNesseProperties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
    }

    @Before
    public void beforeImportTest() throws Exception {
        remoteContext = FitNesseContext.makeContext(injector, FitNesseContext.DEFAULT_PATH, "RooT");
        remoteRoot = remoteContext.root;
        PageCrawler crawler = remoteRoot.getPageCrawler();
        crawler.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
        crawler.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
        crawler.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");

        localContext = FitNesseContext.makeContext(injector, getRootPath(), "local");
        localRoot = localContext.root;
        pageOne = localRoot.addChildPage("PageOne");
        childPageOne = pageOne.addChildPage("ChildOne");
        pageTwo = localRoot.addChildPage("PageTwo");
    }

    @After
    public void afterImportTest() {

    }

}
