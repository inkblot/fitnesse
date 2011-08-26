// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.components.WhereUsedPageFinder;

public class WhereUsedResponder extends ResultResponder {

    protected void startSearching() throws Exception {
        super.startSearching();
        new WhereUsedPageFinder(page, this).search(root);
    }

    protected String getTitle() throws Exception {
        return "Where Used Results";
    }

}
