// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.ColumnFixture;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.SimpleWidgetRoot;

public class RenderFixture extends ColumnFixture {
    public String text;

    public String rendered() throws Exception {
        ParentWidget root = new SimpleWidgetRoot(text, FitnesseFixtureContext.root);
        return root.render();
    }

}
