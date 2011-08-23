// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import junit.framework.TestCase;

import static util.RegexAssertions.assertMatches;

public class ItalicWidgetTest extends TestCase {
    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testRegexp() throws Exception {
        assertMatches(ItalicWidget.REGEXP, "''italic''");
        assertMatches(ItalicWidget.REGEXP, "'' 'italic' ''");
    }

    public void testItalicWidgetRendersHtmlItalics() throws Exception {
        ItalicWidget widget = new ItalicWidget(new MockWidgetRoot(), "''italic text''");
        assertEquals("<i>italic text</i>", widget.render());
    }
}
