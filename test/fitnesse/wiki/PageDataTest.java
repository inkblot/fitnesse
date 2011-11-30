// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static fitnesse.wiki.PageData.*;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertDoesNotHaveRegexp;
import static util.RegexAssertions.assertHasRegexp;

public class PageDataTest extends WikiBaseTestCase {
    public WikiPage page;
    private WikiPage root;
    private PageCrawler crawler;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        page = crawler.addPage(root, PathParser.parse("PagE"), "some content");
    }

    @Test
    public void testVariablePreprocessing() throws Exception {
        PageData d = new PageData(InMemoryPage.makeRoot("RooT", injector), "!define x {''italic''}\n${x}\n");
        String preprocessedText = d.getContent();
        assertHasRegexp("''italic''", preprocessedText);
    }

    @Test
    public void testVariablesRenderedFirst() throws Exception {
        String text = "!define x {''italics''}\n${x}";
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), text);
        String html = page.getData().getHtml();
        assertHasRegexp("''italics''", html);
        assertHasRegexp("<i>italics</i>", html);
    }

    @Test
    public void testVariablesWithinVariablesAreResolved() throws Exception {
        String text = "!define x {b}\n!define y (a${x}c)\n${y}";
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = crawler.addPage(root, PathParser.parse("SomePage"), text);
        String html = page.getData().getHtml();
        assertHasRegexp("abc", html);
        assertHasRegexp("variable defined: y=a\\$\\{x\\}c", html);
        String variableContents = page.getData().getVariable("y");
        assertEquals("abc", variableContents);
    }

    @Test
    public void testThatSpecialCharsAreNotEscapedTwice() throws Exception {
        PageData d = new PageData(new WikiPageDummy(injector), "<b>");
        String html = d.getHtml();
        assertEquals("&lt;b&gt;", html);
    }

    @Test
    public void testLiteral() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = crawler.addPage(root, PathParser.parse("LiteralPage"), "!-literal-!");
        String renderedContent = page.getData().getHtml();
        assertHasRegexp("literal", renderedContent);
        assertDoesNotHaveRegexp("!-literal-!", renderedContent);
    }

    @Test
    public void testClasspath() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = crawler.addPage(root, PathParser.parse("ClassPath"), "!path 123\n!path abc\n");
        List<?> paths = page.getData().getClasspaths();
        assertTrue(paths.contains("123"));
        assertTrue(paths.contains("abc"));
    }

    @Test
    public void testClasspathWithVariable() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);

        WikiPage page = crawler.addPage(root, PathParser.parse("ClassPath"), "!define PATH {/my/path}\n!path ${PATH}.jar");
        List<?> paths = page.getData().getClasspaths();
        assertEquals("/my/path.jar", paths.get(0).toString());

        PageData data = root.getData();
        data.setContent("!define PATH {/my/path}\n");
        root.commit(data);

        page = crawler.addPage(root, PathParser.parse("ClassPath2"), "!path ${PATH}.jar");
        paths = page.getData().getClasspaths();
        assertEquals("/my/path.jar", paths.get(0).toString());
    }

    @Test
    public void testClasspathWithVariableDefinedInIncludedPage() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        crawler.addPage(root, PathParser.parse("VariablePage"), "!define PATH {/my/path}\n");

        WikiPage page = crawler.addPage(root, PathParser.parse("ClassPath"), "!include VariablePage\n!path ${PATH}.jar");
        List<?> paths = page.getData().getClasspaths();
        assertEquals("/my/path.jar", paths.get(0).toString());
    }

    @Test
    public void testVariableIgnoredInParentPreformatted() throws Exception {  //--variables in parent preformatted blocks must not recognize !define widgets.
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage parent = crawler.addPage(root, PathParser.parse("VariablePage"), "{{{\n!define SOMEVAR {A VALUE}\n}}}\n");
        WikiPage child = crawler.addPage(parent, PathParser.parse("ChildPage"), "${SOMEVAR}\n");
        String renderedContent = child.getData().getHtml();
        assertHasRegexp("undefined variable", renderedContent);
    }

    @Test
    public void testGetCrossReferences() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = crawler.addPage(root, PathParser.parse("PageName"), "!see XrefPage\r\n");
        List<?> xrefs = page.getData().getXrefPages();
        assertEquals("XrefPage", xrefs.get(0));
    }

    @Test
    public void testThatExamplesAtEndOfNameSetsSuiteProperty() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("PageExamples"));
        PageData data = new PageData(page);
        assertTrue(data.hasAttribute(SUITE.toString()));
    }

    @Test
    public void testThatExampleAtBeginningOfNameSetsTestProperty() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("ExamplePageExample"));
        PageData data = new PageData(page);
        assertTrue(data.hasAttribute(TEST.toString()));
    }

    @Test
    public void testThatExampleAtEndOfNameSetsTestProperty() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("PageExample"));
        PageData data = new PageData(page);
        assertTrue(data.hasAttribute(TEST.toString()));
    }

    @Test
    public void testThatSuiteAtBeginningOfNameSetsSuiteProperty() throws Exception {
        WikiPage suitePage1 = crawler.addPage(root, PathParser.parse("SuitePage"));
        PageData data = new PageData(suitePage1);
        assertFalse(data.hasAttribute(TEST.toString()));
        assertTrue(data.hasAttribute(SUITE.toString()));
    }

    @Test
    public void testThatSuiteAtEndOfNameSetsSuiteProperty() throws Exception {
        WikiPage suitePage2 = crawler.addPage(root, PathParser.parse("PageSuite"));
        PageData data = new PageData(suitePage2);
        assertFalse(data.hasAttribute(TEST.toString()));
        assertTrue(data.hasAttribute(SUITE.toString()));
    }

    @Test
    public void testThatTestAtBeginningOfNameSetsTestProperty() throws Exception {
        WikiPage testPage1 = crawler.addPage(root, PathParser.parse("TestPage"));
        PageData data = new PageData(testPage1);
        assertTrue(data.hasAttribute(TEST.toString()));
        assertFalse(data.hasAttribute(SUITE.toString()));
    }

    @Test
    public void testThatTestAtEndOfNameSetsTestProperty() throws Exception {
        WikiPage testPage2 = crawler.addPage(root, PathParser.parse("PageTest"));
        PageData data = new PageData(testPage2);
        assertTrue(data.hasAttribute(TEST.toString()));
        assertFalse(data.hasAttribute(SUITE.toString()));
    }

    @Test
    public void testDefaultAttributes() throws Exception {
        WikiPage normalPage = crawler.addPage(root, PathParser.parse("NormalPage"));
        WikiPage suitePage3 = crawler.addPage(root, PathParser.parse("TestPageSuite"));
        WikiPage errorLogsPage = crawler.addPage(root, PathParser.parse("ErrorLogs.TestPage"));
        WikiPage suiteSetupPage = crawler.addPage(root, PathParser.parse(SUITE_SETUP_NAME));
        WikiPage suiteTearDownPage = crawler.addPage(root, PathParser.parse(SUITE_TEARDOWN_NAME));

        PageData data = new PageData(normalPage);
        assertTrue(data.hasAttribute(PropertyEDIT));
        assertTrue(data.hasAttribute(PropertySEARCH));
        assertTrue(data.hasAttribute(PropertyVERSIONS));
        assertTrue(data.hasAttribute(PropertyFILES));
        assertFalse(data.hasAttribute(TEST.toString()));
        assertFalse(data.hasAttribute(SUITE.toString()));

        data = new PageData(suitePage3);
        assertFalse(data.hasAttribute(TEST.toString()));
        assertTrue(data.hasAttribute(SUITE.toString()));

        data = new PageData(errorLogsPage);
        assertFalse(data.hasAttribute(TEST.toString()));
        assertFalse(data.hasAttribute(SUITE.toString()));

        data = new PageData(suiteSetupPage);
        assertFalse(data.hasAttribute(SUITE.toString()));

        data = new PageData(suiteTearDownPage);
        assertFalse(data.hasAttribute(SUITE.toString()));
    }

    @Test
    public void testAttributesAreTruelyCopiedInCopyConstructor() throws Exception {
        PageData data = root.getData();
        data.setAttribute(LAST_MODIFYING_USER, "Joe");
        PageData newData = new PageData(data);
        newData.setAttribute(LAST_MODIFYING_USER, "Jane");

        assertEquals("Joe", data.getAttribute(LAST_MODIFYING_USER));
    }
}
