// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import java.io.IOException;
import java.util.LinkedList;

public class WikiPageUtil {
    public static LinkedList<WikiPage> getAncestorsOf(WikiPage page) {
        PageCrawler crawler = page.getPageCrawler();
        LinkedList<WikiPage> ancestors = new LinkedList<WikiPage>();
        WikiPage parent = page;
        do {
            parent = parent.getParent();
            ancestors.add(parent);
        } while (!crawler.isRoot(parent));

        return ancestors;
    }

    public static LinkedList<WikiPage> getAncestorsStartingWith(WikiPage page) {
        LinkedList<WikiPage> ancestors = getAncestorsOf(page);
        ancestors.addFirst(page);
        return ancestors;
    }

    public static void setPageContents(WikiPage page, String pageContents) throws IOException {
        PageData pageData = page.getData();
        pageData.setContent(pageContents);
        page.commit(pageData);
    }
}
