package fitnesse.components;

import fitnesse.wiki.WikiPage;
import org.junit.Test;

import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AndPageFinderTest extends CompositePageFinderTestCase {

    @Test
    public void singlePageFinder() throws Exception {
        sut = new AndPageFinder();
        setupMockWithEmptyReturnValue();

        sut.add(delegate);

        sut.search(page);

        verify(delegate, times(1)).search(page);
    }

    @Test
    public void multiplePageFinder() throws Exception {
        sut = new AndPageFinder();
        setupMockWithEmptyReturnValue();

        sut.add(delegate);
        sut.add(delegate);
        sut.add(delegate);

        sut.search(page);

        verify(delegate, times(3)).search(page);
    }

    @Test
    public void intersectionIsFound() throws Exception {
        sut = new AndPageFinder();
        List<WikiPage> expected = setupWikiPageList(pageOne, pageTwo);

        when(delegate.search(any(WikiPage.class))).thenReturn(
                setupWikiPageList(pageOne, pageTwo, pageThree));

        PageFinder delegate2 = mock(PageFinder.class);
        when(delegate2.search(any(WikiPage.class))).thenReturn(expected);

        sut.add(delegate);
        sut.add(delegate2);

        List<WikiPage> results = sut.search(page);

        assertFoundResultsEqualsExpectation(expected, results);
    }

    @Test
    public void multpleIntersections() throws Exception {
        sut = new AndPageFinder();
        List<WikiPage> expected = setupWikiPageList(pageOne, pageTwo);

        when(delegate.search(any(WikiPage.class))).thenReturn(
                setupWikiPageList(pageOne, pageTwo, pageThree));

        PageFinder delegate2 = mock(PageFinder.class);
        when(delegate2.search(any(WikiPage.class))).thenReturn(expected);

        sut.add(delegate);
        sut.add(delegate2);
        sut.add(delegate);

        List<WikiPage> results = sut.search(page);

        assertFoundResultsEqualsExpectation(expected, results);
    }
}
