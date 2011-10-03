// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static util.RegexAssertions.*;

public class WikiImportingResponderTest extends ImporterTestCase {
    private WikiImportingResponder responder;
    private String baseUrl;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {
        fitNesseUtil.startFitnesse(remoteContext);
        baseUrl = FitNesseUtil.URL;

        createResponder();
    }

    private void createResponder() throws Exception {
        responder = new WikiImportingResponder(htmlPageFactory);
        responder.path = new WikiPagePath();
        ChunkedResponse response = new ChunkedResponse("html");
        response.readyToSend(new MockResponseSender());
        responder.setResponse(response);
        responder.getImporter().setDeleteOrphanOption(false);
    }

    @After
    public void tearDown() throws Exception {
        fitNesseUtil.stopFitnesse();
    }

    @Test
    public void testActionsOfMakeResponse() throws Exception {
        Response response = makeSampleResponse(baseUrl);
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
        WikiImportProperty importProperty = new WikiImportProperty(baseUrl + "PageOne");
        importProperty.addTo(data.getProperties());
        data.setContent("nonsense");
        pageTwo.commit(data);

        Response response = makeSampleResponse("blah");
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        data = pageTwo.getData();
        assertEquals("page one", data.getContent());

        assertFalse(WikiImportProperty.createFrom(data.getProperties()).isRoot());
    }

    @Test
    public void testImportPropertiesGetAdded() throws Exception {
        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        checkProperties(pageTwo, baseUrl, true, null);

        WikiPage importedPageOne = pageTwo.getChildPage("PageOne");
        checkProperties(importedPageOne, baseUrl + "PageOne", false, remoteRoot.getChildPage("PageOne"));

        WikiPage importedPageTwo = pageTwo.getChildPage("PageTwo");
        checkProperties(importedPageTwo, baseUrl + "PageTwo", false, remoteRoot.getChildPage("PageTwo"));

        WikiPage importedChildOne = importedPageOne.getChildPage("ChildOne");
        checkProperties(importedChildOne, baseUrl + "PageOne.ChildOne", false, remoteRoot.getChildPage("PageOne").getChildPage("ChildOne"));
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
        Response response = makeSampleResponse(baseUrl);
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
        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        // import a second time... nothing was modified
        createResponder();
        response = makeSampleResponse(baseUrl);
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

    private ChunkedResponse makeSampleResponse(String remoteUrl) throws Exception {
        MockRequest request = makeRequest(remoteUrl);

        return getResponse(request);
    }

    private ChunkedResponse getResponse(MockRequest request) throws Exception {
        Response response = responder.makeResponse(localContext, request);
        assertTrue(response instanceof ChunkedResponse);
        return (ChunkedResponse) response;
    }

    private MockRequest makeRequest(String remoteUrl) {
        MockRequest request = new MockRequest();
        request.setResource("PageTwo");
        request.addInput("responder", "import");
        request.addInput("remoteUrl", remoteUrl);
        return request;
    }

    @Test
    public void testMakeResponseImportingNonRootPage() throws Exception {
        MockRequest request = makeRequest(baseUrl + "PageOne");

        Response response = responder.makeResponse(localContext, request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();

        assertNotNull(pageTwo.getChildPage("ChildOne"));
        assertSubString("href=\"PageTwo.ChildOne\"", content);
        assertSubString(">ChildOne<", content);
    }

    @Test
    public void testRemoteUrlNotFound() throws Exception {
        String remoteUrl = baseUrl + "PageDoesntExist";
        Response response = makeSampleResponse(remoteUrl);

        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The remote resource, " + remoteUrl + ", was not found.", content);
    }

    @Test
    public void testErrorMessageForBadUrlProvided() throws Exception {
        String remoteUrl = baseUrl + "blah";
        Response response = makeSampleResponse(remoteUrl);

        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The URL's resource path, blah, is not a valid WikiWord.", content);
    }

    @Test
    public void testUnauthorizedResponse() throws Exception {
        makeSecurePage(remoteRoot);

        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        checkRemoteLoginForm(content);
    }

    private void makeSecurePage(WikiPage page) throws Exception {
        PageData data = page.getData();
        data.setAttribute(PageData.PropertySECURE_READ);
        page.commit(data);
        remoteContext.authenticator = new OneUserAuthenticator("joe", "blow");
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

        Response response = makeSampleResponse(baseUrl);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        String content = sender.sentData();
        assertSubString("The wiki at " + baseUrl + "PageOne requires authentication.", content);
        assertSubString("<form", content);
    }

    @Test
    public void testImportingFromSecurePageWithCredentials() throws Exception {
        makeSecurePage(remoteRoot);

        MockRequest request = makeRequest(baseUrl);
        request.addInput("remoteUsername", "joe");
        request.addInput("remotePassword", "blow");
        Response response = getResponse(request);
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
        MockRequest request = makeRequest(baseUrl);
        responder.setRequest(request);
        responder.data = new PageData(new WikiPageDummy(injector));

        responder.initializeImporter();
        assertFalse(responder.getImporter().getAutoUpdateSetting());

        request.addInput("autoUpdate", "1");
        responder.initializeImporter();
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
