// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wikitext.WikiWidget;
import org.junit.Test;
import util.TimeMeasurement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

// created by Jason Sypher

public class StrikeWidgetTest extends WidgetTestCase {
    @Test
    public void testRegularExpression() throws Exception {
        assertMatchEquals("abc--123--def", "--123--");
        assertNoMatch("------");
    }

    @Test
    public void testOutput() throws Exception {
        StrikeWidget widget =
                new StrikeWidget(new MockWidgetRoot(injector), "--some text--");
        assertEquals(1, widget.numberOfChildren());
        WikiWidget child = widget.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("some text", ((TextWidget) child).getText());
        assertEquals("<span class=\"strike\">some text</span>", widget.render());
    }

    @Test
    public void testEmbeddedDashInStrikedText() throws Exception {
        StrikeWidget widget = new StrikeWidget(new MockWidgetRoot(injector), "--embedded-dash--");
        assertEquals(1, widget.numberOfChildren());
        WikiWidget child = widget.nextChild();
        assertEquals(TextWidget.class, child.getClass());
        assertEquals("embedded-dash", ((TextWidget) child).getText());
        assertEquals("<span class=\"strike\">embedded-dash</span>", widget.render());
    }

    @Test
    public void testEvilExponentialMatch() throws Exception {
        TimeMeasurement measurement = new TimeMeasurement().start();

        assertNoMatch("--1234567890123456789012");

        long duration = measurement.elapsed();
        assertTrue("took too long", duration < 1000);
    }

    protected String getRegexp() {
        return StrikeWidget.REGEXP;
    }

}
