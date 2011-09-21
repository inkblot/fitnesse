// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class RecentChangesTest extends FitnesseBaseTestCase {
    private WikiPage rootPage;
    private WikiPage newPage;
    private WikiPage page1;
    private WikiPage page2;

    @Before
    public void setUp() throws Exception {
        rootPage = InMemoryPage.makeRoot("RooT", injector);
        newPage = rootPage.addChildPage("SomeNewPage");
        page1 = rootPage.addChildPage("PageOne");
        page2 = rootPage.addChildPage("PageTwo");
    }

    @Test
    public void testFirstRecentChange() throws Exception {
        assertEquals(false, rootPage.hasChildPage("RecentChanges"));
        RecentChanges.updateRecentChanges(newPage.getData());
        assertEquals(true, rootPage.hasChildPage("RecentChanges"));
        WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(1, lines.size());
        assertHasRegexp("SomeNewPage", lines.get(0));
    }

    @Test
    public void testTwoChanges() throws Exception {
        RecentChanges.updateRecentChanges(page1.getData());
        RecentChanges.updateRecentChanges(page2.getData());
        WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(2, lines.size());
        assertHasRegexp("PageTwo", lines.get(0));
        assertHasRegexp("PageOne", lines.get(1));
    }

    @Test
    public void testNoDuplicates() throws Exception {
        RecentChanges.updateRecentChanges(page1.getData());
        RecentChanges.updateRecentChanges(page1.getData());
        WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(1, lines.size());
        assertHasRegexp("PageOne", lines.get(0));
    }

    @Test
    public void testMaxSize() throws Exception {
        for (int i = 0; i < 101; i++) {
            StringBuilder b = new StringBuilder("LotsOfAs");
            for (int j = 0; j < i; j++)
                b.append("a");
            WikiPage page = rootPage.addChildPage(b.toString());
            RecentChanges.updateRecentChanges(page.getData());
        }

        WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(100, lines.size());
    }

    @Test
    public void testUsernameColumnWithoutUser() throws Exception {
        RecentChanges.updateRecentChanges(page1.getData());
        WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        String line = lines.get(0);
        assertSubString("|PageOne||", line);
    }

    @Test
    public void testUsernameColumnWithUser() throws Exception {
        PageData data = page1.getData();
        data.setAttribute(PageData.LAST_MODIFYING_USER, "Aladdin");
        page1.commit(data);

        RecentChanges.updateRecentChanges(page1.getData());
        WikiPage recentChanges = rootPage.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        String line = lines.get(0);
        assertSubString("|PageOne|Aladdin|", line);
    }
}
