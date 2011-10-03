// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.PageData;

public class HtmlSlimResponder extends SlimResponder {
    public HtmlSlimResponder(HtmlPageFactory htmlPageFactory) {
        super(htmlPageFactory);
    }

    protected SlimTestSystem getTestSystem(PageData pageData) {
        return new HtmlSlimTestSystem(pageData.getWikiPage(), this);
    }
}
