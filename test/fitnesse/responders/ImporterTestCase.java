package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.After;
import org.junit.Before;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/11/11
 * Time: 8:55 PM
 */
public class ImporterTestCase extends FitnesseBaseTestCase {

    public WikiPage pageOne;
    public FitNesseContext remoteContext;
    public WikiPage remoteRoot;
    public FitNesseContext localContext;
    public WikiPage localRoot;
    public WikiPage childPageOne;
    public WikiPage pageTwo;

    @Before
    public void beforeImportTest() throws Exception {
        remoteContext = new FitNesseContext("RooT");
        remoteRoot = remoteContext.root;
        PageCrawler crawler = remoteRoot.getPageCrawler();
        crawler.addPage(remoteRoot, PathParser.parse("PageOne"), "page one");
        crawler.addPage(remoteRoot, PathParser.parse("PageOne.ChildOne"), "child one");
        crawler.addPage(remoteRoot, PathParser.parse("PageTwo"), "page two");

        localContext = new FitNesseContext("RooT2");
        localRoot = localContext.root;
        pageOne = localRoot.addChildPage("PageOne");
        childPageOne = pageOne.addChildPage("ChildOne");
        pageTwo = localRoot.addChildPage("PageTwo");
    }

    @After
    public void afterImportTest() {

    }

}
