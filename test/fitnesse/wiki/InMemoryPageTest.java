// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class InMemoryPageTest extends WikiBaseTestCase {
    private WikiPage page1;
    private WikiPage page2;
    private WikiPage root;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        PageCrawler crawler = root.getPageCrawler();
        page1 = crawler.addPage(root, PathParser.parse("PageOne"), "page one");
        page2 = crawler.addPage(root, PathParser.parse("PageTwo"), "page two");
    }

    @Test
    public void testCommitUsesProperPageWhenCommittingFromOtherPage() throws Exception {
        PageData data = page1.getData();
        page2.commit(data);
        data = page2.getData();

        assertSame(page2, data.getWikiPage());
    }

    @Test
    public void testVersions() throws Exception {
        PageData data = page1.getData();
        data.setContent("version 1");
        page1.commit(data);
        data.setContent("version 2");
        page1.commit(data);

        data = page1.getData();
        Set<VersionInfo> versions = data.getVersions();

        assertEquals(3, versions.size());
    }

    @Test
    public void testVersionAuthor() throws Exception {
        PageData data = page1.getData();
        Set<VersionInfo> versions = data.getVersions();
        for (VersionInfo versionInfo : versions) {
            assertEquals("", versionInfo.getAuthor());
        }

        data.setAttribute(PageData.LAST_MODIFYING_USER, "Joe");
        page1.commit(data);
        page1.commit(data);

        data = page1.getData();
        versions = data.getVersions();
        boolean joeFound = false;
        for (VersionInfo versionInfo : versions) {
            if ("Joe".equals(versionInfo.getAuthor()))
                joeFound = true;
        }

        assertTrue(joeFound);
    }
}
