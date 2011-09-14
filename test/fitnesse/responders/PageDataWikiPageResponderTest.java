// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageDataWikiPageResponderTest extends FitnesseBaseTestCase {
    private WikiPage pageOne;
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        pageOne = context.root.getPageCrawler().addPage(context.root, PathParser.parse("PageOne"), "Line one\nLine two");
    }

    @Test
    public void testGetPageData() throws Exception {
        Responder responder = new PageDataWikiPageResponder();
        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        request.addInput("pageData", "");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals(pageOne.getData().getContent(), response.getContent());
    }
}
