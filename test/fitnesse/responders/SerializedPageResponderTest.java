// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static util.RegexAssertions.assertNotSubString;
import static util.RegexAssertions.assertSubString;

public class SerializedPageResponderTest extends SerializedPageResponderTestCase {

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        root = context.root;
        crawler = root.getPageCrawler();
        request = new MockRequest();
    }

    @Test
    public void testWithInMemory() throws Exception {
        Object obj = doSetUpWith("bones");
        doTestWith(obj);
    }

    @Test
    public void testGetContentAndAttributes() throws Exception {
        Object obj = doSetUpWith("meat");
        assertNotNull(obj);
        assertTrue(obj instanceof PageData);
        PageData data = (PageData) obj;

        assertEquals("this is page one", data.getContent());

        WikiPageProperties props = data.getProperties();
        assertTrue(props.has("Attr1"));
    }

    @Test
    public void testGetVersionOfPageData() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"), "some content");
        VersionInfo commitRecord = page.commit(page.getData());

        request.addInput("type", "meat");
        request.addInput("version", commitRecord.getName());
        request.setResource("PageOne");

        Object obj = getObject();
        assertEquals(PageData.class, obj.getClass());
        PageData data = (PageData) obj;
        assertEquals("some content", data.getContent());
    }

    @Test
    public void testGetPageHieratchyAsXml() throws Exception {
        crawler.addPage(root, PathParser.parse("PageOne"));
        crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
        crawler.addPage(root, PathParser.parse("PageTwo"));

        request.setResource("root");
        request.addInput("type", "pages");
        Responder responder = new SerializedPageResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String xml = response.getContent();

        assertEquals("text/xml", response.getContentType());
        assertSubString("<name>PageOne</name>", xml);
        assertSubString("<name>PageTwo</name>", xml);
        assertSubString("<name>ChildOne</name>", xml);
    }

    @Test
    public void testGetPageHieratchyAsXmlDoesntContainSymbolicLinks() throws Exception {
        WikiPage pageOne = crawler.addPage(root, PathParser.parse("PageOne"));
        crawler.addPage(root, PathParser.parse("PageOne.ChildOne"));
        crawler.addPage(root, PathParser.parse("PageTwo"));

        PageData data = pageOne.getData();
        WikiPageProperties properties = data.getProperties();
        WikiPageProperty symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
        symLinks.set("SymPage", "PageTwo");
        pageOne.commit(data);

        request.setResource("root");
        request.addInput("type", "pages");
        Responder responder = new SerializedPageResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String xml = response.getContent();

        assertEquals("text/xml", response.getContentType());
        assertSubString("<name>PageOne</name>", xml);
        assertSubString("<name>PageTwo</name>", xml);
        assertSubString("<name>ChildOne</name>", xml);
        assertNotSubString("SymPage", xml);
    }

    @Test
    public void testGetDataAsHtml() throws Exception {
        crawler.addPage(root, PathParser.parse("TestPageOne"), "test page");

        request.setResource("TestPageOne");
        request.addInput("type", "data");
        Responder responder = new SerializedPageResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String xml = response.getContent();

        assertEquals("text/xml", response.getContentType());
        assertSubString("test page", xml);
        assertSubString("<Test", xml);
    }
}
