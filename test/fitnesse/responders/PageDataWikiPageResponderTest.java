// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PageDataWikiPageResponderTest extends FitnesseBaseTestCase {
    private WikiPage pageOne;
    private HtmlPageFactory htmlPageFactory;
    private WikiPage root;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        pageOne = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "Line one\nLine two");
    }

    @Test
    public void testGetPageData() throws Exception {
        Responder responder = new PageDataWikiPageResponder(htmlPageFactory, root);
        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        request.addInput("pageData", "");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);
        assertEquals(pageOne.getData().getContent(), response.getContent());
    }
}
