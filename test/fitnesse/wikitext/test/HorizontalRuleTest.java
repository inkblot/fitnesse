package fitnesse.wikitext.test;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlElement;
import org.junit.Test;

public class HorizontalRuleTest extends FitnesseBaseTestCase {
    @Test
    public void scansHorizontalRules() {
        ParserTestHelper.assertScansTokenType("----", "HorizontalRule", true, injector);
        ParserTestHelper.assertScansTokenType("------", "HorizontalRule", true, injector);
    }

    @Test
    public void translatesNotes() {
        ParserTestHelper.assertTranslatesTo("----", "<hr/>" + HtmlElement.endl, injector);
        ParserTestHelper.assertTranslatesTo("------", "<hr size=\"3\"/>" + HtmlElement.endl, injector);
    }
}
