// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlElement;
import fitnesse.html.HtmlTag;
import fitnesse.html.RawHtml;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.apache.commons.lang.StringUtils.isEmpty;
import static org.junit.Assert.*;
import static util.RegexAssertions.*;

public class CollapsableWidgetTest extends WidgetTestCase {
    @Test
    public void testRegExp() throws Exception {
        assertMatch("!* Some title\n content \n*!");
        assertMatch("!*> Some title\n content \n*!");
        assertMatch("!********** Some title\n content \n**************!");

        assertNoMatch("!* title content *!");
        assertNoMatch("!*missing a space\n content \n*!");
        assertNoMatch("!* Some title\n content *!\n");
        assertNoMatch("!* Some title\n content *!...");

        //invisible: Matches
        assertMatch("!*< Some title\n content \n*!");
        assertMatch("!***< Some title\n content \n***!");
    }

    protected String getRegexp() {
        return CollapsableWidget.REGEXP;
    }

    @Test
    public void testRender() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector), "!* title\ncontent\n*!");
        String html = widget.render();
        assertSubString("title", html);
        assertSubString("content", html);
        assertSubString("collapsableOpen.gif", html);
        assertSubString("<a href=\"javascript:expandAll();\">Expand All</a>", html);
        assertSubString("<a href=\"javascript:collapseAll();\">Collapse All</a>", html);
    }

    //invisible: Test invisible too
    @Test
    public void testExpandedOrCollapsedOrInvisible() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector), "!* title\ncontent\n*!");
        assertTrue(widget.expanded);
        assertFalse(widget.invisible);

        widget = new CollapsableWidget(new MockWidgetRoot(injector), "!*> title\ncontent\n*!");
        assertFalse(widget.expanded);
        assertFalse(widget.invisible);

        //invisible: Test invisible flags
        widget = new CollapsableWidget(new MockWidgetRoot(injector), "!*< title\ncontent\n*!");
        assertFalse(widget.expanded);
        assertTrue(widget.invisible);
    }

    @Test
    public void testRenderCollapsedSection() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector), "!*> title\ncontent\n*!");
        String html = widget.render();
        assertSubString("class=\"hidden\"", html);
        assertNotSubString("class=\"collapsable\"", html);
        assertSubString("collapsableClosed.gif", html);
    }

    //invisible: Test invisible class
    @Test
    public void testRenderInvisibleSection() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector), "!*< title\ncontent\n*!\n");
        String html = widget.render();
        assertSubString("class=\"invisible\"", html);
        assertNotSubString("class=\"collapsable\"", html);
    }

    @Test
    public void testTwoCollapsableSections() throws Exception {
        String text = "!* section1\nsection1 content\n*!\n" +
                "!* section2\nsection2 content\n*!\n";
        ParentWidget widgetRoot = new SimpleWidgetRoot(text, new WikiPageDummy(injector));
        String html = widgetRoot.render();
        assertSubString("<span class=\"meta\">section1", html);
        assertSubString("<span class=\"meta\">section2", html);
    }

    @Test
    public void testEatsNewlineAtEnd() throws Exception {
        String text = "!* section1\nsection1 content\n*!\n";
        ParentWidget widgetRoot = new SimpleWidgetRoot(text, new WikiPageDummy(injector));
        String html = widgetRoot.render();
        assertNotSubString("<br/>", html);
    }

    @Test
    public void testMakeCollapsableSection() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector));
        HtmlTag outerTag = widget.makeCollapsableSection(new RawHtml("title"), new RawHtml("content"));
        assertEquals("div", outerTag.tagName());
        assertEquals("collapse_rim", outerTag.getAttribute("class"));

        List<?> childTags = removeNewlineTags(outerTag);

        HtmlTag collapseAllLinksDiv = (HtmlTag) childTags.get(0);
        assertEquals("div", collapseAllLinksDiv.tagName());

        HtmlTag anchor = (HtmlTag) childTags.get(1);
        assertEquals("a", anchor.tagName());

        HtmlElement title = (HtmlElement) childTags.get(2);
        assertEquals("title", title.html());

        HtmlTag contentDiv = (HtmlTag) childTags.get(3);
        assertEquals("div", contentDiv.tagName());
        assertEquals("collapsable", contentDiv.getAttribute("class"));

        HtmlElement content = (HtmlElement) removeNewlineTags(contentDiv).get(0);
        assertEquals("content", content.html());
    }

    @Test
    public void testWeirdBugThatUncleBobEncountered() throws Exception {
        try {
            new CollapsableWidget(new MockWidgetRoot(injector), "!* Title\n * list element\n*!\n");
            new CollapsableWidget(new MockWidgetRoot(injector), "!* Title\n * list element\r\n*!\n");
        } catch (Exception e) {
            e.printStackTrace();
            fail("no exception expected." + e.getMessage());
        }
    }

    @Test
    public void testEditLinkSuppressedWhenWidgetBuilderConstructorIsUsed() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector), "!* title\ncontent\n*!");
        String html = widget.render();
        assertDoesNotHaveRegexp("^.*href.*edit.*$", html);
    }

    @Test
    public void testEditLinkIncludedWhenOtherConstructorsAreUsed() throws Exception {
        CollapsableWidget widget = new CollapsableWidget(new MockWidgetRoot(injector), new MockWidgetRoot(injector),
                "title", "!* title\ncontent\n*!", "include", false);
        String html = widget.render();
        assertHasRegexp("^.*href.*edit.*$", html);
    }


    private List<?> removeNewlineTags(HtmlTag tag) throws Exception {
        List<?> childTags = new LinkedList<Object>(tag.childTags);
        for (Iterator<?> iterator = childTags.iterator(); iterator.hasNext(); ) {
            HtmlElement element = (HtmlElement) iterator.next();
            if (isEmpty(element.html().trim()))
                iterator.remove();
        }
        return childTags;
    }
}
