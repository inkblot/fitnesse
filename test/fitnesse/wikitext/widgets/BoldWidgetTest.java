// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wikitext.WikiWidget;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BoldWidgetTest extends FitnesseBaseTestCase {

    @Test
    public void testRegexp() throws Exception {
        assertTrue(Pattern.matches(BoldWidget.REGEXP, "'''bold'''"));
        assertTrue(Pattern.matches(BoldWidget.REGEXP, "''''bold''''"));
        assertFalse(Pattern.matches(BoldWidget.REGEXP, "'' 'not bold' ''"));
    }

    @Test
    public void testBadConstruction() throws Exception {
        BoldWidget widget = new BoldWidget(new MockWidgetRoot(injector), "''''some text' '''");
        assertEquals(1, widget.numberOfChildren());
        WikiWidget child = widget.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("'some text' ", ((TextWidget) child).getText());
    }

    public void testHtml() throws Exception {
        BoldWidget widget = new BoldWidget(new MockWidgetRoot(injector), "'''bold text'''");
        assertEquals("<b>bold text</b>", widget.render());
    }

}
