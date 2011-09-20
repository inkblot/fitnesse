// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static util.RegexAssertions.assertMatches;
import static util.RegexAssertions.assertSubString;

public class EditResponderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private MockRequest request;
    private EditResponder responder;
    private PageCrawler crawler;
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        root = context.root;
        crawler = root.getPageCrawler();
        request = new MockRequest();
        responder = new EditResponder();
    }

    @Test
    public void testResponse() throws Exception {
        crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
        request.setResource("ChildPage");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals(200, response.getStatus());

        String body = response.getContent();
        assertSubString("<html>", body);
        assertSubString("<form", body);
        assertSubString("method=\"post\"", body);
        assertSubString("child content with &lt;html&gt;", body);
        assertSubString("name=\"responder\"", body);
        assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
        assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
        assertSubString("type=\"submit\"", body);
        assertSubString(String.format("textarea class=\"%s no_wrap\" wrap=\"off\"", EditResponder.CONTENT_INPUT_NAME), body);
    }

    @Test
    public void testResponseWhenNonexistentPageRequestsed() throws Exception {
        request.setResource("NonExistentPage");
        request.addInput("nonExistent", true);

        FitNesseContext context = this.context;
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals(200, response.getStatus());

        String body = response.getContent();
        assertSubString("<html>", body);
        assertSubString("<form", body);
        assertSubString("method=\"post\"", body);
        assertSubString(context.defaultNewPageContent, body);
        assertSubString("name=\"responder\"", body);
        assertSubString("name=\"" + EditResponder.TIME_STAMP + "\"", body);
        assertSubString("name=\"" + EditResponder.TICKET_ID + "\"", body);
        assertSubString("type=\"submit\"", body);
    }

    @Test
    public void testRedirectToRefererEffect() throws Exception {
        crawler.addPage(root, PathParser.parse("ChildPage"), "child content with <html>");
        request.setResource("ChildPage");
        request.addInput("redirectToReferer", true);
        request.addInput("redirectAction", "boom");
        request.addHeader("Referer", "http://fitnesse.org:8080/SomePage");

        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals(200, response.getStatus());

        String body = response.getContent();
        HtmlTag redirectInputTag = HtmlUtil.makeInputTag("hidden", "redirect", "http://fitnesse.org:8080/SomePage?boom");
        assertSubString(redirectInputTag.html(), body);
    }

    @Test
    public void testPasteFromExcelExists() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String body = response.getContent();
        assertMatches("SpreadsheetTranslator.js", body);
        assertMatches("spreadsheetSupport.js", body);
    }

    @Test
    public void testFormatterScriptsExist() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String body = response.getContent();
        assertMatches("WikiFormatter.js", body);
        assertMatches("wikiFormatterSupport.js", body);
    }

    @Test
    public void testWrapScriptExists() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String body = response.getContent();
        assertMatches("textareaWrapSupport.js", body);
    }

    @Test
    public void testMissingPageDoesNotGetCreated() throws Exception {
        request.setResource("MissingPage");
        responder.makeResponse(context, request);
        assertFalse(root.hasChildPage("MissingPage"));
    }
}
