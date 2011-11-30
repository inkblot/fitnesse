package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class EqualPairTest extends WikiBaseTestCase {
    @Test
    public void scansTripleQuotes() {
        ParserTestHelper.assertScansTokenType("'''bold'''", "Bold", true, injector);
        ParserTestHelper.assertScansTokenType("''''bold''''", "Bold", true, injector);
        ParserTestHelper.assertScansTokenType("'' 'not bold' ''", "Bold", false, injector);
        ParserTestHelper.assertScansTokenType("''''some text' '''", "Bold", true, injector);
    }

    @Test
    public void translatesBold() {
        ParserTestHelper.assertTranslatesTo("'''bold text'''", "<b>bold text</b>", injector);
    }

    @Test
    public void scansDoubleQuotes() {
        ParserTestHelper.assertScansTokenType("''italic''", "Italic", true, injector);
        ParserTestHelper.assertScansTokenType("'' 'italic' ''", "Italic", true, injector);
    }

    @Test
    public void translatesItalic() {
        ParserTestHelper.assertTranslatesTo("''italic text''", "<i>italic text</i>", injector);
    }

    @Test
    public void translatesBoldItalic() {
        ParserTestHelper.assertTranslatesTo("'''''stuff&nonsense'''''",
                "<b><i>stuff&amp;nonsense</i></b>", injector);
    }

    @Test
    public void ignoresAdjacentItalics() {
        ParserTestHelper.assertTranslatesTo("''", "''", injector);
        ParserTestHelper.assertTranslatesTo("''''", "''''", injector);
    }

    @Test
    public void translatesItalicQuote() {
        ParserTestHelper.assertTranslatesTo("'''''", "<i>'</i>", injector);
    }

    @Test
    public void scansDoubleDashes() {
        ParserTestHelper.assertScansTokenType("abc--123--def", "Strike", true, injector);
        ParserTestHelper.assertScansTokenType("--- -", "Strike", true, injector);
    }

    @Test
    public void translatesStrike() {
        ParserTestHelper.assertTranslatesTo("--some text--", "<span class=\"strike\">some text</span>", injector);
        ParserTestHelper.assertTranslatesTo("--embedded-dash--", "<span class=\"strike\">embedded-dash</span>", injector);
    }

    @Test
    public void testEvilExponentialMatch() throws Exception {
        long startTime = System.currentTimeMillis();

        ParserTestHelper.assertTranslatesTo("--1234567890123456789012", "--1234567890123456789012", injector);

        long endTime = System.currentTimeMillis();
        assertTrue("took too long", endTime - startTime < 20);
    }
}
