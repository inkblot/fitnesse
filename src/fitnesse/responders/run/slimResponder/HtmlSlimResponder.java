// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.slimResponder;

import fitnesse.html.HtmlPageFactory;
import fitnesse.wiki.PageData;
import util.Clock;

import java.util.Properties;

public class HtmlSlimResponder extends SlimResponder {
    public HtmlSlimResponder(Properties properties, HtmlPageFactory htmlPageFactory, Clock clock) {
        super(properties, htmlPageFactory, clock);
    }

    protected SlimTestSystem getTestSystem(PageData pageData) {
        return new HtmlSlimTestSystem(pageData.getWikiPage(), this);
    }
}
