package fitnesse.responders.refactoring;

import com.google.inject.Inject;
import fitnesse.components.ContentReplacingSearchObserver;
import fitnesse.components.PageFinder;
import fitnesse.components.RegularExpressionWikiPageFinder;
import fitnesse.components.SearchObserver;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.search.ResultResponder;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class SearchReplaceResponder extends ResultResponder {

    private SearchObserver observer;

    @Inject
    public SearchReplaceResponder(HtmlPageFactory htmlPageFactory) {
        super(htmlPageFactory);
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
