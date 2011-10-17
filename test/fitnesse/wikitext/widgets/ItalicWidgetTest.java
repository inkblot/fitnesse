// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.SingleContextBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertMatches;

public class ItalicWidgetTest extends SingleContextBaseTestCase {
    @Test
    public void testRegexp() throws Exception {
        assertMatches(ItalicWidget.REGEXP, "''italic''");
        assertMatches(ItalicWidget.REGEXP, "'' 'italic' ''");
    }

    @Test
    public void testItalicWidgetRendersHtmlItalics() throws Exception {
        ItalicWidget widget = new ItalicWidget(new MockWidgetRoot(injector), "''italic text''");
        assertEquals("<i>italic text</i>", widget.render());
    }
}
