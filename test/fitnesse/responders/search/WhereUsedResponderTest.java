// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;

public class WhereUsedResponderTest extends FitnesseBaseTestCase {
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        PageCrawler crawler = context.root.getPageCrawler();
        crawler.addPage(context.root, PathParser.parse("PageOne"), "PageOne");
        WikiPage pageTwo = crawler.addPage(context.root, PathParser.parse("PageTwo"), "PageOne");
        crawler.addPage(pageTwo, PathParser.parse("ChildPage"), ".PageOne");
    }

    @Test
    public void testResponse() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        WhereUsedResponder responder = new WhereUsedResponder();

        Response response = responder.makeResponse(context, request);
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

