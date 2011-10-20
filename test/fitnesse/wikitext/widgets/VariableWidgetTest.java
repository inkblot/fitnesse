// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.WidgetBuilder;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertSubString;

public class VariableWidgetTest extends WidgetTestCase {
    private WikiPage root;
    private PageCrawler crawler;
    private ParentWidget widgetRoot;

    @Inject
    public void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        WikiPage page = crawler.addPage(root, PathParser.parse("MyPage"));
        widgetRoot = new WidgetRoot("", page);
    }

    @Test
    public void testMatches() throws Exception {
        assertMatch("${X}");
        assertMatch("${xyz}");
        assertMatch("${x.y.z}");
        assertMatch("${.y.z}");
        assertMatch("${x.y.}");
        assertMatch("${.xy.}");
    }

    protected String getRegexp() {
        return VariableWidget.REGEXP;
    }

    @Test
    public void testVariableIsExpressed() throws Exception {
        widgetRoot.addVariable("x", "1");
        VariableWidget w = new VariableWidget(widgetRoot, "${x}");
        assertEquals("1", w.render());
    }

    @Test
    public void testVariableIsExpressedWithPeriods() throws Exception {
        widgetRoot.addVariable("x.y.z", "2");
        VariableWidget w = new VariableWidget(widgetRoot, "${x.y.z}");
        assertEquals("2", w.render());
    }

    @Test
    public void testRenderTwice() throws Exception {
        widgetRoot.addVariable("x", "1");
        VariableWidget w = new VariableWidget(widgetRoot, "${x}");
        assertEquals("1", w.render());
        assertEquals("1", w.render());
    }

    @Test
    public void testVariableInParentPage() throws Exception {
        WikiPage parent = crawler.addPage(root, PathParser.parse("ParentPage"), "!define var {zot}\n");
        WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "ick");

        ParentWidget widgetRoot = new WidgetRoot("", child, WidgetBuilder.htmlWidgetBuilder);
        VariableWidget w = new VariableWidget(widgetRoot, "${var}");
        assertEquals("zot", w.render());
    }

    @Test
    public void testVariableInParentPageCanReferenceVariableInChildPage() throws Exception {
        WikiPage parent = crawler.addPage(root, PathParser.parse("ParentPage"), "!define X (value=${Y})\n");
        WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "!define Y {child}\n${X}\n");

        String html = child.getData().getHtml();
        assertSubString("value=child", html);
    }

    @Test
    public void testUndefinedVariable() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("MyPage"));
        ParentWidget widgetRoot = new WidgetRoot("", page);
        VariableWidget w = new VariableWidget(widgetRoot, "${x}");
        assertSubString("undefined variable: x", w.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        VariableWidget w = new VariableWidget(widgetRoot, "${x}");
        assertEquals("${x}", w.asWikiText());
    }

    @Test
    public void testAsWikiTextWithPeriods() throws Exception {
        VariableWidget w = new VariableWidget(widgetRoot, "${x.y.z}");
        assertEquals("${x.y.z}", w.asWikiText());
    }

    @Test
    public void testLiteralsInheritedFromParent() throws Exception {
        WikiPage parent = crawler.addPage
                (root,
                        PathParser.parse("ParentPage"),
                        "!define var {!-some literal-!}\n" +
                                "!define paren (!-paren literal-!)\n"
                );
        WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "ick");
        ParentWidget widgetRoot = new WidgetRoot("", child, WidgetBuilder.htmlWidgetBuilder);

        VariableWidget w = new VariableWidget(widgetRoot, "${var}");
        assertEquals("some literal", w.render());
        w = new VariableWidget(widgetRoot, "${paren}");
        assertEquals("paren literal", w.render());
    }

    @Test
    public void testNestedVariable() throws Exception {
        WikiPage parent = crawler.addPage(root, PathParser.parse("ParentPage"), "!define var {zot}\n!define voo (x${var})\n");
        WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "ick");

        ParentWidget widgetRoot = new WidgetRoot("", child, WidgetBuilder.htmlWidgetBuilder);
        VariableWidget w = new VariableWidget(widgetRoot, "${voo}");
        assertEquals("xzot", w.render());
    }
}
