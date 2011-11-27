// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlElement;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class WikiTextTranslatorTest extends FitnesseBaseTestCase {
    private WikiPage page;
    private String expectedHtmlFromWikiText =
            "<table border=\"1\" cellspacing=\"0\">\n<tr><td>this</td>" + HtmlElement.endl +
                    "<td>is</td>" + HtmlElement.endl +
                    "<td>a</td>" + HtmlElement.endl +
                    "<td>table</td>" + HtmlElement.endl +
                    "</tr>\n" +
                    "<tr><td>that</td>" + HtmlElement.endl +
                    "<td>has</td>" + HtmlElement.endl +
                    "<td>four</td>" + HtmlElement.endl +
                    "<td>columns</td>" + HtmlElement.endl +
                    "</tr>\n</table>\n";
    private WikiPage root;

    @Inject
    public void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        PageCrawler crawler = root.getPageCrawler();
        page = crawler.addPage(root, PathParser.parse("WidgetRoot"));
    }

    @Test
    public void testTranslation1() throws Exception {
        String wikiText = "!c !1 This is a WidgetRoot\n" +
                "\n" +
                "''' ''Some Bold and Italic text'' '''\n";
        String html = "<div class=\"centered\"><h1>This is a <a href=\"WidgetRoot\">WidgetRoot</a></h1></div>" +
                "<br/>" +
                "<b> <i>Some Bold and Italic text</i> </b><br/>";
        assertEquals(html, translate(wikiText, page));
    }

    @Test
    public void testHtmlEscape() throws Exception {
        String wikiText = "<h1>this \"&\" that</h1>";
        String html = "&lt;h1&gt;this \"&amp;\" that&lt;/h1&gt;";
        assertEquals(html, translate(wikiText, new WikiPageDummy(injector)));
    }

    @Test
    public void testTableHtml() throws Exception {
        String wikiText = "|this|is|a|table|\n|that|has|four|columns|\n";
        assertEquals(expectedHtmlFromWikiText, translate(wikiText, new WikiPageDummy(injector)));
    }

    @Test
    public void testTableHtmlStripsTrailingWhiteSpaceFromLines() throws Exception {
        String wikiText = "|this|is|a|table|\t\n|that|has|four|columns|  \n";
        assertEquals(expectedHtmlFromWikiText, translate(wikiText, new WikiPageDummy(injector)));
    }

    @Test
    public void testTableBlankLinesConvertedToBreaks() throws Exception {
        String wikiText1 = "|this|is|a|table|\t\n|that|has|four|columns|  \n\n  \n\t\n\t";
        assertEquals(expectedHtmlFromWikiText + "<br/><br/><br/>\t", translate(wikiText1, new WikiPageDummy(injector)));
        String wikiText2 = "|this|is|a|table|\t\n|that|has|four|columns|  \n\n  \n\t\n\t\n";
        assertEquals(expectedHtmlFromWikiText + "<br/><br/><br/><br/>", translate(wikiText2, new WikiPageDummy(injector)));
        String wikiText3 = "|this|is|a|table|\t\n|that|has|four|columns|  \n\n  \n\t\n\t\n\n";
        assertEquals(expectedHtmlFromWikiText + "<br/><br/><br/><br/><br/>", translate(wikiText3, new WikiPageDummy(injector)));
    }

    @Test
    public void testBlankLinesConvertedToBreaks() throws Exception {
        String wikiText1 = "\n  \n\t\n\t";
        assertEquals("<br/><br/><br/>\t", translate(wikiText1, new WikiPageDummy(injector)));
        String wikiText2 = "\n  \n\t\n\t\n";
        assertEquals("<br/><br/><br/><br/>", translate(wikiText2, new WikiPageDummy(injector)));
        String wikiText3 = "\n  \n\t\n\t\n";
        assertEquals("<br/><br/><br/><br/>", translate(wikiText3, new WikiPageDummy(injector)));
    }

    private static String translate(String value, WikiPage source) throws Exception {
        ParentWidget page = new WidgetRoot(value, source, WidgetBuilder.htmlWidgetBuilder);
        return page.render();
    }
}
