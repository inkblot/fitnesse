// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class PageCreatorTest extends FitnesseBaseTestCase {
    @Before
    public void setUp() throws Exception {
        FitnesseFixtureContext.root = InMemoryPage.makeRoot("root", injector);
    }

    @Test
    public void testCreatePage() throws Exception {
        WikiPage testPage = makePage("TestPage", "contents", "attr=val");
        assertNotNull(testPage);
        PageData data = testPage.getData();
        assertEquals("contents", data.getContent());
        assertEquals("val", data.getAttribute("attr"));
    }

    private WikiPage makePage(String pageName, String pageContent, String pageAttributes) throws Exception {
        PageCreator creator = new PageCreator();
        creator.setPageName(pageName);
        creator.setPageContents(pageContent);
        creator.setPageAttributes(pageAttributes);
        assertTrue(creator.valid());
        return FitnesseFixtureContext.root.getChildPage("TestPage");
    }

    @Test
    public void testMultipleAttributes() throws Exception {
        WikiPage testPage = makePage("TestPage", "Contents", "att1=one,att2=two");
        PageData data = testPage.getData();
        assertEquals("one", data.getAttribute("att1"));
        assertEquals("two", data.getAttribute("att2"));
    }
}
