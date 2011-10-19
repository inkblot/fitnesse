package fitnesse.components;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNeseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class CompositePageFinderTestCase extends FitnesseBaseTestCase {

    protected PageFinder delegate;
    protected CompositePageFinder sut;
    protected WikiPage root;
    PageCrawler crawler;
    protected WikiPage pageOne;
    protected WikiPage pageTwo;
    protected WikiPage pageThree;

    @Inject
    public void inject(@Named(FitNeseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void init() throws Exception {
        delegate = mock(PageFinder.class);
        crawler = root.getPageCrawler();
        pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "this is page one ^ChildPage");
        pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne . SomeMissingPage");
        pageThree = crawler.addPage(root, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
        crawler.addPage(pageTwo, PathParser.parse("ChildPage"), "I will be a virtual page to .PageOne ");
    }

    protected void setupMockWithEmptyReturnValue() throws Exception {
        when(delegate.search(any(WikiPage.class))).thenReturn(
                new ArrayList<WikiPage>());
    }

    protected List<WikiPage> setupWikiPageList(WikiPage... pages) {
        return new ArrayList<WikiPage>(Arrays.asList(pages));
    }

    protected void assertFoundResultsEqualsExpectation(List<WikiPage> expected2, List<WikiPage> results) {
        assertEquals(expected2.size(), results.size());
        assertTrue(results.containsAll(expected2));
    }

}