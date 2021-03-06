// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

public class PageDataWikiPageResponder extends BasicWikiPageResponder {
    @Inject
    public PageDataWikiPageResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        super(htmlPageFactory, root);
    }

    protected String contentFrom(WikiPage requestedPage)
            throws Exception {
        return requestedPage.getData().getContent();
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
