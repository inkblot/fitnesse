// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static util.RegexAssertions.assertDoesNotHaveRegexp;
import static util.RegexAssertions.assertHasRegexp;

public class NameWikiPageResponderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private NameWikiPageResponder responder;
    private MockRequest request;
    private PageCrawler crawler;
    private String pageOneName;
    private String pageTwoName;
    private String frontPageName;
    private WikiPagePath pageOnePath;
    private WikiPagePath pageTwoPath;
    private WikiPagePath frontPagePath;
    private FitNesseContext context;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        root = context.root;
        crawler = root.getPageCrawler();
        responder = new NameWikiPageResponder(htmlPageFactory);
        request = new MockRequest();

        pageOneName = "PageOne";
        pageTwoName = "PageTwo";
        frontPageName = "FrontPage";

        pageOnePath = PathParser.parse(pageOneName);
        pageTwoPath = PathParser.parse(pageTwoName);
        frontPagePath = PathParser.parse(frontPageName);
    }

    @Test
    public void testTextPlain() throws Exception {

        Response r = responder.makeResponse(context, request);
        assertEquals("text/plain", r.getContentType());
    }

    @Test
    public void testPageNamesFromRoot() throws Exception {
        crawler.addPage(root, pageOnePath);
        crawler.addPage(root, pageTwoPath);
        request.setResource("");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertHasRegexp(pageOneName, response.getContent());
        assertHasRegexp(pageTwoName, response.getContent());
    }

    @Test
    public void testPageNamesFromASubPage() throws Exception {
        WikiPage frontPage = crawler.addPage(root, frontPagePath);
        crawler.addPage(frontPage, pageOnePath);
        crawler.addPage(frontPage, pageTwoPath);
        request.setResource("");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertHasRegexp(frontPageName, response.getContent());
        assertDoesNotHaveRegexp(pageOneName, response.getContent());
        assertDoesNotHaveRegexp(pageTwoName, response.getContent());

        request.setResource(frontPageName);
        response = (SimpleResponse) responder.makeResponse(context, request);
        assertHasRegexp(pageOneName, response.getContent());
        assertHasRegexp(pageTwoName, response.getContent());
        assertDoesNotHaveRegexp(frontPageName, response.getContent());
    }

    @Test
    public void jsonFormat() throws Exception {
        crawler.addPage(root, pageOnePath);
        crawler.addPage(root, pageTwoPath);
        request.setResource("");
        request.addInput("format", "json");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        JSONArray actual = new JSONArray(response.getContent());
        assertEquals(2, actual.length());
        Set<String> actualSet = new HashSet<String>();
        actualSet.add(actual.getString(0));
        actualSet.add(actual.getString(1));
        Set<String> expectedSet = new HashSet<String>();
        expectedSet.add(pageOneName);
        expectedSet.add(pageTwoName);
        assertEquals(expectedSet, actualSet);
    }

    @Test
    public void canShowChildCount() throws Exception {
        WikiPage frontPage = crawler.addPage(root, frontPagePath);
        crawler.addPage(frontPage, pageOnePath);
        crawler.addPage(frontPage, pageTwoPath);
        request.setResource("");
        request.addInput("ShowChildCount", "");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertHasRegexp("FrontPage 2", response.getContent());

    }
}
