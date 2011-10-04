// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import com.google.inject.Inject;
import fitnesse.components.WhereUsedPageFinder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class WhereUsedResponder extends ResultResponder {

    @Inject
    public WhereUsedResponder(HtmlPageFactory htmlPageFactory) {
        super(htmlPageFactory);
    }

    protected void startSearching(WikiPage root) throws IOException {
        super.startSearching(root);
        new WhereUsedPageFinder(page, this).search(root);
    }

    protected String getTitle() {
        return "Where Used Results";
    }

}
