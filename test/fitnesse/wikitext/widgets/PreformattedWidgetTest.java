// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;

public class PreformattedWidgetTest extends FitnesseBaseTestCase {
    @Test
    public void testRegexp() throws Exception {
        Pattern pattern = Pattern.compile(PreformattedWidget.REGEXP, Pattern.DOTALL);
        assertTrue("match1", pattern.matcher("{{{preformatted}}}").matches());
        assertTrue("match2", pattern.matcher("{{{{preformatted}}}}").matches());
        assertFalse("match3", pattern.matcher("{{ {not preformatted}}}").matches());
        assertTrue("match4", pattern.matcher("{{{\npreformatted\n}}}").matches());
    }

    @Test
    public void testHtml() throws Exception {
        PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(injector), "{{{preformatted text}}}");
        assertEquals("<pre>preformatted text</pre>", widget.render());
    }

    @Test
    public void testMultiLine() throws Exception {
        PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(injector), "{{{\npreformatted text\n}}}");
        assertEquals("<pre><br/>preformatted text<br/></pre>", widget.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(injector), "{{{preformatted text}}}");
        assertEquals("{{{preformatted text}}}", widget.asWikiText());
    }

    @Test
    public void testThatLiteralsWorkInPreformattedText() throws Exception {
        ParentWidget root = new SimpleWidgetRoot("{{{abc !-123-! xyz}}}", new WikiPageDummy(injector));
        String text = root.render();
        assertEquals("<pre>abc 123 xyz</pre>", text);
    }

    @Test
    public void testThatVariablesWorkInPreformattedText() throws Exception {
        ParentWidget root = new SimpleWidgetRoot("!define X {123}\n{{{abc ${X} xyz}}}", new WikiPageDummy(injector));
        String text = root.render();
        assertSubString("<pre>abc 123 xyz</pre>", text);
    }
}
