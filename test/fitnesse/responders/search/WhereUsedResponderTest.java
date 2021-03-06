// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;

public class WhereUsedResponderTest extends FitnesseBaseTestCase {
    private RunningTestingTracker runningTestingTracker;
    private HtmlPageFactory htmlPageFactory;
    private WikiPage root;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, RunningTestingTracker runningTestingTracker) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.runningTestingTracker = runningTestingTracker;
    }

    @Before
    public void setUp() throws Exception {
        PageCrawler crawler = root.getPageCrawler();
        crawler.addPage(root, PathParser.parse("PageOne"), "PageOne");
        WikiPage pageTwo = crawler.addPage(root, PathParser.parse("PageTwo"), "PageOne");
        crawler.addPage(pageTwo, PathParser.parse("ChildPage"), ".PageOne");
    }

    @Test
    public void testResponse() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        WhereUsedResponder responder = new WhereUsedResponder(htmlPageFactory, root, runningTestingTracker, isChunkingEnabled());

        Response response = responder.makeResponse(request);
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

