// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.Provider;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.testutil.FitNesseUtil;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class ProxyPageTest extends FitnesseBaseTestCase {
    private ProxyPage proxy;
    public WikiPage child1;
    private PageCrawler crawler;
    private FitNesseUtil fitNesseUtil;

    private Provider<Clock> clockProvider;

    @Inject
    public void inject(Provider<Clock> clockProvider) {
        this.clockProvider = clockProvider;
    }

    @Before
    public void setUp() throws Exception {
        CachingPage.cacheTime = 0;
        FitNesseContext context = makeContext();
        WikiPage root = context.root;
        crawler = root.getPageCrawler();
        WikiPagePath page1Path = PathParser.parse("PageOne");
        WikiPage original = crawler.addPage(root, page1Path, "page one content");
        child1 = crawler.addPage(original, PathParser.parse("ChildOne"), "child one");
        crawler.addPage(original, PathParser.parse("ChildTwo"), "child two");
        PageData data = original.getData();
        data.setAttribute("Attr1");
        original.commit(data);

        fitNesseUtil = new FitNesseUtil();
        fitNesseUtil.startFitnesse(context);

        proxy = new ProxyPage(original, injector);
        proxy.setTransientValues("localhost", clockProvider.get().currentClockTimeInMillis());
        proxy.setHostPort(FitNesseUtil.DEFAULT_PORT);
    }

    @After
    public void tearDown() throws Exception {
        fitNesseUtil.stopFitnesse();
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals("page one content", proxy.getData().getContent());
        assertEquals("PageOne", proxy.getName());
        assertEquals(true, proxy.getData().hasAttribute("Attr1"));
    }

    @Test
    public void testHasChildren() throws Exception {
        assertEquals(false, proxy.hasChildPage("BlaH"));
        assertEquals(true, proxy.hasChildPage("ChildOne"));
        assertEquals(true, proxy.hasChildPage("ChildTwo"));
    }

    @Test
    public void testGetChildrenOneAtATime() throws Exception {
        WikiPage child1 = proxy.getChildPage("ChildOne");
        assertEquals("child one", child1.getData().getContent());
        WikiPage child2 = proxy.getChildPage("ChildTwo");
        assertEquals("child two", child2.getData().getContent());
    }

    @Test
    public void testGetAllChildren() throws Exception {
        List<?> children = proxy.getChildren();
        assertEquals(2, children.size());
        WikiPage child = (WikiPage) children.get(0);
        assertEquals(true, "ChildOne".equals(child.getName()) || "ChildTwo".equals(child.getName()));
        child = (WikiPage) children.get(1);
        assertEquals(true, "ChildOne".equals(child.getName()) || "ChildTwo".equals(child.getName()));
    }

    @Test
    public void testSetHostAndPort() throws Exception {
        List<WikiPage> children = proxy.getChildren();
        proxy.setTransientValues("a.new.host", clockProvider.get().currentClockTimeInMillis());
        proxy.setHostPort(123);

        assertEquals("a.new.host", proxy.getHost());
        assertEquals(123, proxy.getHostPort());

        for (WikiPage child : children) {
            ProxyPage page = (ProxyPage) child;
            assertEquals("a.new.host", page.getHost());
            assertEquals(123, page.getHostPort());
        }
    }

    @Test
    public void testCanFindNewChildOfAProxy() throws Exception {
        ProxyPage child1Proxy = (ProxyPage) proxy.getChildPage("ChildOne");
        assertNull(child1Proxy.getChildPage("ChildOneChild"));

        crawler.addPage(child1, PathParser.parse("ChildOneChild"), "child one child");
        assertNotNull(child1Proxy.getChildPage("ChildOneChild"));
    }

    @Test
    public void testHasSubpageCallsLoadChildrenNoMoreThanNeeded() throws Exception {
        proxy.loadChildren();
        ProxyPage.retrievalCount = 0;
        proxy.hasChildPage("ChildTwo");
        assertEquals(0, ProxyPage.retrievalCount);
        proxy.hasChildPage("SomeMissingChild");
        assertEquals(1, ProxyPage.retrievalCount);
    }
}
