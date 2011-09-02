// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.http.MockSocket;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class FitNesseServerTest extends FitnesseBaseTestCase {
    private PageCrawler crawler;
    private WikiPagePath pageOnePath;
    private WikiPagePath pageOneTwoPath;
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("TestDir");
        crawler = root.getPageCrawler();
        pageOnePath = PathParser.parse("PageOne");
        pageOneTwoPath = PathParser.parse("PageOne.PageTwo");
        context = makeContext(root);
        makeSampleFiles();
    }

    @Test
    public void testSimple() throws Exception {
        crawler.addPage(context.root, PathParser.parse("SomePage"), "some string");
        String output = getSocketOutput("GET /SomePage HTTP/1.1\r\n\r\n");
        String statusLine = "HTTP/1.1 200 OK\r\n";
        assertTrue("Should have statusLine", Pattern.compile(statusLine, Pattern.MULTILINE).matcher(output).find());
        assertTrue("Should have canned Content", hasSubString("some string", output));
    }

    @Test
    public void testNotFound() throws Exception {
        String output = getSocketOutput("GET /WikiWord HTTP/1.1\r\n\r\n");

        assertSubString("Page doesn't exist.", output);
    }

    @Test
    public void testBadRequest() throws Exception {
        String output = getSocketOutput("Bad Request \r\n\r\n");

        assertSubString("400 Bad Request", output);
        assertSubString("The request string is malformed and can not be parsed", output);
    }

    @Test
    public void testSomeOtherPage() throws Exception {
        crawler.addPage(context.root, pageOnePath, "Page One Content");
        String output = getSocketOutput("GET /PageOne HTTP/1.1\r\n\r\n");
        String expected = "Page One Content";
        assertTrue("Should have page one", hasSubString(expected, output));
    }

    @Test
    public void testSecondLevelPage() throws Exception {
        crawler.addPage(context.root, pageOnePath, "Page One Content");
        crawler.addPage(context.root, pageOneTwoPath, "Page Two Content");
        String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n");

        String expected = "Page Two Content";
        assertTrue("Should have page Two", hasSubString(expected, output));
    }

    @Test
    public void testRelativeAndAbsoluteLinks() throws Exception {
        crawler.addPage(context.root, pageOnePath, "PageOne");
        crawler.addPage(context.root, pageOneTwoPath, "PageTwo");
        String output = getSocketOutput("GET /PageOne.PageTwo HTTP/1.1\r\n\r\n");
        String expected = "href=\"PageOne.PageTwo\".*PageTwo";
        assertTrue("Should have relative link", hasSubString(expected, output));

        crawler.addPage(context.root, PathParser.parse("PageTwo"), "PageTwo at root");
        crawler.addPage(context.root, PathParser.parse("PageOne.PageThree"), "PageThree has link to .PageTwo at the root");
        output = getSocketOutput("GET /PageOne.PageThree HTTP/1.1\r\n\r\n");
        expected = "href=\"PageTwo\".*[.]PageTwo";
        assertTrue("Should have absolute link", hasSubString(expected, output));
    }

    @Test
    public void testServingRegularFiles() throws Exception {
        String output = getSocketOutput("GET /files/testDir/testFile2 HTTP/1.1\r\n\r\n");
        assertHasRegexp("file2 content", output);
    }

    private String getSocketOutput(String requestLine) throws Exception {
        MockSocket s = new MockSocket(requestLine);
        FitNesseServer server = new FitNesseServer(context);
        server.serve(s, 1000);
        return s.getOutput();
    }

    private static boolean hasSubString(String expected, String output) {
        return Pattern.compile(expected, Pattern.MULTILINE).matcher(output).find();
    }
}
