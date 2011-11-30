// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

import java.io.IOException;

public class WhereUsedResponder extends ResultResponder {

    @Inject
    public WhereUsedResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, RunningTestingTracker runningTestingTracker, @Named(FitNesseModule.ENABLE_CHUNKING) boolean chunkingEnabled) {
        super(htmlPageFactory, root, runningTestingTracker, chunkingEnabled);
    }

    protected void startSearching(WikiPage root, WikiPage page) throws IOException {
        super.startSearching(root, page);
        new WhereUsedPageFinder(page, this).search(root);
    }

    protected String getTitle() {
        return "Where Used Results";
    }

}
