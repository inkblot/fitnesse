// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import java.util.regex.Pattern;

import fitnesse.responders.ResponderFactory;
import fitnesse.responders.files.SampleFileUtility;
import fitnesse.testutil.MockSocket;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class FitNesseServerTest {
    private SampleFileUtility sample = new SampleFileUtility();

    private PageCrawler crawler;
    private WikiPage root;
    private WikiPagePath pageOnePath;
    private WikiPagePath pageOneTwoPath;
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        sample.makeSampleFiles();
        root = InMemoryPage.makeRoot("RootPage");
        crawler = root.getPageCrawler();
        pageOnePath = PathParser.parse("PageOne");
        pageOneTwoPath = PathParser.parse("PageOne.PageTwo");
        context = FitNesseUtil.makeTestContext(root);
    }

    @After
    public void tearDown() throws Exception {
        sample.deleteSampleFiles();
    }

    @Test
    public void testSimple() throws Exception {
        crawler.addPage(root, PathParser.parse("SomePage"), "some string");
        String output = getSocketOutput("GET /SomePage HTTP/1.1\r\n\r\n", root);
        String statusLine = "HTTP/1.1 200 OK\r\n";
        assertTrue("Should have statusLine", Pattern.compile(statusLine, Pattern.MULTILINE).matcher(output).find());
        assertTrue("Should have canned Content", hasSubString("some string", output));
    }

    @Test
    public void testNotFound() throws Exception {
        String output = getSocketOutput("GET /WikiWord HTTP/1.1\r\n\r\n", new WikiPageDummy());

        assertSubString("Page doesn't exist.", output);
    }

    @Test
    public void testBadRequest() throws Exception {
        String output = getSocketOutput("Bad Request \r\n\r\n", new WikiPageDummy());

        assertSubString("400 Bad Request", output);
        assertSubString("The request string is malformed and can not be parsed", output);
    }

    @Test
    public void testSomeOtherPage() throws Exception {
        crawler.addPage(root, pageOnePath, "Page One Content");
        String output = getSocketOutput("GET /PageOne HTTP/1.1\r\n\r\n", root);
        String expected = "Page One Content";
        assertTrue("Should have page one", hasSubString(expected, output));
    }

    @Test
    public void testSecondLevelPage() throws Exception {
        crawler.addPage(root, pageOnePath, "Page One Content");
        crawler.addPage(root, pageOneTwoPath, "Page Two Content");
        String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n", root);

        String expected = "Page Two Content";
        assertTrue("Should have page Two", hasSubString(expected, output));
    }

    @Test
    public void testRelativeAndAbsoluteLinks() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RootPage");
        crawler.addPage(root, pageOnePath, "PageOne");
        crawler.addPage(root, pageOneTwoPath, "PageTwo");
        String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n", root);
        String expected = "href=\"PageOne.PageTwo\".*PageTwo";
        assertTrue("Should have relative link", hasSubString(expected, output));

        crawler.addPage(root, PathParser.parse("PageTwo"), "PageTwo at root");
        crawler.addPage(root, PathParser.parse("PageOne.PageThree"), "PageThree has link to .PageTwo at the root");
        output = getSocketOutput("GET /PageOne.PageThree HTTP/1.1\r\n\r\n", root);
        expected = "href=\"PageTwo\".*[.]PageTwo";
        assertTrue("Should have absolute link", hasSubString(expected, output));
    }

    @Test
    public void testServingRegularFiles() throws Exception {
        String output = getSocketOutput("GET /files/testDir/testFile2 HTTP/1.1\r\n\r\n", new WikiPageDummy());
        assertHasRegexp("file2 content", output);
    }

    private String getSocketOutput(String requestLine, WikiPage page) throws Exception {
        MockSocket s = new MockSocket(requestLine);
        context.rootPagePath = sample.base;
        context.responderFactory = new ResponderFactory(sample.base);
        context.root = page;
        FitNesseServer server = new FitNesseServer(context);
        server.serve(s, 1000);
        return s.getOutput();
    }

    private static boolean hasSubString(String expected, String output) {
        return Pattern.compile(expected, Pattern.MULTILINE).matcher(output).find();
    }
}
