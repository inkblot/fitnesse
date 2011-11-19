// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.WikiPageResponder;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;
import static util.RegexAssertions.assertNotSubString;
import static util.RegexAssertions.assertSubString;

public class WikiImportPropertyTest extends FitnesseBaseTestCase {
    private WikiImportProperty property;
    private WikiPage page;

    private Clock clock;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(Clock clock, HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root, FitNesseUtil fitNesseUtil) {
        this.clock = clock;
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.fitNesseUtil = fitNesseUtil;
    }

    @Before
    public void setUp() {
        property = new WikiImportProperty("");
    }

    @Test
    public void testSource() throws Exception {
        property = new WikiImportProperty("import source");
        assertEquals("import source", property.getSourceUrl());
        assertEquals("import source", property.get("Source"));
    }

    @Test
    public void testIsRoot() throws Exception {
        assertFalse(property.isRoot());
        assertFalse(property.has("IsRoot"));

        property.setRoot(true);

        assertTrue(property.isRoot());
        assertTrue(property.has("IsRoot"));
    }

    @Test
    public void testAutoUpdate() throws Exception {
        assertFalse(property.isAutoUpdate());
        assertFalse(property.has("AutoUpdate"));

        property.setAutoUpdate(true);

        assertTrue(property.isAutoUpdate());
        assertTrue(property.has("AutoUpdate"));
    }

    @Test
    public void testLastUpdated() throws Exception {
        SimpleDateFormat format = WikiPageProperty.getTimeFormat();
        Date date = clock.currentClockDate();
        property.setLastRemoteModificationTime(date);

        assertEquals(format.format(date), format.format(property.getLastRemoteModificationTime()));

        assertEquals(format.format(date), property.get("LastRemoteModification"));
    }

    @Test
    public void testFailedCreateFromProperty() throws Exception {
        assertNull(WikiImportProperty.createFrom(new WikiPageProperty()));
    }

    @Test
    public void testCreateFromProperty() throws Exception {
        WikiPageProperty rawImportProperty = property.set(WikiImportProperty.PROPERTY_NAME);
        rawImportProperty.set("IsRoot");
        rawImportProperty.set("AutoUpdate");
        rawImportProperty.set("Source", "some source");
        Date date = clock.currentClockDate();
        rawImportProperty.set("LastRemoteModification", WikiPageProperty.getTimeFormat().format(date));

        WikiImportProperty importProperty = WikiImportProperty.createFrom(property);
        assertEquals("some source", importProperty.getSourceUrl());
        assertTrue(importProperty.isRoot());
        assertTrue(importProperty.isAutoUpdate());
        SimpleDateFormat format = WikiPageProperty.getTimeFormat();
        assertEquals(format.format(date), format.format(importProperty.getLastRemoteModificationTime()));
    }

    @Test
    public void testAddtoProperty() throws Exception {
        WikiImportProperty importProperty = new WikiImportProperty("some source");
        importProperty.setRoot(true);
        importProperty.setAutoUpdate(true);
        importProperty.addTo(property);

        WikiImportProperty importProperty2 = WikiImportProperty.createFrom(property);
        assertEquals("some source", importProperty2.getSourceUrl());
        assertTrue(importProperty2.isRoot());
        assertTrue(importProperty2.isAutoUpdate());
    }

    // Tests for the rendering of import specific page details
    private WikiPage root;
    private FitNesseUtil fitNesseUtil;
    private PageCrawler crawler;

    public void pageRenderingSetUp() throws Exception {
        crawler = root.getPageCrawler();
    }

    private SimpleResponse requestPage(String name) throws Exception {
        MockRequest request = new MockRequest();
        request.setResource(name);
        Responder responder = new WikiPageResponder(getProperties(), htmlPageFactory, clock, root);
        return (SimpleResponse) responder.makeResponse(request);
    }

    @Test
    public void testVirtualPageIndication() throws Exception {
        pageRenderingSetUp();

        WikiPage targetPage = crawler.addPage(root, PathParser.parse("TargetPage"));
        crawler.addPage(targetPage, PathParser.parse("ChildPage"));
        WikiPage linkPage = crawler.addPage(root, PathParser.parse("LinkPage"));
        VirtualCouplingExtensionTest.setVirtualWiki(linkPage, FitNesseUtil.URL + "TargetPage");

        fitNesseUtil.startFitnesse();
        SimpleResponse response = null;
        try {
            response = requestPage("LinkPage.ChildPage");
        } finally {
            fitNesseUtil.stopFitnesse();
        }

        assertSubString("<body class=\"virtual\">", response.getContent());
    }

    @Test
    public void testImportedPageIndication() throws Exception {
        pageRenderingSetUp();

        page = crawler.addPage(root, PathParser.parse("SamplePage"));
        PageData data = page.getData();
        WikiImportProperty importProperty = new WikiImportProperty("blah");
        importProperty.addTo(data.getProperties());
        page.commit(data);

        String content = getContentAfterSpecialImportHandling();

        assertSubString("<body class=\"imported\">", content);
    }

    @Test
    public void testEditActions() throws Exception {
        pageRenderingSetUp();

        page = crawler.addPage(root, PathParser.parse("SamplePage"));
        PageData data = page.getData();
        page.commit(data);
        String content = getContentAfterSpecialImportHandling();

        assertNotSubString("Edit Locally", content);
        assertNotSubString("Edit Remotely", content);

        WikiImportProperty importProperty = new WikiImportProperty("blah");
        importProperty.addTo(data.getProperties());
        page.commit(data);
        content = getContentAfterSpecialImportHandling();

        assertSubString("<a href=\"SamplePage?edit\" accesskey=\"e\">Edit Locally</a>", content);
        assertSubString("<a href=\"blah?responder=edit&amp;redirectToReferer=true&amp;redirectAction=importAndView\" accesskey=\"e\">Edit Remotely</a>", content);
    }

    private String getContentAfterSpecialImportHandling() throws Exception {
        HtmlPage html = new HtmlPageFactory().newPage();
        WikiImportProperty.handleImportProperties(html, page, page.getData());
        return html.html();
    }

}
