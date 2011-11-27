// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;

public class XRefWidgetTest extends WidgetTestCase {
    private WidgetRoot wroot, croot;

    @Test
    public void testRegexp() throws Exception {
        assertMatchEquals("!see SomePage", "!see SomePage");
        assertMatchEquals("!see SomePage.SubPage", "!see SomePage.SubPage");
        assertMatchEquals("!see SomePage.SubPage junk", "!see SomePage.SubPage");

        //!see: Child and backward search matching
        assertMatchEquals("!see >SubPage junk", "!see >SubPage");
        assertMatchEquals("!see <SomeParent.SubPage junk", "!see <SomeParent.SubPage");
    }

    @Before
    public void setUp() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = root.getPageCrawler().addPage(root, PathParser.parse("SomePage"));
        WikiPage child = root.getPageCrawler().addPage(page, PathParser.parse("SomeChild"));
        /*child2*/
        root.getPageCrawler().addPage(page, PathParser.parse("SomeChild2"));

        wroot = new SimpleWidgetRoot(page);
        croot = new SimpleWidgetRoot(child);
    }

    @Test
    public void testHtml() throws Exception {
        XRefWidget widget = new XRefWidget(wroot, "!see SomePage");
        assertHasRegexp("<b>See: <a href=.*SomePage</a></b>", widget.render());

        widget = new XRefWidget(wroot, "!see NoPage");
        assertHasRegexp("<b>See: NoPage<a title=.* href=.*>\\[\\?\\]</a></b>", widget.render());

        //see: Left & right arrow testing
        widget = new XRefWidget(wroot, "!see >SomeChild");
        assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild.*SomeChild</a></b>", widget.render());

        widget = new XRefWidget(croot, "!see <SomePage.SomeChild2");
        assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild2.*SomePage.SomeChild2</a></b>", widget.render());

        //Regracing
        wroot.addVariable(WikiWordWidget.REGRACE_LINK, "true");
        widget = new XRefWidget(wroot, "!see SomePage");
        assertHasRegexp("<b>See: <a href=.*Some Page</a></b>", widget.render());

        widget = new XRefWidget(wroot, "!see NoPage");
        assertHasRegexp("<b>See: NoPage<a title=.* href=.*>\\[\\?\\]</a></b>", widget.render());

        widget = new XRefWidget(wroot, "!see >SomeChild");
        assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild.*&gt;Some Child</a></b>", widget.render());

        croot.addVariable(WikiWordWidget.REGRACE_LINK, "true");
        widget = new XRefWidget(croot, "!see <SomePage.SomeChild2");
        assertHasRegexp("<b>See: <a href=.*SomePage.SomeChild2.*&lt;Some Page .Some Child 2</a></b>", widget.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        final String TEST_WIDGET = "!see SomePage";
        XRefWidget w = new XRefWidget(wroot, TEST_WIDGET);
        assertEquals(TEST_WIDGET, w.asWikiText());
    }

    protected String getRegexp() {
        return XRefWidget.REGEXP;
    }
}
