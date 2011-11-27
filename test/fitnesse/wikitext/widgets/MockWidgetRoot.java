// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Injector;
import fitnesse.wiki.PagePointer;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wiki.WikiPagePath;

public class MockWidgetRoot extends WidgetRoot {
    public MockWidgetRoot(Injector injector) throws Exception {
        super(null, new PagePointer(new WikiPageDummy("RooT", injector), new WikiPagePath()).getPage());
    }

    protected void buildWidgets(String value) {
    }
}
