// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import junit.framework.TestCase;

import static util.RegexAssertions.assertHasRegexp;

public class WhereUsedResponderTest extends TestCase {
    private WikiPage root;

    public void setUp() throws Exception {
        root = InMemoryPage.makeRoot("RooT");
        PageCrawler crawler = root.getPageCrawler();
        crawler.addPage(root, PathParser.parse("PageOne"), "PageOne");
        WikiPage pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "PageOne");
        crawler.addPage(pageTwo, PathParser.parse("ChildPage"), ".PageOne");
    }

    public void testResponse() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        WhereUsedResponder responder = new WhereUsedResponder();

        Response response = responder.makeResponse(new FitNesseContext(root), request);
        MockResponseSender sender = new MockResponseSender();
        response.readyToSend(sender);
        sender.waitForClose(5000);

        String content = sender.sentData();
        assertEquals(200, response.getStatus());
        assertHasRegexp("Where Used", content);
        assertHasRegexp(">PageOne<", content);
        assertHasRegexp(">PageTwo<", content);
        assertHasRegexp(">PageTwo\\.ChildPage<", content);
    }
}

