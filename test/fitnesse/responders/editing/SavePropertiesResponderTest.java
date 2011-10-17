// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SavePropertiesResponderTest extends SingleContextBaseTestCase {
    private WikiPage root;

    private MockRequest request;
    private WikiPage page;
    private PageCrawler crawler;
    private VirtualCouplingExtension extension;
    private WikiPage linker;
    private FitNesseContext context;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.context = context;
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
    }

    private void createRequest() throws Exception {
        page = crawler.addPage(root, PathParser.parse("PageOne"));

        request = new MockRequest();
        request.addInput("PageType", "Test");
        request.addInput("Properties", "on");
        request.addInput("Search", "on");
        request.addInput("RecentChanges", "on");
        request.addInput(PageData.PropertySECURE_READ, "on");
        request.addInput(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://www.fitnesse.org");
        request.addInput("Suites", "Suite A, Suite B");
        request.addInput("HelpText", "Help text literal");
        request.setResource("PageOne");
    }

    @Test
    public void testClearChildrenWhenVWisCleared() throws Exception {
        createSimpleVirtualLink();

        // new request to get rid of the virtual wiki link
        SavePropertiesResponder responder = new SavePropertiesResponder(htmlPageFactory);
        request = new MockRequest();
        request.addInput(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "");
        request.setResource("LinkerPage");
        responder.makeResponse(context, request);
        assertEquals(0, linker.getChildren().size());
    }

    private WikiPage createSimpleVirtualLink() throws Exception {
        WikiPage linkee = crawler.addPage(root, PathParser.parse("LinkeePage"));
        crawler.addPage(linkee, PathParser.parse("ChildPageOne"));
        WikiPage linkee2 = crawler.addPage(root, PathParser.parse("LinkeePageTwo"));
        crawler.addPage(linkee2, PathParser.parse("ChildPageTwo"));
        linker = crawler.addPage(root, PathParser.parse("LinkerPage"));
        FitNesseUtil.bindVirtualLinkToPage(linker, linkee);

        extension = (VirtualCouplingExtension) linker.getExtension(VirtualCouplingExtension.NAME);
        List<?> children = extension.getVirtualCoupling().getChildren();
        assertEquals(1, children.size());
        WikiPage child = (WikiPage) children.get(0);
        assertEquals("ChildPageOne", child.getName());
        return linker;
    }

    @Test
    public void testClearChildrenChangingVW() throws Exception {
        createSimpleVirtualLink();
        assertTrue(!(extension.getVirtualCoupling() instanceof NullVirtualCouplingPage));

        // new request to get rid of the virtual wiki link
        SavePropertiesResponder responder = new SavePropertiesResponder(htmlPageFactory);
        request = new MockRequest();
        request.addInput(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, FitNesseUtil.URL + "LinkeePageTwo");
        request.setResource("LinkerPage");
        responder.makeResponse(context, request);

        assertTrue(extension.getVirtualCoupling() instanceof NullVirtualCouplingPage);
    }

    @Test
    public void testResponse() throws Exception {
        createRequest();

        Responder responder = new SavePropertiesResponder(htmlPageFactory);
        Response response = responder.makeResponse(context, request);

        PageData data = page.getData();
        assertEquals("http://www.fitnesse.org", data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE));
        assertTrue(data.hasAttribute("Test"));
        assertTrue(data.hasAttribute("Properties"));
        assertTrue(data.hasAttribute("Search"));
        assertFalse(data.hasAttribute("Edit"));
        assertTrue(data.hasAttribute("RecentChanges"));
        assertTrue(data.hasAttribute(PageData.PropertySECURE_READ));
        assertFalse(data.hasAttribute(PageData.PropertySECURE_WRITE));
        assertEquals("Suite A, Suite B", data.getAttribute(PageData.PropertySUITES));
        assertEquals("Help text literal", data.getAttribute(PageData.PropertyHELP));

        assertEquals(303, response.getStatus());
        assertEquals("PageOne", response.getHeader("Location"));
    }

}
