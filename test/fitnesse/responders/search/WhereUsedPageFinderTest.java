package fitnesse.responders.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.components.SearchObserver;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class WhereUsedPageFinderTest extends FitnesseBaseTestCase implements SearchObserver {
    private WikiPage root;
    private InMemoryPage pageOne;
    private WikiPage pageTwo;
    private WikiPage pageThree;
    private WhereUsedPageFinder whereUsed;

    private List<WikiPage> hits = new ArrayList<WikiPage>();
    private PageCrawler crawler;

    public void hit(WikiPage page) {
        hits.add(page);
    }

    @Inject
    public void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        pageOne = (InMemoryPage) crawler.addPage(root, PathParser.parse("PageOne"), "this is page one ^ChildPage");
        pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "I am Page Two my brother is PageOne . SomeMissingPage");
        pageThree = crawler.addPage(root, PathParser.parse("PageThree"), "This is !-PageThree-!, I Have \n!include PageTwo");
        crawler.addPage(pageTwo, PathParser.parse("ChildPage"), "I will be a virtual page to .PageOne ");

        whereUsed = new WhereUsedPageFinder(root, this);

        hits.clear();
    }

    @Test
    public void testFindReferencingPages() throws Exception {
        whereUsed = new WhereUsedPageFinder(pageOne, this);
        List<WikiPage> resultList = whereUsed.search(root);
        assertEquals(2, resultList.size());
        assertEquals(pageTwo, resultList.get(0));

        whereUsed = new WhereUsedPageFinder(pageTwo, this);
        resultList = whereUsed.search(root);
        assertEquals(1, resultList.size());

        whereUsed = new WhereUsedPageFinder(pageThree, this);
        resultList = whereUsed.search(root);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testObserving() throws Exception {
        whereUsed = new WhereUsedPageFinder(pageOne, this);
        whereUsed.search(root);
        assertEquals(2, hits.size());
    }

    @Test
    public void testOnlyOneReferencePerPage() throws Exception {
        whereUsed = new WhereUsedPageFinder(pageThree, this);
        WikiPage newPage = crawler.addPage(root, PathParser.parse("NewPage"), "one reference to PageThree.  Two reference to PageThree");
        List<WikiPage> resultList = whereUsed.search(root);
        assertEquals(1, resultList.size());
        assertEquals(newPage, resultList.get(0));
    }

    @Test
    public void testWordsNotFoundInPreprocessedText() throws Exception {
        crawler.addPage(root, PathParser.parse("NewPage"), "{{{ PageThree }}}");
        List<WikiPage> resultList = whereUsed.search(pageThree);
        assertEquals(0, resultList.size());
    }

    @Test
    public void testDontLookForReferencesInVirtualPages() throws Exception {
        FitNesseUtil.bindVirtualLinkToPage(pageOne, pageTwo);
        whereUsed = new WhereUsedPageFinder(pageOne, this);
        whereUsed.search(pageOne);
        assertEquals(0, hits.size());
    }

}
