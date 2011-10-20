// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.components.WhereUsedPageFinder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class WhereUsedResponder extends ResultResponder {

    @Inject
    public WhereUsedResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        super(htmlPageFactory, root, context);
    }

    protected void startSearching(WikiPage root, WikiPage page) throws IOException {
        super.startSearching(root, page);
        new WhereUsedPageFinder(page, this).search(root);
    }

    protected String getTitle() {
        return "Where Used Results";
    }

}
