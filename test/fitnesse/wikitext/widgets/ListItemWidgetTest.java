// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.SingleContextBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ListItemWidgetTest extends SingleContextBaseTestCase {
    @Test
    public void testHtml() throws Exception {
        ListItemWidget widget = new ListItemWidget(new MockWidgetRoot(injector), "some text", 0);
        assertEquals("<li>some text</li>\n", widget.render());
        widget = new ListItemWidget(new MockWidgetRoot(injector), "some text", 3);
        assertEquals("\t\t\t<li>some text</li>\n", widget.render());
    }
}
