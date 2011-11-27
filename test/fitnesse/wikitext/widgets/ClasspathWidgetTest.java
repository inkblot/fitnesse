// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;

public class ClasspathWidgetTest extends WidgetTestCase {
    public void testRegexp() throws Exception {
        assertMatchEquals("!path somePath", "!path somePath");
    }

    @Test
    public void testHtml() throws Exception {
        ClasspathWidget widget = new ClasspathWidget(new MockWidgetRoot(injector), "!path some.path");
        Pattern p = Pattern.compile("classpath: some.path");
        Matcher match = p.matcher(widget.render());
        assertTrue("pattern not found", match.find());
    }

    @Test
    public void testAsWikiText() throws Exception {
        final String PATH_WIDGET = "!path some.path";
        ClasspathWidget w = new ClasspathWidget(new MockWidgetRoot(injector), PATH_WIDGET);
        assertEquals(PATH_WIDGET, w.asWikiText());
    }

    @Test
    public void testPathWithVariable() throws Exception {
        String text = "!define BASE {/my/base/}\n!path ${BASE}*.jar\n";
        ParentWidget root = new SimpleWidgetRoot(text, new WikiPageDummy(injector));
        String html = root.render();
        assertSubString("/my/base/*.jar", html);
    }

    @Test
    public void testPathWikiTextWithVariable() throws Exception {
        String text = "!define BASE {/my/base/}\n!path ${BASE}*.jar\n";
        ParentWidget root = new SimpleWidgetRoot(text, new WikiPageDummy(injector));
        String text2 = root.asWikiText();
        assertSubString("!path ${BASE}*.jar", text2);
    }

    @Test
    public void testIsWidgetWithTextArgument() throws Exception {
        ClasspathWidget widget = new ClasspathWidget(new MockWidgetRoot(injector), "!path some.path");
        assertTrue(widget instanceof WidgetWithTextArgument);
    }

    protected String getRegexp() {
        return ClasspathWidget.REGEXP;
    }
}
