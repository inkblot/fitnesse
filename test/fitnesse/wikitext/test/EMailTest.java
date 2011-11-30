package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class EMailTest extends WikiBaseTestCase {
    @Test
    public void parsesEMail() throws Exception {
        ParserTestHelper.assertParses("bob@bl.org", "SymbolList[EMail]", injector);
    }

    @Test
    public void translatesEMail() {
        ParserTestHelper.assertTranslatesTo("bob@bl.org", "<a href=\"mailto:bob@bl.org\">bob@bl.org</a>", injector);
    }
}
