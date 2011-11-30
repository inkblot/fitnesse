package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class CommentTest extends WikiBaseTestCase {
    @Test
    public void scansComments() {
        ParserTestHelper.assertScansTokenType("# comment\n", "Comment", true, injector);
        ParserTestHelper.assertScansTokenType(" # comment\n", "Comment", false, injector);
    }

    @Test
    public void parsesComments() throws Exception {
        ParserTestHelper.assertParses("# comment\n", "SymbolList[Comment[Text]]", injector);
        ParserTestHelper.assertParses("# comment", "SymbolList[Comment[Text]]", injector);
    }

    @Test
    public void translatesComments() {
        ParserTestHelper.assertTranslatesTo("# comment\n", "", injector);
        ParserTestHelper.assertTranslatesTo("# comment", "", injector);
    }
}
