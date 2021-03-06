package fitnesse.components;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.junit.internal.matchers.TypeSafeMatcher;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertThat;

public class RegularExpressionWikiPageFinderTest extends FitnesseBaseTestCase implements SearchObserver {

    private WikiPage root;
    private WikiPage pageOne;
    private WikiPage childPage;
    private WikiPage pageTwo;

    List<WikiPage> foundPages = new ArrayList<WikiPage>();
    private WikiPageFinder pageFinder;

    public void hit(WikiPage page) {
        foundPages.add(page);
    }

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        PageCrawler crawler = root.getPageCrawler();
        pageOne = crawler.addPage(root, PathParser.parse("PageOne"), "has PageOne content");
        childPage = crawler.addPage(root, PathParser.parse("PageOne.PageOneChild"),
                "PageChild is a child of PageOne");
        pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"),
                "PageTwo has a bit of content too\n^PageOneChild");
        foundPages.clear();
    }

    @Test
    public void searcher() throws Exception {
        pageFinder = pageFinder("has");
        pageFinder.search(root);
        assertThat(foundPages, found(pageOne, pageTwo));
    }

    @Test
    public void searcherAgain() throws Exception {
        pageFinder = pageFinder("a");
        pageFinder.search(root);
        assertThat(foundPages, found(pageOne, childPage, pageTwo));
    }

    @Test
    public void dontSearchProxyPages() throws Exception {
        pageFinder = pageFinder("a");
        pageFinder.search(pageTwo);
        assertEquals(1, foundPages.size());
    }

    @Test
    public void observing() throws Exception {
        pageFinder = pageFinder("has");
        pageFinder.search(root);
        assertEquals(2, foundPages.size());
    }

    @Test
    public void pagesNotMatching() throws Exception {
        pageFinder = pageFinder(notMatchingSearchText());

        pageFinder.search(root);

        assertThat(foundPages, isEmpty());
    }

    @Test
    public void singlePageMatches() throws Exception {
        pageFinder = pageFinder(matchTextForPageOne());

        pageFinder.search(root);

        assertThat(foundPages, found(pageOne));
    }

    @Test
    public void multiplePageMatch() throws Exception {
        pageFinder = pageFinder(matchAll());

        pageFinder.search(root);

        assertThat(foundPages, found(root, pageOne, childPage, pageTwo));
    }

    @Test
    public void matchesSublevels() throws Exception {
        pageFinder = pageFinder(matchAll());

        pageFinder.search(pageOne);

        assertThat(foundPages, found(pageOne, childPage));
    }

    private String matchAll() {
        return ".*";
    }

    private String matchTextForPageOne() {
        return "PageOne content";
    }

    private String notMatchingSearchText() {
        return "this search text does not match any page";
    }

    private WikiPageFinder pageFinder(String searchText) {
        return new RegularExpressionWikiPageFinder(searchText, this);
    }

    private Matcher<List<WikiPage>> found(final WikiPage... pages) {
        return new TypeSafeMatcher<List<WikiPage>>() {

            public boolean matchesSafely(List<WikiPage> foundPages) {
                if (foundPages.size() != pages.length) return false;

                for (WikiPage expectedPage : pages) {
                    if (!foundPages.contains(expectedPage)) return false;
                }
                return true;
            }

            public void describeTo(Description description) {
                description.appendText("a list containing ").appendValue(pages);
            }
        };
    }

    private Matcher<List<WikiPage>> isEmpty() {
        return new EmptyListMatcher();
    }

    private static class EmptyListMatcher extends TypeSafeMatcher<List<WikiPage>> {
        public boolean matchesSafely(List<WikiPage> pages) {
            return pages.isEmpty();
        }

        public void describeTo(Description description) {
            description.appendText("an empty list");
        }
    }
}
