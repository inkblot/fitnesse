// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.components.TraversalListener;

import java.io.IOException;

//TODO after extracting the WikiPageModel... rethink this class.  Lots of these methods might be able to go back into WikiPAge.
public interface PageCrawler {
    WikiPage getPage(WikiPage context, WikiPagePath path) throws IOException;

    void setDeadEndStrategy(PageCrawlerDeadEndStrategy strategy);

    boolean pageExists(WikiPage context, WikiPagePath path) throws IOException;

    WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath);

    WikiPagePath getFullPath(WikiPage page);

    WikiPage addPage(WikiPage context, WikiPagePath path, String content) throws IOException;

    WikiPage addPage(WikiPage context, WikiPagePath path) throws IOException;

    String getRelativeName(WikiPage base, WikiPage page);

    boolean isRoot(WikiPage page);

    WikiPage getRoot(WikiPage page);

    void traverse(WikiPage root, TraversalListener pageCrawlerTest) throws IOException;

    WikiPage getSiblingPage(WikiPage page, WikiPagePath pathRelativeToSibling) throws IOException;

    WikiPage findAncestorWithName(WikiPage page, String name);
}