// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.components.WhereUsedPageFinder;

import java.io.IOException;

public class WhereUsedResponder extends ResultResponder {

    protected void startSearching() throws IOException {
        super.startSearching();
        new WhereUsedPageFinder(page, this).search(root);
    }

    protected String getTitle() {
        return "Where Used Results";
    }

}
