// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlElement;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HruleWidgetTest extends FitnesseBaseTestCase {
    private static String endl = HtmlElement.endl;

    @Test
    public void testRegexp() throws Exception {
        assertTrue("match1", Pattern.matches(HruleWidget.REGEXP, "----"));
        assertTrue("match2", Pattern.matches(HruleWidget.REGEXP, "------------------"));
        assertTrue("match3", !Pattern.matches(HruleWidget.REGEXP, "--- -"));
    }

    @Test
    public void testGetSize() throws Exception {
        HruleWidget widget = new HruleWidget(new MockWidgetRoot(injector), "----");
        assertEquals(0, widget.getExtraDashes());
        widget = new HruleWidget(new MockWidgetRoot(injector), "-----");
        assertEquals(1, widget.getExtraDashes());
        widget = new HruleWidget(new MockWidgetRoot(injector), "--------------");
        assertEquals(10, widget.getExtraDashes());
    }

    @Test
    public void testHtml() throws Exception {
        HruleWidget widget = new HruleWidget(new MockWidgetRoot(injector), "----");
        assertEquals("<hr/>" + endl, widget.render());
        widget = new HruleWidget(new MockWidgetRoot(injector), "------");
        assertEquals("<hr size=\"3\"/>" + endl, widget.render());
    }
}
