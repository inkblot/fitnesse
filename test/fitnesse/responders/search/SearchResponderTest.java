// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Response;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import junit.framework.TestCase;

import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class SearchResponderTest extends TestCase {
    private SearchResponder responder;
    private MockRequest request;
    private FitNesseContext context;

    public void setUp() throws Exception {
        context = new FitNesseContext("RooT");
        PageCrawler crawler = context.root.getPageCrawler();
        crawler.addPage(context.root, PathParser.parse("SomePage"), "has something in it");
        responder = new SearchResponder();
        request = new MockRequest();
        request.addInput("searchString", "blah");
        request.addInput("searchType", "blah");
    }

    public void tearDown() throws Exception {
    }

    public void testHtml() throws Exception {
        String content = getResponseContentUsingSearchString("something");

        assertHasRegexp("something", content);
        assertHasRegexp("SomePage", content);
    }

    public void testTableSorterScript() throws Exception {
        String content = getResponseContentUsingSearchString("something");
        assertSubString("<script language=\"javascript\">tableSorter = new TableSorter('searchResultsTable', new DateParser(/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/,8,2,3,4,5,6));</script>", content);
    }

    public void testClientSideSortScript() throws Exception {
        String content = getResponseContentUsingSearchString("something");
        assertHasRegexp("<script src=\"/files/javascript/clientSideSort.js\"> </script>", content);
    }

    public void testPageSortLink() throws Exception {
        String content = getResponseContentUsingSearchString("something");
        assertSubString("<a href=\"javascript:void(tableSorter.sort(1));\">Page</a>", content);
    }

    public void testLastModifiedSortLink() throws Exception {
        String content = getResponseContentUsingSearchString("something");
        assertSubString("<a href=\"javascript:void(tableSorter.sort(3, 'date'));\">LastModified</a>", content);
    }

    public void testNoSearchStringBringsUpNoResults() throws Exception {
        String content = getResponseContentUsingSearchString("");
        assertSubString("No pages matched your search criteria.", content);
    }

    public void testEscapesSearchString() throws Exception {
        String content = getResponseContentUsingSearchString("!+-<&>");
        assertSubString("!+-<&>", content);
    }

    private String getResponseContentUsingSearchString(String searchString) throws Exception {
        request.addInput("searchString", searchString);

        Response response = responder.makeResponse(context, request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        return sender.sentData();
    }

    public void testTitle() throws Exception {
        request.addInput("searchType", "something with the word title in it");
        responder.setRequest(request);
        String title = responder.getTitle();
        assertSubString("Title Search Results", title);

        request.addInput("searchType", "something with the word content in it");
        title = responder.getTitle();
        assertSubString("Content Search Results", title);
    }

    public void testJavascriptDateFormatRegex() {
        assertEquals("/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/", SearchResponder.getDateFormatJavascriptRegex());
    }

}
