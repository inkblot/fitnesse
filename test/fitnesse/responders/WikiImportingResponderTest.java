// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.*;
import com.google.inject.name.Names;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.Responder;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static util.RegexAssertions.*;

public class WikiImportingResponderTest extends ImporterTestCase {
    private WikiImportingResponder responder;
    private String remoteUrl;
    private HtmlPageFactory htmlPageFactory;
    private Authenticator remoteAuthenticator;
    private WikiPage root;
    private RunningTestingTracker runningTestingTracker;

    @Override
    protected Module getRemoteOverrides() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(Authenticator.class).toProvider(new Provider<Authenticator>() {
                    @Override
                    public Authenticator get() {
                        return remoteAuthenticator;
                    }
                });
            }
        };
    }

    @Before
    public void setUp() throws Exception {
        htmlPageFactory = localInjector.getInstance(HtmlPageFactory.class);
        root = localInjector.getInstance(Key.get(WikiPage.class, Names.named(FitNesseModule.ROOT_PAGE)));
        runningTestingTracker = localInjector.getInstance(RunningTestingTracker.class);
        remoteAuthenticator = new PromiscuousAuthenticator(remoteRoot, remoteInjector);
        fitNesseUtil.startFitnesse(remoteContext);
        remoteUrl = FitNesseUtil.URL;

        responder = createResponder(htmlPageFactory, root, localContext, runningTestingTracker);
    }

    private static WikiImportingResponder createResponder(HtmlPageFactory htmlPageFactory, WikiPage root, FitNesseContext context, RunningTestingTracker runningTestingTracker) throws Exception {
        WikiImportingResponder responder = new WikiImportingResponder(htmlPageFactory, root, context, runningTestingTracker);
        ChunkedResponse response = new ChunkedResponse("html");
        response.readyToSend(new MockResponseSender());
        responder.setResponse(response);
        responder.getImporter().setDeleteOrphanOption(false);
        return responder;
    }

    @After
    public void tearDown() throws Exception {
        fitNesseUtil.stopFitnesse();
    }

    @Test
    public void testActionsOfMakeResponse() throws Exception {
        Response response = makeSampleResponse(remoteUrl, responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        assertEquals(2, pageTwo.getChildren().size());
        WikiPage importedPageOne = pageTwo.getChildPage("PageOne");
        assertNotNull(importedPageOne);
        assertEquals("page one", importedPageOne.getData().getContent());

        WikiPage importedPageTwo = pageTwo.getChildPage("PageTwo");
        assertNotNull(importedPageTwo);
        assertEquals("page two", importedPageTwo.getData().getContent());

        assertEquals(1, importedPageOne.getChildren().size());
        WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
        assertNotNull(importedChildOne);
        assertEquals("child one", importedChildOne.getData().getContent());
    }

    @Test
    public void testImportingFromNonRootPageUpdatesPageContent() throws Exception {
        PageData data = pageTwo.getData();
        WikiImportProperty importProperty = new WikiImportProperty(remoteUrl + "PageOne");
        importProperty.addTo(data.getProperties());
        data.setContent("nonsense");
        pageTwo.commit(data);

        Response response = makeSampleResponse("blah", responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        data = pageTwo.getData();
        assertEquals("page one", data.getContent());

        assertFalse(WikiImportProperty.createFrom(data.getProperties()).isRoot());
    }

    @Test
    public void testImportPropertiesGetAdded() throws Exception {
        Response response = makeSampleResponse(remoteUrl, responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        checkProperties(pageTwo, remoteUrl, true, null);

        WikiPage importedPageOne = pageTwo.getChildPage("PageOne");
        checkProperties(importedPageOne, remoteUrl + "PageOne", false, remoteRoot.getChildPage("PageOne"));

        WikiPage importedPageTwo = pageTwo.getChildPage("PageTwo");
        checkProperties(importedPageTwo, remoteUrl + "PageTwo", false, remoteRoot.getChildPage("PageTwo"));

        WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
        checkProperties(importedChildOne, remoteUrl + "PageOne.ChildOne", false, remoteRoot.getChildPage("PageOne").getChildPage("ChildOne"));
    }

    private void checkProperties(WikiPage page, String source, boolean isRoot, WikiPage remotePage) throws Exception {
        WikiPageProperties props = page.getData().getProperties();
        if (!isRoot)
            assertFalse("should not have Edit property", props.has("Edit"));

        WikiImportProperty importProperty = WikiImportProperty.createFrom(props);
        assertNotNull(importProperty);
        assertEquals(source, importProperty.getSourceUrl());
        assertEquals(isRoot, importProperty.isRoot());

        if (remotePage != null) {
            long remoteLastModificationTime = remotePage.getData().getProperties().getLastModificationTime().getTime();
            long importPropertyLastModificationTime = importProperty.getLastRemoteModificationTime().getTime();
            assertEquals(remoteLastModificationTime, importPropertyLastModificationTime);
        }
    }

    @Test
    public void testHtmlOfMakeResponse() throws Exception {
        Response response = makeSampleResponse(remoteUrl, responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertSubString("<html>", content);
        assertSubString("Wiki Import", content);

        assertSubString("href=\"PageTwo\"", content);
        assertSubString("href=\"PageTwo.PageOne\"", content);
        assertSubString("href=\"PageTwo.PageOne.ChildOne\"", content);
        assertSubString("href=\"PageTwo.PageTwo\"", content);
        assertSubString("Import complete.", content);
        assertSubString("3 pages were imported.", content);
    }

    @Test
    public void testHtmlOfMakeResponseWithNoModifications() throws Exception {
        Response response = makeSampleResponse(remoteUrl, responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        // import a second time... nothing was modified
        WikiImportingResponder secondResponder = createResponder(htmlPageFactory, root, localContext, runningTestingTracker);
        response = makeSampleResponse(remoteUrl, secondResponder);
        sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertSubString("<html>", content);
        assertSubString("Wiki Import", content);

        assertSubString("href=\"PageTwo\"", content);
        assertNotSubString("href=\"PageTwo.PageOne\"", content);
        assertNotSubString("href=\"PageTwo.PageOne.ChildOne\"", content);
        assertNotSubString("href=\"PageTwo.PageTwo\"", content);
        assertSubString("Import complete.", content);
        assertSubString("0 pages were imported.", content);
        assertSubString("3 pages were unmodified.", content);
    }

    private static ChunkedResponse makeSampleResponse(String remoteUrl, Responder responder) throws Exception {
        return getResponse(responder, makeRequest(remoteUrl));
    }

    private static ChunkedResponse getResponse(Responder responder, MockRequest request) throws Exception {
        Response response = responder.makeResponse(request);
        assertTrue(response instanceof ChunkedResponse);
        return (ChunkedResponse) response;
    }

    private static MockRequest makeRequest(String remoteUrl) {
        MockRequest request = new MockRequest();
        request.setResource("PageTwo");
        request.addInput("responder", "import");
        request.addInput("remoteUrl", remoteUrl);
        return request;
    }

    @Test
    public void testMakeResponseImportingNonRootPage() throws Exception {
        MockRequest request = makeRequest(remoteUrl + "PageOne");

        Response response = responder.makeResponse(request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertNotNull(pageTwo.getChildPage("ChildOne"));
        assertSubString("href=\"PageTwo.ChildOne\"", content);
        assertSubString(">ChildOne<", content);
    }

    @Test
    public void testRemoteUrlNotFound() throws Exception {
        String remoteUrl = this.remoteUrl + "PageDoesntExist";
        Response response = makeSampleResponse(remoteUrl, responder);

        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The remote resource, " + remoteUrl + ", was not found.", content);
    }

    @Test
    public void testErrorMessageForBadUrlProvided() throws Exception {
        String remoteUrl = this.remoteUrl + "blah";
        Response response = makeSampleResponse(remoteUrl, responder);

        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The URL's resource path, blah, is not a valid WikiWord.", content);
    }

    @Test
    public void testUnauthorizedResponse() throws Exception {
        makeSecurePage(remoteRoot);

        Response response = makeSampleResponse(remoteUrl, responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        checkRemoteLoginForm(content);
    }

    private void makeSecurePage(WikiPage page) throws Exception {
        PageData data = page.getData();
        data.setAttribute(PageData.PropertySECURE_READ);
        page.commit(data);
        remoteAuthenticator = new OneUserAuthenticator("joe", "blow", remoteRoot, remoteInjector);
    }

    private void checkRemoteLoginForm(String content) {
        assertHasRegexp("The wiki at .* requires authentication.", content);
        assertSubString("<form", content);
        assertHasRegexp("<input[^>]*name=\"remoteUsername\"", content);
        assertHasRegexp("<input[^>]*name=\"remotePassword\"", content);
    }

    @Test
    public void testUnauthorizedResponseFromNonRoot() throws Exception {
        WikiPage childPage = remoteRoot.getChildPage("PageOne");
        makeSecurePage(childPage);

        Response response = makeSampleResponse(remoteUrl, responder);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The wiki at " + remoteUrl + "PageOne requires authentication.", content);
        assertSubString("<form", content);
    }

    @Test
    public void testImportingFromSecurePageWithCredentials() throws Exception {
        makeSecurePage(remoteRoot);

        MockRequest request = makeRequest(remoteUrl);
        request.addInput("remoteUsername", "joe");
        request.addInput("remotePassword", "blow");
        Response response = getResponse(responder, request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertNotSubString("requires authentication", content);
        assertSubString("3 pages were imported.", content);

        assertEquals("joe", WikiImporter.remoteUsername);
        assertEquals("blow", WikiImporter.remotePassword);
    }

    @Test
    public void testListOfOrphanedPages() throws Exception {
        WikiImporter importer = new WikiImporter();

        String tail = responder.makeTailHtml(importer).html();

        assertNotSubString("orphan", tail);
        assertNotSubString("PageOne", tail);
        assertNotSubString("PageOne.ChildPagae", tail);

        importer.getOrphans().add(new WikiPagePath(pageOne));
        importer.getOrphans().add(new WikiPagePath(childPageOne));

        tail = responder.makeTailHtml(importer).html();

        assertSubString("2 orphaned pages were found and have been removed.", tail);
        assertSubString("PageOne", tail);
        assertSubString("PageOne.ChildOne", tail);
    }

    @Test
    public void testAutoUpdatingTurnedOn() throws Exception {
        MockRequest request = makeRequest(remoteUrl);
        responder.setRequest(request);
        responder.data = new PageData(new WikiPageDummy(localInjector));

        responder.initializeImporter(ChunkingResponder.getWikiPagePath(request));
        assertFalse(responder.getImporter().getAutoUpdateSetting());

        request.addInput("autoUpdate", "1");
        responder.initializeImporter(ChunkingResponder.getWikiPagePath(request));
        assertTrue(responder.getImporter().getAutoUpdateSetting());
    }

    @Test
    public void testAutoUpdateSettingDisplayedInTail() throws Exception {
        WikiImporter importer = new MockWikiImporter();
        importer.setAutoUpdateSetting(true);

        String tail = responder.makeTailHtml(importer).html();
        assertSubString("Automatic Update turned ON", tail);

        importer.setAutoUpdateSetting(false);

        tail = responder.makeTailHtml(importer).html();
        assertSubString("Automatic Update turned OFF", tail);
    }
}
