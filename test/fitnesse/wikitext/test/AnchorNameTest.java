package fitnesse.wikitext.test;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

public class AnchorNameTest extends FitnesseBaseTestCase {
    @Test
    public void scansAnchors() {
        ParserTestHelper.assertScansTokenType("!anchor name", "AnchorName", true);
        ParserTestHelper.assertScansTokenType("! anchor name", "AnchorName", false);
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
        ParserTestHelper.assertTranslatesTo("!anchor name", anchorWithName("name"));
        ParserTestHelper.assertTranslatesTo("!anchor name stuff", anchorWithName("name") + " stuff");
        ParserTestHelper.assertTranslatesTo("more!anchor name stuff", "more" + anchorWithName("name") + " stuff");
        ParserTestHelper.assertTranslatesTo("more !anchor name stuff", "more " + anchorWithName("name") + " stuff");
    }

    private String anchorWithName(String name) {
        return "<a name=\"" + name + "\"> </a>";
    }
}
