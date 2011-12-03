// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.http.Request;
import fitnesse.wiki.*;

import java.io.IOException;
import java.util.List;

public abstract class SecurePageOperation implements SecureOperation {
    protected abstract String getSecurityMode();

    public boolean shouldAuthenticate(WikiPage root, Request request) {
        try {
            WikiPagePath path = PathParser.parse(request.getResource());
            PageCrawler crawler = root.getPageCrawler();
            crawler.setDeadEndStrategy(new MockingPageCrawler());
            WikiPage page = crawler.getPage(root, path);
            if (page == null)
                return false;

            List<WikiPage> ancestors = WikiPageUtil.getAncestorsStartingWith(page);
            for (WikiPage ancestor : ancestors) {
                if (hasSecurityModeAttribute(ancestor))
                    return true;
            }
            return false;
        } catch (IOException e) {
            // when we can't determine whether the page is secure, be conservative
            return true;
        }
    }

    private boolean hasSecurityModeAttribute(WikiPage ancestor) throws IOException {
        PageData data = ancestor.getData();
        return data.hasAttribute(getSecurityMode());
    }
}
