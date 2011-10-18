// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContextModule;
import fitnesse.SingleContextBaseTestCase;
import fitnesse.wiki.*;
import fitnesse.wikitext.test.ParserTestHelper;
import org.junit.Before;
import org.junit.Test;
import util.RegexAssertions;

import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class WidgetRootTest extends SingleContextBaseTestCase {
    public static final int PORT = 9876;
    private FitNesseContext context;
    private WikiPage root;
    private PageCrawler crawler;

    @Override
    protected int getPort() {
        return PORT;
    }

    @Inject
    public void inject(@Named(FitNesseContextModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        this.root = root;
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
    }

    //PAGE_NAME: Test
    @Test
    public void testPageNameVariable() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"));
        PageData data = page.getData();
        assertEquals("SomePage", data.getVariable("PAGE_NAME"));
    }

    @Test
    public void testParentPageNameVariable() throws Exception {
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        final String ROOT_PAGE_NAME = "RootPage";
        WikiPage root = crawler.addPage(this.root, PathParser.parse(ROOT_PAGE_NAME));
        final String INCLUDED_PAGE_NAME = "IncludedPage";
        WikiPage includedPage = crawler.addPage(this.root, PathParser.parse(INCLUDED_PAGE_NAME));
        WidgetRoot widgetRoot = new WidgetRoot(root);
        WidgetRoot includedRoot = new WidgetRoot(includedPage, widgetRoot);
        PageData data = includedPage.getData();
        assertEquals(INCLUDED_PAGE_NAME, data.getVariable("PAGE_NAME"));
        assertEquals(INCLUDED_PAGE_NAME, includedRoot.getVariable("PAGE_NAME"));
        assertEquals(ROOT_PAGE_NAME, includedRoot.getVariable("RUNNING_PAGE_NAME"));
    }

    @Test
    public void testParentPageNameVariableWithNoParent() throws Exception {
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPage includedPage = crawler.addPage(root, PathParser.parse("IncludedPage"));
        WidgetRoot includedRoot = new WidgetRoot(includedPage);
        assertEquals("IncludedPage", includedRoot.getVariable("PAGE_NAME"));
        // RUNNING_PAGE_NAME returns PAGE_NAME if the page isn't included.
        assertEquals("IncludedPage", includedRoot.getVariable("RUNNING_PAGE_NAME"));
    }

    @Test
    public void testMultipleLevelsOfIncludedPages() throws Exception {
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        final String ROOT_PAGE_NAME = "RootPage";
        WikiPage root = crawler.addPage(this.root, PathParser.parse(ROOT_PAGE_NAME));
        final String INCLUDED_PAGE_NAME = "IncludedPage";
        WikiPage includedPage = crawler.addPage(this.root, PathParser.parse(INCLUDED_PAGE_NAME));
        final String SECOND_LEVEL_INCLUDED_PAGE_NAME = "SecondLevelIncludedPage";
        WikiPage secondLevelIncludedPage = crawler.addPage(this.root, PathParser.parse(SECOND_LEVEL_INCLUDED_PAGE_NAME));
        WidgetRoot widgetRoot = new WidgetRoot(root);
        WidgetRoot includedRoot = new WidgetRoot(includedPage, widgetRoot);
        WidgetRoot secondLevelRoot = new WidgetRoot(secondLevelIncludedPage, includedRoot);
        PageData data = secondLevelIncludedPage.getData();
        assertEquals(SECOND_LEVEL_INCLUDED_PAGE_NAME, data.getVariable("PAGE_NAME"));
        assertEquals(SECOND_LEVEL_INCLUDED_PAGE_NAME, secondLevelRoot.getVariable("PAGE_NAME"));
        assertEquals(ROOT_PAGE_NAME, secondLevelRoot.getVariable("RUNNING_PAGE_NAME"));
    }

    @Test
    public void testVariablesOnTheRootPage() throws Exception {
        PageData data = root.getData();
        data.setContent("!define v1 {Variable #1}\n");
        root.commit(data);
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "!define v2 {blah}\n${v1}\n");
        data = page.getData();
        assertEquals("Variable #1", data.getVariable("v1"));
    }

    @Test
    public void VariableOnParentShouldBeAbleToUseVariablesDeclaredOnChild() throws Exception {
        WikiPagePath parentPath = PathParser.parse("ParentPage");
        WikiPagePath childPath = PathParser.parse("ChildPage");
        WikiPage parent = crawler.addPage(root, parentPath, "!define X (value=${Y})\n");
        WikiPage child = crawler.addPage(parent, childPath, "!define Y {saba}\n");
        PageData childData = child.getData();
        assertEquals("value=saba", childData.getVariable("X"));
    }

    @Test
    public void testVariablesFromSystemProperties() throws Exception {
        PageData data = root.getData();
        System.getProperties().setProperty("widgetRootTestKey", "widgetRootTestValue");
        root.commit(data);
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "!define v2 {blah}\n${v1}\n");
        data = page.getData();
        assertEquals("widgetRootTestValue", data.getVariable("widgetRootTestKey"));
    }

    @Test
    public void testProcessLiterals() throws Exception {
        WidgetRoot widgetRoot = new WidgetRoot("", root);
        assertEquals(0, widgetRoot.getLiterals().size());
        String result = widgetRoot.processLiterals("With a !-literal-! in the middle");
        RegexAssertions.assertNotSubString("!-", result);
        assertEquals(1, widgetRoot.getLiterals().size());
        assertEquals("literal", widgetRoot.getLiteral(0));
    }

    @Test
    public void testProcessLiteralsCalledWhenConstructed() throws Exception {
        WidgetRoot widgetRoot = new WidgetRoot("With !-another literal-! in the middle", root);
        assertEquals(1, widgetRoot.getLiterals().size());
        assertEquals("another literal", widgetRoot.getLiteral(0));
    }

    @Test
    public void testLiteralsInConstructionAndAfterwards() throws Exception {
        WidgetRoot widgetRoot = new WidgetRoot("the !-first-! literal", root);
        String result = widgetRoot.processLiterals("the !-second-! literal");

        assertEquals("the first literal", widgetRoot.render());
        //Paren Literal: () -> ??
        assertEquals("the !lit?1? literal", result);
        assertEquals(2, widgetRoot.getLiterals().size());
        assertEquals("first", widgetRoot.getLiteral(0));
        assertEquals("second", widgetRoot.getLiteral(1));
    }

    @Test
    public void testShouldHavePortVariableAvailable() throws Exception {
        context.getInjector().getInstance(FitNesse.class);
        WidgetRoot root = new WidgetRoot("", this.root);
        assertEquals(Integer.toString(PORT), root.getVariable("FITNESSE_PORT"));
    }

    @Test
    public void testShouldHaveRootPathVariableAvailable() throws Exception {
        context.getInjector().getInstance(FitNesse.class);
        WidgetRoot root = new WidgetRoot("", this.root);
        assertEquals(getRootPath(), root.getVariable("FITNESSE_ROOTPATH"));
    }

    @Test
    public void carriageReturnsShouldNotMatterIfPresentOnPage() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"), "''italics''\r\n\r'''bold'''\r\n\r");
        PageData data = page.getData();
        String html = data.getHtml();
        assertEquals("<i>italics</i>"
                + ParserTestHelper.newLineRendered
                + "<b>bold</b>"
                + ParserTestHelper.newLineRendered, html);
    }

    @Test
    public void nestedTableExpansion() throws Exception {
        PageData data = root.getData();
        data.setContent("!define AA {|aa|\n}\n" +
                "!define BB (|${AA}|\n)\n"
        );
        root.commit(data);
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "${BB}\n");
        data = page.getData();
        String html = data.getHtml();

        boolean found = Pattern
                .compile("<table.+<table.+</table.+</table", Pattern.DOTALL)
                .matcher(html).find();
        assertTrue(html, found);
    }

    @Test
    public void nestedNewlineExpansion() throws Exception {
        PageData data = root.getData();
        data.setContent("!define LIST {\n * list\n}\n" +
                "!define BB (|${LIST}|\n)\n"
        );
        root.commit(data);
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), "${BB}\n");
        data = page.getData();
        String html = data.getHtml();

        boolean found = Pattern
                .compile("<table.+<td.+<ul.+<li.+</li.+</ul.+</td.+</table", Pattern.DOTALL)
                .matcher(html).find();

        assertTrue("[" + html + "]", found);
    }
}
