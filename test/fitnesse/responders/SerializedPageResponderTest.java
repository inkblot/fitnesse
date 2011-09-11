// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import junit.framework.TestCase;
import util.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import static util.RegexAssertions.assertNotSubString;
import static util.RegexAssertions.assertSubString;

public class SerializedPageResponderTest extends TestCase {
    private final String RootPath = "TestRooT";
    private PageCrawler crawler;
    private WikiPage root;
    private MockRequest request;
    private FitNesseContext context;

    public SerializedPageResponderTest() {
    }

    public void setUp() throws Exception {
        context = new FitNesseContext("RooT");
        root = context.root;
        crawler = root.getPageCrawler();
        request = new MockRequest();
    }

    public void tearDown() throws Exception {
        FileUtil.deleteFileSystemDirectory(RootPath);
    }

    public void testWithInMemory() throws Exception {
        Object obj = doSetUpWith("bones");
        doTestWith(obj);

    }

    public void testWithFileSystem() throws Exception {
        context = new FitNesseContext(new FileSystemPage(".", RootPath));
        root = context.root;
        crawler = root.getPageCrawler();
        Object obj = doSetUpWith("bones");
        FileUtil.deleteFileSystemDirectory(RootPath);
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

    public void testGetContentAndAttributes() throws Exception {
        Object obj = doSetUpWith("meat");
        assertNotNull(obj);
        assertTrue(obj instanceof PageData);
        PageData data = (PageData) obj;

        assertEquals("this is page one", data.getContent());

        WikiPageProperties props = data.getProperties();
        assertTrue(props.has("Attr1"));
    }

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
