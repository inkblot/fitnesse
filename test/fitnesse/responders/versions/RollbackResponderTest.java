// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RollbackResponderTest extends FitnesseBaseTestCase {
    private WikiPage page;
    private HtmlPageFactory htmlPageFactory;
    private WikiPage root;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        page = root.getPageCrawler().addPage(root, PathParser.parse("PageOne"), "original content");
    }

    @Test
    public void testStuff() throws Exception {
        PageData data = page.getData();
        data.setContent("new stuff");
        data.setProperties(new WikiPageProperties());
        VersionInfo commitRecord = page.commit(data);

        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        request.addInput("version", commitRecord.getName());

        Responder responder = new RollbackResponder(htmlPageFactory, root);
        Response response = responder.makeResponse(request);

        assertEquals(303, response.getStatus());
        assertEquals("PageOne", response.getHeader("Location"));

        data = page.getData();
        assertEquals("original content", data.getContent());
        assertEquals(true, data.hasAttribute("Edit"));
    }
}
