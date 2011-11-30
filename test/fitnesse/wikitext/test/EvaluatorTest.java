package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class EvaluatorTest extends WikiBaseTestCase {
    @Test
    public void scansEvaluators() {
        ParserTestHelper.assertScansTokenType("${=3+4=}", "Evaluator", true, injector);
    }

    @Test
    public void translatesEvaluators() {
        ParserTestHelper.assertTranslatesTo("${= 8 =}", "8", injector);
        ParserTestHelper.assertTranslatesTo("${=42.24=}", "42.24", injector);
        ParserTestHelper.assertTranslatesTo("${=1.2E+3=}", "1200", injector);
        ParserTestHelper.assertTranslatesTo("${=-123=}", "-123", injector);
        ParserTestHelper.assertTranslatesTo("${=%d:3.2=}", "3", injector);
        ParserTestHelper.assertTranslatesTo("${==}", "", injector);
        ParserTestHelper.assertTranslatesTo("${= =}", "", injector);
        ParserTestHelper.assertTranslatesTo("${=3+4=}", "7", injector);
        ParserTestHelper.assertTranslatesTo("${=abort=}", "<span class=\"meta\">invalid expression: abort</span>", injector);
    }
}
