// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.FitNesseUtil;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;

public class VirtualWikiWidgetTest extends WidgetTestCase {
    @Test
    public void testRegexp() throws Exception {
        assertMatch("!virtualwiki " + FitNesseUtil.URL + "SomePage");
        assertNoMatch("!virtualwiki SomeName");
    }

    @Test
    public void testPieces() throws Exception {
        String text = "!virtualwiki " + FitNesseUtil.URL + "SomePage.ChildPage";
        VirtualWikiWidget widget = new VirtualWikiWidget(new MockWidgetRoot(injector), text);
        assertEquals(FitNesseUtil.URL + "SomePage.ChildPage", widget.getRemoteUrl());
    }

    @Test
    public void testHtml() throws Exception {
        String text = "!virtualwiki " + FitNesseUtil.URL + "SomePage.ChildPage";
        VirtualWikiWidget widget = new VirtualWikiWidget(new MockWidgetRoot(injector), text);
        String html = widget.render();
        assertHasRegexp("deprecated", html);
    }

    protected String getRegexp() {
        return VirtualWikiWidget.REGEXP;
    }
}
