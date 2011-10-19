// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CenterWidgetTest extends FitnesseBaseTestCase {
    @Test
    public void testRegexp() throws Exception {
        assertTrue("match1", Pattern.matches(CenterWidget.REGEXP, "!c centered text\n"));
        assertTrue("match2", Pattern.matches(CenterWidget.REGEXP, "!C more text\n"));
        assertTrue("match3", !Pattern.matches(CenterWidget.REGEXP, "!ctext\n"));
        assertTrue("match4", Pattern.matches(CenterWidget.REGEXP, "!c text\n"));
        assertTrue("match5", !Pattern.matches(CenterWidget.REGEXP, " !c text\n"));
        assertTrue("match5", Pattern.matches(CenterWidget.REGEXP, "!c text"));
    }

    @Test
    public void testHtml() throws Exception {
        CenterWidget widget = new CenterWidget(new MockWidgetRoot(injector), "!c some text\n");
        assertEquals("<div class=\"centered\">some text</div>", widget.render());
    }
}
