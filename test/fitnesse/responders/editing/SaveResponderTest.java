// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.components.SaveRecorder;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class SaveResponderTest extends SingleContextBaseTestCase {
    private Response response;
    public MockRequest request;
    public Responder responder;
    private PageCrawler crawler;

    private ContentFilter contentFilter;

    private WikiPage root;
    private FitNesseContext context;
    private Clock clock;

    @Inject
    public void inject(Clock clock, FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE) WikiPage root) {
        this.clock = clock;
        this.context = context;
        this.root = root;
    }

    @Override
    protected Module getOverrideModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(ContentFilter.class).toProvider(new Provider<ContentFilter>() {
                    @Override
                    public ContentFilter get() {
                        return contentFilter;
                    }
                });
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        contentFilter = new DefaultContentFilter();
        crawler = root.getPageCrawler();
        request = new MockRequest();
        responder = injector.getInstance(SaveResponder.class);
        SaveRecorder.clear();
    }

    @Test
    public void testResponse() throws Exception {
        crawler.addPage(root, PathParser.parse("ChildPage"));
        prepareRequest("ChildPage");

        Response response = responder.makeResponse(context, request);
        assertEquals(303, response.getStatus());
        assertHasRegexp("Location: ChildPage", response.makeHttpHeaders());

        String newContent = root.getChildPage("ChildPage").getData().getContent();
        assertEquals("some new content", newContent);

        checkRecentChanges(root, "ChildPage");
    }

    private void prepareRequest(String pageName) {
        request.setResource(pageName);
        request.addInput(EditResponder.TIME_STAMP, "12345");
        request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
        request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());
    }

    @Test
    public void testResponseWithRedirect() throws Exception {
        crawler.addPage(root, PathParser.parse("ChildPage"));
        prepareRequest("ChildPage");
        request.addInput("redirect", "http://fitnesse.org:8080/SomePage");

        Response response = responder.makeResponse(context, request);
        assertEquals(303, response.getStatus());
        assertHasRegexp("Location: http://fitnesse.org:8080/SomePage", response.makeHttpHeaders());
    }

    private void checkRecentChanges(WikiPage source, String changedPage) throws Exception {
        assertTrue("RecentChanges should exist", source.hasChildPage("RecentChanges"));
        String recentChanges = source.getChildPage("RecentChanges").getData().getContent();
        assertTrue("ChildPage should be in RecentChanges", recentChanges.contains(changedPage));
    }

    @Test
    public void testCanCreatePage() throws Exception {
        prepareRequest("ChildPageTwo");

        responder.makeResponse(context, request);

        assertEquals(true, root.hasChildPage("ChildPageTwo"));
        String newContent = root.getChildPage("ChildPageTwo").getData().getContent();
        assertEquals("some new content", newContent);
        assertTrue("RecentChanges should exist", root.hasChildPage("RecentChanges"));
        checkRecentChanges(root, "ChildPageTwo");
    }

    @Test
    public void testCanCreatePageWithoutTicketIdAndEditTime() throws Exception {
        request.setResource("ChildPageTwo");
        request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");

        responder.makeResponse(context, request);

        assertEquals(true, root.hasChildPage("ChildPageTwo"));
        String newContent = root.getChildPage("ChildPageTwo").getData().getContent();
        assertEquals("some new content", newContent);
        assertTrue("RecentChanges should exist", root.hasChildPage("RecentChanges"));
        checkRecentChanges(root, "ChildPageTwo");
    }

    @Test
    public void testKnowsWhenToMerge() throws Exception {
        String simplePageName = "SimplePageName";
        createAndSaveANewPage(simplePageName);

        request.setResource(simplePageName);
        request.addInput(EditResponder.CONTENT_INPUT_NAME, "some new content");
        request.addInput(EditResponder.TIME_STAMP, "" + (clock.currentClockTimeInMillis() - 10000));
        request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        assertHasRegexp("Merge", response.getContent());
    }

    @Test
    public void testKnowWhenNotToMerge() throws Exception {
        String pageName = "NewPage";
        createAndSaveANewPage(pageName);
        String newContent = "some new Content work damn you!";
        request.setResource(pageName);
        request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent);
        request.addInput(EditResponder.TIME_STAMP, "" + clock.currentClockTimeInMillis());
        request.addInput(EditResponder.TICKET_ID, "" + SaveRecorder.newTicket());

        Response response = responder.makeResponse(context, request);
        assertEquals(303, response.getStatus());

        request.addInput(EditResponder.CONTENT_INPUT_NAME, newContent + " Ok I'm working now");
        request.addInput(EditResponder.TIME_STAMP, "" + clock.currentClockTimeInMillis());
        response = responder.makeResponse(context, request);
        assertEquals(303, response.getStatus());
    }

    @Test
    public void testUsernameIsSavedInPageProperties() throws Exception {
        addRequestParameters();
        request.setCredentials("Aladdin", "open sesame");
        response = responder.makeResponse(context, request);

        String user = root.getChildPage("EditPage").getData().getAttribute(PageData.LAST_MODIFYING_USER);
        assertEquals("Aladdin", user);
    }

    @Test
    public void testContentFilter() throws Exception {
        contentFilter = new ContentFilter() {
            public boolean isContentAcceptable(String content, String page) {
                return false;
            }
        };
        crawler.addPage(root, PathParser.parse("ChildPage"));
        prepareRequest("ChildPage");

        Response response = responder.makeResponse(context, request);
        assertEquals(200, response.getStatus());
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        assertSubString("Your changes will not be saved!", sender.sentData());
    }

    private void createAndSaveANewPage(String pageName) throws Exception {
        WikiPage simplePage = crawler.addPage(root, PathParser.parse(pageName));

        PageData data = simplePage.getData();
        SaveRecorder.pageSaved(data, 0, clock);
        simplePage.commit(data);
    }

    private void doSimpleEdit() throws Exception {
        crawler.addPage(root, PathParser.parse("EditPage"));
        addRequestParameters();

        response = responder.makeResponse(context, request);
    }

    private void addRequestParameters() {
        prepareRequest("EditPage");
    }

    @Test
    public void testHasVersionHeader() throws Exception {
        doSimpleEdit();
        assertTrue("header missing", response.getHeader("Previous-Version") != null);
    }
}
