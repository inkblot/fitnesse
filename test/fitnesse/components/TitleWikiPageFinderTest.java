package fitnesse.components;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class TitleWikiPageFinderTest extends FitnesseBaseTestCase implements SearchObserver {
    WikiPage root;

    private List<WikiPage> hits = new ArrayList<WikiPage>();

    public void hit(WikiPage page) throws Exception {
        hits.add(page);
    }

    @Before
    public void setUp() throws Exception {
        root = InMemoryPage.makeRoot("RooT", injector);
        PageCrawler crawler = root.getPageCrawler();
        crawler.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
        crawler.addPage(root, PathParser.parse("PageOne.PageOneChild"), "PageChild is a child of PageOne");
        WikiPage pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "PageTwo has a bit of content too\n^PageOneChild");
        PageData data = pageTwo.getData();
        data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, FitNesseUtil.URL + "PageOne");
        pageTwo.commit(data);
        hits.clear();
    }

    @Test
    public void titleSearch() throws Exception {
        TitleWikiPageFinder searcher = new TitleWikiPageFinder("one", this);
        hits.clear();
        searcher.search(root);
        assertPagesFound("PageOne", "PageOneChild");
    }

    private void assertPagesFound(String... pageNames) throws Exception {
        assertEquals(pageNames.length, hits.size());

        List<String> pageNameList = Arrays.asList(pageNames);
        for (WikiPage page : hits) {
            assertTrue(pageNameList.contains(page.getName()));
        }
    }

}
