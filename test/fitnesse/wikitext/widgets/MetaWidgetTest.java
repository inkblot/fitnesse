// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertMatches;

public class MetaWidgetTest extends WidgetTestCase {
    @Test
    public void testRegexp() throws Exception {
        assertMatches(MetaWidget.REGEXP, "!meta some string");
        assertMatches(MetaWidget.REGEXP, "!meta '''BoldWikiWord'''");
    }

    @Test
    public void testItalicWidgetRendersHtmlItalics() throws Exception {
        MetaWidget widget = new MetaWidget(new MockWidgetRoot(injector), "!meta text");
        assertEquals("<span class=\"meta\">text</span>", widget.render());
    }

    protected String getRegexp() {
        return MetaWidget.REGEXP;
    }

}
