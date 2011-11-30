package fitnesse.responders.refactoring;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.components.ContentReplacingSearchObserver;
import fitnesse.components.PageFinder;
import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.SearchObserver;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class SearchReplaceResponder extends ResultResponder {

    private SearchObserver observer;

    @Inject
    public SearchReplaceResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, RunningTestingTracker runningTestingTracker, @Named(FitNesseModule.ENABLE_CHUNKING) boolean chunkingEnabled) {
        super(htmlPageFactory, root, runningTestingTracker, chunkingEnabled);
    }

    protected String getTitle() {
        return String.format("Replacing matching content \"%s\" with content \"%s\"",
                getSearchString(), getReplacementString());
    }

    private String getReplacementString() {
        return (String) request.getInput("replacementString");
    }

    private String getSearchString() {
        return (String) request.getInput("searchString");
    }

    public void hit(WikiPage page) throws IOException {
        observer.hit(page);
        super.hit(page);
    }

    protected void startSearching(WikiPage root, WikiPage page) throws IOException {
        super.startSearching(root, page);
        String searchString = getSearchString();
        String replacementString = getReplacementString();

        observer = new ContentReplacingSearchObserver(searchString, replacementString);
        PageFinder finder = new RegularExpressionWikiPageFinder(searchString, this);
        finder.search(page);
    }

}
