// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNeseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class RecentChangesTest extends FitnesseBaseTestCase {
    private WikiPage newPage;
    private WikiPage page1;
    private WikiPage page2;
    private WikiPage root;

    @Inject
    public void inject(@Named(FitNeseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        newPage = root.addChildPage("SomeNewPage");
        page1 = root.addChildPage("PageOne");
        page2 = root.addChildPage("PageTwo");
    }

    @Test
    public void testFirstRecentChange() throws Exception {
        assertEquals(false, root.hasChildPage("RecentChanges"));
        RecentChanges.updateRecentChanges(newPage.getData());
        assertEquals(true, root.hasChildPage("RecentChanges"));
        WikiPage recentChanges = root.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(1, lines.size());
        assertHasRegexp("SomeNewPage", lines.get(0));
    }

    @Test
    public void testTwoChanges() throws Exception {
        RecentChanges.updateRecentChanges(page1.getData());
        RecentChanges.updateRecentChanges(page2.getData());
        WikiPage recentChanges = root.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(2, lines.size());
        assertHasRegexp("PageTwo", lines.get(0));
        assertHasRegexp("PageOne", lines.get(1));
    }

    @Test
    public void testNoDuplicates() throws Exception {
        RecentChanges.updateRecentChanges(page1.getData());
        RecentChanges.updateRecentChanges(page1.getData());
        WikiPage recentChanges = root.getChildPage("RecentChanges");
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
            WikiPage page = root.addChildPage(b.toString());
            RecentChanges.updateRecentChanges(page.getData());
        }

        WikiPage recentChanges = root.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        assertEquals(100, lines.size());
    }

    @Test
    public void testUsernameColumnWithoutUser() throws Exception {
        RecentChanges.updateRecentChanges(page1.getData());
        WikiPage recentChanges = root.getChildPage("RecentChanges");
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
        WikiPage recentChanges = root.getChildPage("RecentChanges");
        List<String> lines = RecentChanges.getRecentChangesLines(recentChanges.getData());
        String line = lines.get(0);
        assertSubString("|PageOne|Aladdin|", line);
    }
}
