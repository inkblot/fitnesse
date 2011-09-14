// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;


import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LiteralWidgetTest extends WidgetTestCase {
    @Test
    public void testMatches() throws Exception {  //Paren Literal: () -> ??
        assertMatch("!lit?0?");
        assertMatch("!lit?99?");
        assertNoMatch("!lit?-1?");
        assertNoMatch("!lit?a?");
    }

    protected String getRegexp() {
        return LiteralWidget.REGEXP;
    }

    @Test
    public void testWikiWordIsNotParsed() throws Exception {
        ParentWidget root = new MockWidgetRoot();
        root.defineLiteral("Bob");
        //Paren Literal: () -> ??
        LiteralWidget w = new LiteralWidget(root, "!lit?0?");
        String html = w.render();
        assertEquals("Bob", html);
    }
}
