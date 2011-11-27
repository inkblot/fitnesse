// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.io.IOException;
import java.util.List;

public class TextIgnoringWidgetRoot extends WidgetRoot {
    //Refactored for isGathering parameter.
    public TextIgnoringWidgetRoot(String value, WikiPage page, WidgetBuilder builder) throws IOException {
        super(value, page, builder, /*isGatheringInfo=*/ true);
    }

    public void addChildWidgets(String value) {
        getBuilder().addChildWidgets(value, this, false);
    }
}

