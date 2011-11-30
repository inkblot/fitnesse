// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class MockingPageCrawlerTest extends WikiBaseTestCase {
    private WikiPage root;
    private PageCrawler crawler;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        crawler.setDeadEndStrategy(new MockingPageCrawler());
    }

    @Test
    public void testGetMockPageSimple() throws Exception {
        WikiPagePath pageOnePath = PathParser.parse("PageOne");
        WikiPage mockPage = crawler.getPage(root, pageOnePath);
        assertNotNull(mockPage);
        assertTrue(mockPage instanceof WikiPageDummy);
        assertEquals("PageOne", mockPage.getName());
    }

    @Test
    public void testGetMockPageMoreComplex() throws Exception {
        WikiPagePath otherPagePath = PathParser.parse("PageOne.SomePage.OtherPage");
        WikiPage mockPage = crawler.getPage(root, otherPagePath);
        assertNotNull(mockPage);
        assertTrue(mockPage instanceof WikiPageDummy);
        assertEquals("OtherPage", mockPage.getName());
    }
}
