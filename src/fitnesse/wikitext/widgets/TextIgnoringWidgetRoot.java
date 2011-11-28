// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;

import java.io.IOException;

public class TextIgnoringWidgetRoot extends WidgetRoot {
    public static WidgetBuilder xrefWidgetBuilder = new WidgetBuilder(XRefWidget.class);

    public TextIgnoringWidgetRoot(String value, WikiPage page) throws IOException {
        super(value, page, xrefWidgetBuilder);
    }

    public void addChildWidgets(String value) {
        getBuilder().addChildWidgets(value, this, false);
    }

    @Override
    public boolean isIgnoringText() {
        return true;
    }
}

