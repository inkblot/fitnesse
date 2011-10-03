// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RollbackResponderTest extends FitnesseBaseTestCase {
    private WikiPage page;
    private Response response;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {
        FitNesseContext context = makeContext();
        page = context.root.getPageCrawler().addPage(context.root, PathParser.parse("PageOne"), "original content");
        PageData data = page.getData();
        data.setContent("new stuff");
        data.setProperties(new WikiPageProperties());
        VersionInfo commitRecord = page.commit(data);

        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        request.addInput("version", commitRecord.getName());

        Responder responder = new RollbackResponder(htmlPageFactory);
        response = responder.makeResponse(context, request);
    }

    @Test
    public void testStuff() throws Exception {
        assertEquals(303, response.getStatus());
        assertEquals("PageOne", response.getHeader("Location"));

        PageData data = page.getData();
        assertEquals("original content", data.getContent());
        assertEquals(true, data.hasAttribute("Edit"));
    }
}
