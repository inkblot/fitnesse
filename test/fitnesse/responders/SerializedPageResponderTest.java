// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertNotSubString;
import static util.RegexAssertions.assertSubString;

public class SerializedPageResponderTest extends FitnesseBaseTestCase {
    private static final String ROOT_PAGE = "TestRooT";
    private PageCrawler crawler;
    private WikiPage root;
    private MockRequest request;
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        context = new FitNesseContext(ROOT_PAGE);
        root = context.root;
        crawler = root.getPageCrawler();
        request = new MockRequest();
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.deleteFileSystemDirectory(ROOT_PAGE);
    }

    @Test
    public void testWithInMemory() throws Exception {
        Object obj = doSetUpWith("bones");
        doTestWith(obj);

    }

    @Test
    public void testWithFileSystem() throws Exception {
        context = new FitNesseContext(new FileSystemPage(".", ROOT_PAGE), this.getRootPath());
        root = context.root;
        crawler = root.getPageCrawler();
        Object obj = doSetUpWith("bones");
        FileUtil.deleteFileSystemDirectory(ROOT_PAGE);
        doTestWith(obj);
    }

    private void doTestWith(Object obj) throws Exception {
        assertNotNull(obj);
        assertEquals(true, obj instanceof ProxyPage);
        WikiPage page = (WikiPage) obj;
        assertEquals("PageOne", page.getName());
    }

    private Object doSetUpWith(String proxyType) throws Exception {
        WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"), "this is page one");
        PageData data = page1.getData();
        data.setAttribute("Attr1", "true");
        page1.commit(data);
        crawler.addPage(page1, PathParser.parse("ChildOne"), "this is child one");

        request.addInput("type", proxyType);
        request.setResource("PageOne");

        return getObject();
    }

    private Object getObject() throws Exception {
        Responder responder = new SerializedPageResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(response.getContentBytes()));
        return ois.readObject();
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
