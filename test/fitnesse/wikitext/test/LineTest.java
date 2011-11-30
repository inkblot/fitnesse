package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class LineTest extends WikiBaseTestCase {
    @Test
    public void scansHeaders() {
        ParserTestHelper.assertScans("!1 some text\n", "HeaderLine=!1,Whitespace= ,Text=some,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!2 \n", "HeaderLine=!2,Whitespace= ,Newline=\n", injector);
        ParserTestHelper.assertScans("!3 text\n", "HeaderLine=!3,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!4 text\n", "HeaderLine=!4,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!5 text\n", "HeaderLine=!5,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!6 text\n", "HeaderLine=!6,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!3text\n", "HeaderLine=!3,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!0 text\n", "Text=!0,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!7 text\n", "Text=!7,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("not start !1 text\n", "Text=not,Whitespace= ,Text=start,Whitespace= ,HeaderLine=!1,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("at start\n!1 text\n", "Text=at,Whitespace= ,Text=start,Newline=\n,HeaderLine=!1,Whitespace= ,Text=text,Newline=\n", injector);
    }

    @Test
    public void translatesHeaders() {
        for (int i = 1; i < 7; i++)
            ParserTestHelper.assertTranslatesTo("!" + i + " some text", "<h" + i + ">some text</h" + i + ">" + HtmlElement.endl, injector);
    }

    @Test
    public void scansCenters() {
        ParserTestHelper.assertScans("!c some text\n", "CenterLine=!c,Whitespace= ,Text=some,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!C more text\n", "CenterLine=!C,Whitespace= ,Text=more,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!ctext\n", "CenterLine=!c,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!c text\n", "CenterLine=!c,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans(" !c text\n", "Whitespace= ,CenterLine=!c,Whitespace= ,Text=text,Newline=\n", injector);
        ParserTestHelper.assertScans("!c text", "CenterLine=!c,Whitespace= ,Text=text", injector);
    }

    @Test
    public void translatesCenters() {
        ParserTestHelper.assertTranslatesTo("!c some text", "<div class=\"centered\">some text</div>" + HtmlElement.endl, injector);
    }

    @Test
    public void scansNotes() {
        ParserTestHelper.assertScans("!note some note\n", "NoteLine=!note,Whitespace= ,Text=some,Whitespace= ,Text=note,Newline=\n", injector);
        ParserTestHelper.assertScans("! note some note\n", "Text=!,Whitespace= ,Text=note,Whitespace= ,Text=some,Whitespace= ,Text=note,Newline=\n", injector);
    }

    @Test
    public void translatesNotes() {
        ParserTestHelper.assertTranslatesTo("!note some note", "<span class=\"note\">some note</span>", injector);
    }

    @Test
    public void translatesMetas() {
        ParserTestHelper.assertTranslatesTo("!meta stuff", "<span class=\"meta\">stuff</span>", injector);
    }
}
