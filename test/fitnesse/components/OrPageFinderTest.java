package fitnesse.components;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class OrPageFinderTest extends CompositePageFinderTestCase {

    @Test
    public void singlePageFinder() throws Exception {
        sut = new OrPageFinder();
        setupMockWithEmptyReturnValue();

        sut.add(delegate);

        sut.search(page);

        verify(delegate, times(1)).search(page);
    }

    @Test
    public void multiplePageFinder() throws Exception {
        sut = new OrPageFinder();
        setupMockWithEmptyReturnValue();

        sut.add(delegate);
        sut.add(delegate);
        sut.add(delegate);

        sut.search(page);

        verify(delegate, times(3)).search(page);
    }

    @Test
    public void combinationIsFound() throws Exception {
        sut = new OrPageFinder();
        List<WikiPage> expected = setupWikiPageList(pageOne, pageTwo, pageThree);

        when(delegate.search(any(WikiPage.class))).thenReturn(expected);

        PageFinder delegate2 = mock(PageFinder.class);
        when(delegate2.search(any(WikiPage.class))).thenReturn(setupWikiPageList(pageOne, pageTwo));

        sut.add(delegate);
        sut.add(delegate2);

        List<WikiPage> results = sut.search(page);

        assertFoundResultsEqualsExpectation(expected, results);
    }

    @Test
    public void multpleIntersections() throws Exception {
        sut = new OrPageFinder();
        List<WikiPage> expected = setupWikiPageList(pageOne, pageTwo, pageThree);

        when(delegate.search(any(WikiPage.class))).thenReturn(
                setupWikiPageList(pageOne, pageTwo));

        PageFinder delegate2 = mock(PageFinder.class);
        when(delegate2.search(any(WikiPage.class))).thenReturn(setupWikiPageList(pageThree));

        sut.add(delegate);
        sut.add(delegate2);
        sut.add(delegate);

        List<WikiPage> results = sut.search(page);

        assertFoundResultsEqualsExpectation(expected, results);
    }
}
