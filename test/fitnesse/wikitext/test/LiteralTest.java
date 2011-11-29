package fitnesse.wikitext.test;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

public class LiteralTest extends FitnesseBaseTestCase {
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
