package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class LiteralTest extends WikiBaseTestCase {
    @Test
    public void scansLiteral() {
        ParserTestHelper.assertScansTokenType("!- stuff -!", "Literal", true, injector);
    }

    @Test
    public void translatesLiteral() {
        ParserTestHelper.assertTranslatesTo("!-stuff-!", "stuff", injector);
        ParserTestHelper.assertTranslatesTo("!-''not italic''-!", "''not italic''", injector);
        ParserTestHelper.assertTranslatesTo("!-break\n-!|", "break\n|", injector);
    }
}
