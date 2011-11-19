package fitnesse.components;

import fitnesse.wiki.TraversalListener;
import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class WikiPageFinder implements TraversalListener, PageFinder {

    protected List<WikiPage> hits;
    protected SearchObserver observer;

    protected WikiPageFinder(SearchObserver observer) {
        this.observer = observer;
    }

    protected abstract boolean pageMatches(WikiPage page) throws IOException;

    public void processPage(WikiPage page) throws IOException {
        if (pageMatches(page)) {
            observer.hit(page);
        }
    }

    public List<WikiPage> search(WikiPage page) throws IOException {
        hits = new ArrayList<WikiPage>();
        page.getPageCrawler().traverse(page, this);
        return hits;
    }
}