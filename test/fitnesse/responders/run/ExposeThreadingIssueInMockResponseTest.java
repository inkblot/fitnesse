// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.testutil.FitSocketReceiver;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExposeThreadingIssueInMockResponseTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private MockRequest request;
    private TestResponder responder;
    private FitNesseContext context;
    private PageCrawler crawler;
    private FitSocketReceiver receiver;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {
        context = makeContext(9123);
        root = context.root;
        crawler = root.getPageCrawler();
        request = new MockRequest();
        responder = new TestResponder(htmlPageFactory);

        receiver = new FitSocketReceiver(context.port, context.socketDealer);
        receiver.receiveSocket();
    }

    @After
    public void tearDown() throws Exception {
        receiver.close();
    }

    public static void assertHasRegexp(String regexp, String output) {
        Matcher match = Pattern.compile(regexp, Pattern.MULTILINE | Pattern.DOTALL).matcher(output);
        boolean found = match.find();
        if (!found)
            Assert.fail("The regexp <" + regexp + "> was not found in: " + output + ".");
    }

    @Test
    public void testDoSimpleSlimTable() throws Exception {
        String results = doSimpleRun(simpleSlimDecisionTable());
        assertHasRegexp("<td><span class=\"pass\">wow</span></td>", results);
    }

    private String simpleSlimDecisionTable() {
        return "!define TEST_SYSTEM {slim}\n" + "|!-DT:fitnesse.slim.test.TestSlim-!|\n" + "|string|get string arg?|\n"
                + "|wow|wow|\n";
    }

    private String doSimpleRun(String fixtureTable) throws Exception {
        String simpleRunPageName = "TestPage";
        WikiPage testPage = crawler.addPage(root, PathParser.parse(simpleRunPageName), classpathWidgets() + fixtureTable);
        request.setResource(testPage.getName());

        Response response = responder.makeResponse(context, request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);

        return sender.sentData();
    }

    private String classpathWidgets() {
        return "!path classes\n";
    }
}
