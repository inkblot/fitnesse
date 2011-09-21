// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static util.RegexAssertions.assertDoesNotHaveRegexp;

public class AliasLinkWidgetTest extends WidgetTestCase {
    private WikiPage root;
    private PageCrawler crawler;

    @Before
    public void setUp() throws Exception {
        root = InMemoryPage.makeRoot("RooT", injector);
        crawler = root.getPageCrawler();
    }

    @Test
    public void testMatches() throws Exception {
        assertMatch("[[tag][link]]");
        assertMatch("[[this is fun][http://www.objectmentor.com]]");
        assertMatch("[[x][x?test]]");
        assertMatch("[[x][x#anchor]]");
        assertNoMatch("[[this\nshould][not match]]");
        assertNoMatch("[[][]]");
        assertNoMatch("[[x][]");
        assertNoMatch("[[][x]");
        assertNoMatch("[[x] [x]]");
        assertNoMatch("[[x]]");
    }

    @Test
    public void testHtmlAtTopLevelPage() throws Exception {
        crawler.addPage(root, PathParser.parse("TestPage"));
        ParentWidget wroot = new WidgetRoot(new PagePointer(root, PathParser.parse("TestPage")));
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][TestPage]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage\">tag</a>", html);
    }

    @Test
    public void testHtmlAtTopLevelPageWithQuestionMark() throws Exception {
        crawler.addPage(root, PathParser.parse("TestPage"));
        ParentWidget wroot = new WidgetRoot(new PagePointer(root, PathParser.parse("TestPage")));
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][TestPage?test]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage?test\">tag</a>", html);
    }

    @Test
    public void testHtmlAtTopLevelPageWithAnchor() throws Exception {
        crawler.addPage(root, PathParser.parse("TestPage"));
        ParentWidget wroot = new WidgetRoot(new PagePointer(root, PathParser.parse("TestPage")));
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][TestPage#anchor]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage#anchor\">tag</a>", html);
    }

    @Test
    public void testHtmlOnSubPage() throws Exception {
        crawler.addPage(root, PathParser.parse("ParenT"), "Content");
        WikiPage parent = root.getChildPage("ParenT");
        crawler.addPage(parent, PathParser.parse("ChilD"), "ChilD");
        crawler.addPage(parent, PathParser.parse("ChildTwo"), "ChildTwo");
        WikiPage child = parent.getChildPage("ChilD");
        ParentWidget parentWidget = new WidgetRoot(new PagePointer(root, PathParser.parse("ParenT.ChilD")));
        AliasLinkWidget w = new AliasLinkWidget(parentWidget, "[[tag][ChildTwo]]");
        assertEquals("<a href=\"ParenT.ChildTwo\">tag</a>", w.render());
        AliasLinkWidget w2 = new AliasLinkWidget(new WidgetRoot(child), "[[tag][.ParenT]]");
        assertEquals("<a href=\"ParenT\">tag</a>", w2.render());
    }

    @Test
    public void testHtmlForPageThatDoesNotExist() throws Exception {
        crawler.addPage(root, PathParser.parse("FrontPage"));
        ParentWidget parentWidget = new WidgetRoot(new PagePointer(root, PathParser.parse("FrontPage")));
        AliasLinkWidget w = new AliasLinkWidget(parentWidget, "[[tag][TestPage]]");
        assertEquals("tag<a title=\"create page\" href=\"TestPage?edit&amp;nonExistent=true\">[?]</a>", w.render());
    }

    @Test
    public void testUparrowOnPageThatDoesNotExist() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("FrontPage"));
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(page), "[[tag][^TestPage]]");
        assertEquals("tag<a title=\"create page\" href=\"FrontPage.TestPage?edit&amp;nonExistent=true\">[?]</a>", w.render());
    }

    @Test
    public void testUparrowOnPageThatDoesExist() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
        crawler.addPage(page, PathParser.parse("SubPage"));
        ParentWidget wroot = new WidgetRoot(page);
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][^SubPage]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage.SubPage\">tag</a>", html);
    }

    @Test
    public void testSpacePaddingOnAliasLink() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"));
        crawler.addPage(page, PathParser.parse("SubPage"));
        ParentWidget wroot = new WidgetRoot(page);
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[tag][ ^SubPage ]]");
        String html = w.render();
        assertEquals("<a href=\"TestPage.SubPage\">tag</a>", html);
    }

    @Test
    public void testVariableIsRenderedInAliasLink() throws Exception {
        ParentWidget wroot = new WidgetRoot(root);
        wroot.addVariable("X", "Y");
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[x][${X}]]");
        assertEquals("<a href=\"Y\">x</a>", w.render());
    }

    @Test
    public void testVariableIsRenderedInAliasTag() throws Exception {
        ParentWidget wroot = new WidgetRoot(root);
        wroot.addVariable("X", "Y");
        AliasLinkWidget w = new AliasLinkWidget(wroot, "[[${X}][x]]");
        assertEquals("<a href=\"x\">Y</a>", w.render());
    }

    @Test
    public void testStandardLink() throws Exception {
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), "[[x][http://a.com]]");
        assertEquals("<a href=\"http://a.com\">x</a>", w.render());
    }

    @Test
    public void testStandardLinkWithQuestionMark() throws Exception {
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), "[[x][http://a.com?zap]]");
        assertEquals("<a href=\"http://a.com?zap\">x</a>", w.render());
    }

    @Test
    public void testStandardLinkWithAnchor() throws Exception {
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), "[[x][http://a.com#zap]]");
        assertEquals("<a href=\"http://a.com#zap\">x</a>", w.render());
    }

    @Test
    public void testQuestionMarkDoesNotAppear() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("FrontPage"));
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(page), "[[here][http://www.objectmentor.com/FitNesse/fitnesse.zip]]");
        assertDoesNotHaveRegexp("[?]", w.render());
    }

    protected String getRegexp() {
        return AliasLinkWidget.REGEXP;
    }

    @Test
    public void testUsageOnRootPageDoesntCrash() throws Exception {
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), "[[here][PageOne]]");
        try {
            w.render();
        } catch (Exception e) {
            fail("should not throw Exception: " + e);
        }
    }

    @Test
    public void testAsWikiText() throws Exception {
        String ALIAS_LINK = "[[this][that]]";
        AliasLinkWidget w = new AliasLinkWidget(new WidgetRoot(root), ALIAS_LINK);
        assertEquals(ALIAS_LINK, w.asWikiText());
    }

    @Test
    public void testLinkToNonExistentWikiPageOnVirtualPage() throws Exception {
        // When a virtual page contains a link to a non-existent page, the ? should
        // issue an edit request to the remote machine

        ProxyPage virtualPage = new ProxyPage("VirtualPage", root, "host", 9999, PathParser.parse("RealPage.VirtualPage"), injector);
        AliasLinkWidget widget = new AliasLinkWidget(new WidgetRoot(virtualPage), "[[link][NonExistentPage]]");
        assertEquals("link<a title=\"create page\" href=\"http://host:9999/RealPage.NonExistentPage?edit&amp;nonExistent=true\" target=\"NonExistentPage\">[?]</a>", widget.render());
    }
}
