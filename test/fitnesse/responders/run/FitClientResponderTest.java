// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertNotSubString;
import static util.RegexAssertions.assertSubString;

public class FitClientResponderTest extends FitnesseBaseTestCase {
    private FitClientResponder responder;
    private MockRequest request;
    private PageCrawler crawler;
    private WikiPage suite;
    private WikiPage root;
    private SocketDealer socketDealer;
    private RunningTestingTracker runningTestingTracker;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root, SocketDealer socketDealer, RunningTestingTracker runningTestingTracker) {
        this.root = root;
        this.socketDealer = socketDealer;
        this.runningTestingTracker = runningTestingTracker;
    }

    @Before
    public void setUp() throws Exception {
        responder = new FitClientResponder(root, getPort(), socketDealer, runningTestingTracker, injector);
        request = new MockRequest();

        crawler = root.getPageCrawler();
        suite = crawler.addPage(root, PathParser.parse("SuitePage"), "!path classes\n");
        WikiPage page1 = crawler.addPage(suite, PathParser.parse("TestPassing"), "!|fitnesse.testutil.PassFixture|\n");
        WikiPage page2 = crawler.addPage(suite, PathParser.parse("TestFailing"), "!|fitnesse.testutil.FailFixture|\n");
        WikiPage page3 = crawler.addPage(suite, PathParser.parse("TestAnotherFailing"), "!|fitnesse.testutil.FailFixture|\n");
        crawler.addPage(suite, PathParser.parse("TestError"), "!|fitnesse.testutil.ErrorFixture|\n");
        crawler.addPage(suite, PathParser.parse("TestIgnore"), "!|fitnesse.testutil.IgnoreFixture|\n");
        crawler.addPage(suite, PathParser.parse("SomePage"), "This is just some page.");

        PageData data1 = page1.getData();
        PageData data2 = page2.getData();
        PageData data3 = page3.getData();
        data1.setAttribute(PageData.PropertySUITES, "skip,foo");
        data2.setAttribute(PageData.PropertySUITES, "bar,smoke");
        data3.setAttribute(PageData.PropertySUITES, "foo");
        page1.commit(data1);
        page2.commit(data2);
        page3.commit(data3);
    }

    @Test
    public void testPageNotFound() throws Exception {
        String result = getResultFor("MissingPage");
        assertSubString("MissingPage was not found", result);
    }

    @Test
    public void testOneTest() throws Exception {
        String result = getResultFor("SuitePage.TestPassing");
        assertEquals("0000000000", result.substring(0, 10));
        assertSubString("PassFixture", result);
    }

    @Test
    public void testSuite() throws Exception {
        String result = getResultFor("SuitePage");
        assertEquals("0000000000", result.substring(0, 10));
        assertSubString("PassFixture", result);
        assertSubString("FailFixture", result);
        assertSubString("ErrorFixture", result);
        assertSubString("IgnoreFixture", result);
        assertNotSubString("some page", result);
    }

    @Test
    public void testRelativePageNamesIncluded() throws Exception {
        String result = getResultFor("SuitePage");
        assertNotSubString("SuitePage", result);
        assertSubString("TestPassing", result);
        assertSubString("TestFailing", result);
        assertSubString("TestError", result);
        assertSubString("TestIgnore", result);
    }

    @Test
    public void testPageThatIsNoATest() throws Exception {
        String result = getResultFor("SuitePage.SomePage");
        assertSubString("SomePage is neither a Test page nor a Suite page.", result);
    }

    private String getResultFor(String name) throws Exception {
        return getResultFor(name, false);
    }

    private String getResultFor(String name, boolean addPaths) throws Exception {
        request.setResource(name);
        if (addPaths)
            request.addInput("includePaths", "blah");
        Response response = responder.makeResponse(request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        return sender.sentData();
    }

    @Test
    public void testWithClasspathOnSuite() throws Exception {
        String result = getResultFor("SuitePage", true);
        assertTrue("was: " + result, result.startsWith("00000000000000000007classes"));
    }

    @Test
    public void testWithClasspathOnTestInSuite() throws Exception {
        crawler.addPage(suite, PathParser.parse("TestPage"), "!path jar.jar\n!path /some/dir/with/.class/files\n!|fitnesse.testutil.IgnoreFixture|\n");
        String result = getResultFor("SuitePage.TestPage", true);

        assertSubString("classes", result);
        assertSubString("jar.jar", result);
        assertSubString("/some/dir/with/.class/files", result);
    }
}
