// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.SingleContextBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TextWidgetTest extends SingleContextBaseTestCase {
    @Test
    public void testGetText() throws Exception {
        TextWidget widget = new TextWidget(new MockWidgetRoot(injector), "some text");
        assertEquals("some text", widget.getText());
    }

    @Test
    public void testHtml() throws Exception {
        TextWidget widget = new TextWidget(new MockWidgetRoot(injector), "some text");
        assertEquals("some text", widget.render());
    }

    @Test
    public void testSpecialEscapes() throws Exception {
        TextWidget widget = new TextWidget(new MockWidgetRoot(injector), "text &bang; &bar; &dollar;");
        assertEquals("text ! | $", widget.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        TextWidget widget = new TextWidget(new MockWidgetRoot(injector), "some text");
        assertEquals("some text", widget.asWikiText());
    }

    @Test
    public void testSpecialWikiCharsAsWikiText() throws Exception {
        TextWidget widget = new TextWidget(new MockWidgetRoot(injector), "text \\! \\$ \\|");
        assertEquals("text \\! \\$ \\|", widget.asWikiText());
    }

    @Test
    public void testNewLines() throws Exception {
        TextWidget widget = new TextWidget(new MockWidgetRoot(injector), "one\ntwo\rthree\r\n");
        assertEquals("one<br/>two<br/>three<br/>", widget.render());
    }

}
