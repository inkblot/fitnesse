package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class AnchorNameTest extends WikiBaseTestCase {
    @Test
    public void scansAnchors() {
        ParserTestHelper.assertScansTokenType("!anchor name", "AnchorName", true, injector);
        ParserTestHelper.assertScansTokenType("! anchor name", "AnchorName", false, injector);
    }

    @Test
    public void parsesAnchors() throws Exception {
        ParserTestHelper.assertParses("!anchor name", "SymbolList[AnchorName[Text]]", injector);
        ParserTestHelper.assertParses("!anchor 1234", "SymbolList[AnchorName[Text]]", injector);
        ParserTestHelper.assertParses("!anchor @#$@#%", "SymbolList[Text, Whitespace, Text]", injector);
        ParserTestHelper.assertParses("!anchorname", "SymbolList[Text]", injector);
    }

    @Test
    public void translatesAnchors() {
        ParserTestHelper.assertTranslatesTo("!anchor name", anchorWithName("name"), injector);
        ParserTestHelper.assertTranslatesTo("!anchor name stuff", anchorWithName("name") + " stuff", injector);
        ParserTestHelper.assertTranslatesTo("more!anchor name stuff", "more" + anchorWithName("name") + " stuff", injector);
        ParserTestHelper.assertTranslatesTo("more !anchor name stuff", "more " + anchorWithName("name") + " stuff", injector);
    }

    private String anchorWithName(String name) {
        return "<a name=\"" + name + "\"> </a>";
    }
}
