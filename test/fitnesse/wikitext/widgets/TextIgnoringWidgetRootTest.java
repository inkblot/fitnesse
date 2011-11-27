// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TextIgnoringWidgetRootTest extends FitnesseBaseTestCase {
    @Test
    public void testNoTextWidgetAreCreated() throws Exception {
        String text = "Here is some text with '''bold''' and ''italics'' and a cross reference: \n!see SomeOtherPage";
        WikiPageDummy page = new WikiPageDummy("SomePage", text, injector);
        ParentWidget root = new TextIgnoringWidgetRoot(text, page);
        List<?> widgets = root.getChildren();
        assertEquals(1, widgets.size());
        assertTrue(widgets.get(0) instanceof XRefWidget);
    }

}
