package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static util.RegexAssertions.assertSubString;

public class AddChildPageResponderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private PageData childPageData;
    private PageCrawler crawler;
    private String childName;
    private MockRequest request;
    private FitNesseContext context;
    private Responder responder;
    private WikiPagePath path;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory,@Named(FitNesseContextModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        crawler.addPage(root, PathParser.parse("TestPage"));
        childName = "ChildPage";
        String childContent = "child content";
        String pagetype = "";
        request = new MockRequest();
        request.setResource("TestPage");
        request.addInput("name", childName);
        request.addInput("content", childContent);
        request.addInput("pageType", pagetype);
        responder = new AddChildPageResponder(htmlPageFactory);
        path = PathParser.parse("TestPage.ChildPage");
    }

    @Test
    public void canGetRedirectResponse() throws Exception {
        final SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        final String body = response.getContent();
        assertEquals("", body);
        assertEquals(response.getStatus(), 303);

    }

    @Test
    public void childPageIsMade() throws Exception {
        assertTrue(crawler.getPage(root, path) == null);
        responder.makeResponse(context, request);
        assertTrue(crawler.getPage(root, path) != null);
    }

    @Test
    public void noPageIsMadeIfNameIsNull() throws Exception {
        request.addInput("name", "");
        assertTrue(crawler.getPage(root, path) == null);
        responder.makeResponse(context, request);
        assertTrue(crawler.getPage(root, path) == null);
    }

    @Test
    public void givesAInvalidNameErrorForAInvalidName() throws Exception {
        request = makeInvalidRequest("");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals(400, response.getStatus());
        assertSubString("Invalid Child Name", response.getContent());

        request = makeInvalidRequest("hello goodbye");
        response = (SimpleResponse) responder.makeResponse(context, request);
        assertSubString("Invalid Child Name", response.getContent());

        request = makeInvalidRequest("1man1mission");
        response = (SimpleResponse) responder.makeResponse(context, request);
        assertSubString("Invalid Child Name", response.getContent());

        request = makeInvalidRequest("PageOne.PageTwo");
        response = (SimpleResponse) responder.makeResponse(context, request);
        assertSubString("Invalid Child Name", response.getContent());
    }

    private MockRequest makeInvalidRequest(String name) {
        MockRequest request = new MockRequest();
        request.setResource("TestPage");
        request.addInput("name", name);
        request.addInput("content", "hello");
        request.addInput("pageType", "");
        return request;
    }

    @Test
    public void withDefaultPageTypeAndPageNameForNormalThenNoAttributeShouldBeSet() throws Exception {
        request.addInput("name", "NormalPage");
        responder.makeResponse(context, request);
        getChildPage("NormalPage");
        assertFalse(isSuite());
        assertFalse(isTest());
    }

    @Test
    public void withDefaultPageTypeAndPageNameForTestTheTestAttributeShouldBeSet() throws Exception {
        request.addInput("name", "TestPage");
        responder.makeResponse(context, request);
        getChildPage("TestPage");
        assertFalse(isSuite());
        assertTrue(isTest());
    }

    @Test
    public void withDefaultPageTypeAndPageNameForSuiteTheSuiteAttributeShouldBeSet() throws Exception {
        request.addInput("name", "SuitePage");
        responder.makeResponse(context, request);
        getChildPage("SuitePage");
        assertTrue(isSuite());
        assertFalse(isTest());
    }

    private boolean isSuite() {
        return childPageData.hasAttribute("Suite");
    }

    @Test
    public void correctAttributeWhenNameHasTestButAttributeIsNormal() throws Exception {
        request.addInput("name", "TestChildPage");
        request.addInput("pageType", "Normal");
        responder.makeResponse(context, request);
        getChildPage("TestChildPage");
        assertFalse(isTest());
        assertFalse(isSuite());
    }

    @Test
    public void pageTypeShouldBeTestWhenAttributeIsTest() throws Exception {
        request.addInput("pageType", "Test");
        responder.makeResponse(context, request);
        getChildPage(childName);
        assertTrue(isTest());
        assertFalse(isSuite());
    }

    @Test
    public void pageTypeShouldBeSuiteWhenAttributeIsSuite() throws Exception {
        request.addInput("pageType", "Suite");
        responder.makeResponse(context, request);
        getChildPage(childName);
        assertFalse(isTest());
        assertTrue(isSuite());
    }

    private boolean isTest() {
        return childPageData.hasAttribute("Test");
    }


    private void getChildPage(String childName) throws Exception {
        WikiPage childPage = crawler.getPage(root, PathParser.parse("TestPage." + childName));
        childPageData = childPage.getData();
    }
}