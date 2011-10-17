package fitnesse.wikitext.test;

import fitnesse.SingleContextBaseTestCase;
import org.junit.Test;

public class CommentTest extends SingleContextBaseTestCase {
    @Test
    public void scansComments() {
        ParserTestHelper.assertScansTokenType("# comment\n", "Comment", true);
        ParserTestHelper.assertScansTokenType(" # comment\n", "Comment", false);
    }

    @Test
    public void parsesComments() throws Exception {
        ParserTestHelper.assertParses("# comment\n", "SymbolList[Comment[Text]]", injector);
        ParserTestHelper.assertParses("# comment", "SymbolList[Comment[Text]]", injector);
    }

    @Test
    public void translatesComments() {
        ParserTestHelper.assertTranslatesTo("# comment\n", "");
        ParserTestHelper.assertTranslatesTo("# comment", "");
    }
}
