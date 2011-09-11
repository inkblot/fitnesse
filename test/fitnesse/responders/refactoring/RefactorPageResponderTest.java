// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import junit.framework.TestCase;

import static util.RegexAssertions.assertSubString;

public class RefactorPageResponderTest extends TestCase {
    WikiPage root;
    private MockRequest request;
    private Responder responder;
    private FitNesseContext context;

    public void setUp() throws Exception {
        context = new FitNesseContext("root");
        root = context.root;
        PageCrawler crawler = root.getPageCrawler();
        String childPage = "ChildPage";
        crawler.addPage(root, PathParser.parse(childPage));

        request = new MockRequest();
        request.setResource(childPage);
        responder = new RefactorPageResponder();
    }

    public void testHtml() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals(200, response.getStatus());

        String content = response.getContent();
        assertSubString("Replace", content);
        assertSubString("Delete Page", content);
        assertSubString("Rename Page", content);
        assertSubString("Move Page", content);
    }
}






