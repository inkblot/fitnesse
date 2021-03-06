package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertSame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PageListSetUpTearDownSurrounderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private WikiPage suite;
    private WikiPage testPage;
    private PageCrawler crawler;

    private PageListSetUpTearDownSurrounder surrounder;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        PageData data = root.getData();
        root.commit(data);
        suite = crawler.addPage(root, PathParser.parse("SuitePageName"), "The is the test suite\n");
        testPage = crawler.addPage(suite, PathParser.parse("TestPage"), "My test and has some content");
        surrounder = new PageListSetUpTearDownSurrounder(root);
    }

    @Test
    public void testPagesForTestSystemAreSurroundedByRespectiveSuiteSetupAndTeardown() throws Exception {
        WikiPage slimPage = crawler.addPage(testPage, PathParser.parse("SlimPageTest"));
        WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"));
        WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"));
        WikiPage setUp2 = crawler.addPage(slimPage, PathParser.parse("SuiteSetUp"));
        WikiPage tearDown2 = crawler.addPage(slimPage, PathParser.parse("SuiteTearDown"));

        SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
        List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();
        surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(testPages);

        assertEquals(6, testPages.size());

        int testPageIndex = testPages.indexOf(testPage);
        assertTrue(testPageIndex != -1);
        assertSame(setUp, testPages.get(testPageIndex - 1));
        assertSame(tearDown, testPages.get(testPageIndex + 1));

        int slimPageIndex = testPages.indexOf(slimPage);
        assertTrue(slimPageIndex != -1);
        assertSame(setUp2, testPages.get(slimPageIndex - 1));
        assertSame(tearDown2, testPages.get(slimPageIndex + 1));
    }

    @Test
    public void testSetUpAndTearDown() throws Exception {
        WikiPage setUp = crawler.addPage(root, PathParser.parse("SuiteSetUp"), "suite set up");
        WikiPage tearDown = crawler.addPage(root, PathParser.parse("SuiteTearDown"), "suite tear down");

        SuiteContentsFinder finder = new SuiteContentsFinder(suite, null, root);
        List<WikiPage> testPages = finder.getAllPagesToRunForThisSuite();
        surrounder.surroundGroupsOfTestPagesWithRespectiveSetUpAndTearDowns(testPages);
        assertEquals(3, testPages.size());
        assertSame(setUp, testPages.get(0));
        assertSame(tearDown, testPages.get(2));
    }
}
