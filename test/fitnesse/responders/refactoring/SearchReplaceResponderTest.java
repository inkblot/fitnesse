package fitnesse.responders.refactoring;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.assertThat;
import static org.junit.internal.matchers.StringContains.containsString;

public class SearchReplaceResponderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private PageCrawler crawler;
    private SearchReplaceResponder responder;
    private MockRequest request;
    private RunningTestingTracker runningTestingTracker;
    private WikiPagePath pagePath;
    private WikiPage somePage;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, RunningTestingTracker runningTestingTracker) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.runningTestingTracker = runningTestingTracker;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        pagePath = PathParser.parse("SomePage");
        somePage = crawler.addPage(root, pagePath, "has something in it");
        responder = new SearchReplaceResponder(htmlPageFactory, root, runningTestingTracker, isChunkingEnabled());
        request = new MockRequest();
        request.setResource("SomePage");
    }

    @Test
    public void testSingleReplacementHtml() throws Exception {
        String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");

        assertThat(content, containsString("replacedthing"));
        assertThat(content, containsString("SomePage"));
    }

    @Test
    public void multipleReplacements() throws Exception {
        crawler.addPage(somePage, PathParser.parse("ChildPage"), "this page has something too.");
        String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");
        assertThat(content, containsString("SomePage"));
        assertThat(content, containsString("ChildPage"));
    }

    @Test
    public void onlyReplacedPagesAreListed() throws Exception {
        crawler.addPage(somePage, PathParser.parse("ChildPage"), "this page has nothing to replace.");
        String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");
        assertThat(content, containsString("SomePage"));
        assertThat(content, not(containsString("ChildPage")));
    }

    @Test
    public void testReplacement() throws Exception {
        getResponseContentUsingSearchReplaceString("something", "replacedthing");
        WikiPage page = crawler.getPage(root, pagePath);
        assertThat(page.getData().getContent(), containsString("has replacedthing in it"));
    }

    @Test
    public void noPageMatched() throws Exception {
        String content = getResponseContentUsingSearchReplaceString("non-available text", "replaced");

        assertThat(content, containsString("No pages matched your search criteria."));
    }

    @Test
    public void onlySelectedPageAndChildrenAreSearched() throws Exception {
        request.setResource("SomePage.ChildPage");
        crawler.addPage(somePage, PathParser.parse("ChildPage"), "this page has something to replace.");
        String content = getResponseContentUsingSearchReplaceString("something", "replacedthing");
        assertThat(content, not(containsString("<a href=\"SomePage\">")));
        assertThat(content, containsString("<a href=\"SomePage.ChildPage\">"));
    }

    private String getResponseContentUsingSearchReplaceString(String searchString, String replacementString) throws Exception {
        request.addInput("searchString", searchString);
        request.addInput("replacementString", replacementString);
        Response response = responder.makeResponse(request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        return sender.sentData();
    }

}
