// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommentWidgetTest extends FitnesseBaseTestCase {
    private ParentWidget root;

    @Before
    public void setUp() throws Exception {
        WikiPageDummy page = new WikiPageDummy(injector);
        root = new WidgetRoot(page);
    }

    @Test
    public void testRegexp() throws Exception {
        assertTrue("match1", Pattern.matches(CommentWidget.REGEXP, "# Comment text\n"));
        assertTrue("match2", Pattern.matches(CommentWidget.REGEXP, "#\n"));
        assertTrue("match3", !Pattern.matches(CommentWidget.REGEXP, " #\n"));
    }

    @Test
    public void testHtml() throws Exception {
        CommentWidget widget = new CommentWidget(root, "# some text\n");
        assertEquals("", widget.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        CommentWidget widget = new CommentWidget(root, "# some text\n");
        assertEquals("# some text\n", widget.asWikiText());
    }
}

