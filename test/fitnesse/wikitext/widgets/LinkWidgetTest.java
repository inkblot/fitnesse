// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertSubString;

public class LinkWidgetTest extends WidgetTestCase {
    @Test
    public void testRegexp() throws Exception {
        assertMatchEquals("http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.html", "http://www.objectmentor.com/resources/bookstore/books/PPPCoverIcon.html");
        assertMatchEquals("http://files/someFile", "http://files/someFile");
        assertMatchEquals("http://files", "http://files");
        assertMatchEquals("http://objectmentor.com", "http://objectmentor.com");
        assertMatchEquals("(http://objectmentor.com)", "http://objectmentor.com");
        assertMatchEquals("http://objectmentor.com.", "http://objectmentor.com");
        assertMatchEquals("(http://objectmentor.com).", "http://objectmentor.com");
        assertMatchEquals("https://objectmentor.com", "https://objectmentor.com");
    }

    @Test
    public void testHtml() throws Exception {
        LinkWidget widget = new LinkWidget(new MockWidgetRoot(injector), "http://host.com/file.html");
        assertEquals("<a href=\"http://host.com/file.html\">http://host.com/file.html</a>", widget.render());

        widget = new LinkWidget(new MockWidgetRoot(injector), "http://files/somePage");
        assertEquals("<a href=\"/files/somePage\">http://files/somePage</a>", widget.render());

        widget = new LinkWidget(new MockWidgetRoot(injector), "http://www.objectmentor.com");
        assertEquals("<a href=\"http://www.objectmentor.com\">http://www.objectmentor.com</a>", widget.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        final String LINK_TEXT = "http://xyz.com";
        LinkWidget widget = new LinkWidget(new MockWidgetRoot(injector), LINK_TEXT);
        assertEquals(LINK_TEXT, widget.asWikiText());
    }

    @Test
    public void testHttpsLink() throws Exception {
        String link = "https://link.com";
        LinkWidget widget = new LinkWidget(new MockWidgetRoot(injector), link);
        assertEquals("<a href=\"https://link.com\">https://link.com</a>", widget.render());
        assertEquals(link, widget.asWikiText());
    }

    @Test
    public void testLinkWikiWithVariable() throws Exception {
        String text = "!define HOST {somehost}\nhttp://www.${HOST}.com\n";
        ParentWidget root = new WidgetRoot(text, new WikiPageDummy(injector));
        assertSubString("<a href=\"http://www.somehost.com\">http://www.somehost.com</a>", root.render());
        assertEquals(text, root.asWikiText());
    }

    protected String getRegexp() {
        return LinkWidget.REGEXP;
    }
}
