// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNeseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.testutil.SimpleCachingPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class VirtualCouplingExtensionTest extends FitnesseBaseTestCase {
    private FitNesseUtil fitNesseUtil;
    public WikiPage root;
    public BaseWikiPage page1;
    public WikiPage page2;
    private PageCrawler crawler;
    private FitNesseContext context;

    @Inject
    public void inject(FitNesseContext context, @Named(FitNeseModule.ROOT_PAGE) WikiPage root) {
        this.context = context;
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        fitNesseUtil = new FitNesseUtil();
        fitNesseUtil.startFitnesse(context);

        page2 = crawler.addPage(root, PathParser.parse("PageTwo"), "page two");
        crawler.addPage(page2, PathParser.parse("PageTwoChild"), "page two child");
        page1 = (BaseWikiPage) crawler.addPage(root, PathParser.parse("PageOne"), "page one content\n!contents\n");
        crawler.addPage(page1, PathParser.parse("SomeOtherPage"), "some other page");

        setVirtualWiki(page1, FitNesseUtil.URL + "PageTwo");
    }

    public static void setVirtualWiki(WikiPage page, String virtualWikiURL) throws Exception {
        PageData data = page.getData();
        data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, virtualWikiURL);
        page.commit(data);
    }

    @After
    public void tearDown() throws Exception {
        fitNesseUtil.stopFitnesse();
    }

    @Test
    public void testGetChildren() throws Exception {
        List<?> children = page1.getChildren();
        assertEquals(1, children.size());
        assertEquals("SomeOtherPage", ((WikiPage) children.get(0)).getName());

        VirtualCouplingExtension extension = (VirtualCouplingExtension) page1.getExtension(VirtualCouplingExtension.NAME);
        children = extension.getVirtualCoupling().getChildren();
        assertEquals(1, children.size());
        assertTrue(children.get(0) instanceof ProxyPage);
        assertEquals("PageTwoChild", ((WikiPage) children.get(0)).getName());
    }

    @Test
    public void testNewProxyChildrenAreFound() throws Exception {
        CachingPage.cacheTime = 0;
        BaseWikiPage realChild = (BaseWikiPage) page2.getChildPage("PageTwoChild");

        PageCrawler crawler = page2.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        ProxyPage childProxy = (ProxyPage) crawler.getPage(page1, PathParser.parse("PageTwoChild"));
        assertNull(childProxy.getChildPage("AnotherChild"));

        crawler.addPage(realChild, PathParser.parse("AnotherChild"), "another child");
        assertNotNull(childProxy.getChildPage("AnotherChild"));
    }

    @Test
    public void testProxyChildrenAreFoundOnStartUp() throws Exception {
        WikiPage page3 = crawler.addPage(root, PathParser.parse("PageThree"), "page three content");
        setVirtualWiki(page3, FitNesseUtil.URL + "PageTwo");

        assertTrue(page3.hasExtension(VirtualCouplingExtension.NAME));

        VirtualCouplingExtension extension = (VirtualCouplingExtension) page3.getExtension(VirtualCouplingExtension.NAME);
        List<?> children = extension.getVirtualCoupling().getChildren();
        assertEquals(1, children.size());
        assertEquals("PageTwoChild", ((WikiPage) children.get(0)).getName());
    }

    @Test
    public void testGetChildrenOnlyAsksOnce() throws Exception {
        CachingPage.cacheTime = 10000;
        ProxyPage.retrievalCount = 0;
        SimpleCachingPage page = new SimpleCachingPage("RooT", null, injector);
        setVirtualWiki(page, FitNesseUtil.URL + "PageTwo");
        VirtualCouplingExtension extension = (VirtualCouplingExtension) page.getExtension(VirtualCouplingExtension.NAME);
        extension.getVirtualCoupling().getChildren();
        assertEquals(1, ProxyPage.retrievalCount);
    }

    @Test
    public void testNoNastyExceptionIsThrownWhenVirtualChildrenAreLoaded() throws Exception {
        WikiPage page3 = crawler.addPage(root, PathParser.parse("PageThree"), "page three content");
        setVirtualWiki(page3, "http://dorothy.movealong.org/SomePage");
        VirtualCouplingExtension extension = (VirtualCouplingExtension) page3.getExtension(VirtualCouplingExtension.NAME);
        extension.getVirtualCoupling().getChildren();
        assertNotNull(page3.getChildPage("VirtualWikiNetworkError"));
    }

}
