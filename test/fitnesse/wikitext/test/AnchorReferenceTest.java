package fitnesse.wikitext.test;

import fitnesse.SingleContextBaseTestCase;
import fitnesse.html.HtmlElement;
import org.junit.Test;

public class AnchorReferenceTest extends SingleContextBaseTestCase {
    @Test
    public void scansAnchors() {
        ParserTestHelper.assertScansTokenType(".#anchorName", "AnchorReference", true);
        ParserTestHelper.assertScansTokenType(".# anchorName", "AnchorReference", true);
        ParserTestHelper.assertScansTokenType(". #anchor Name", "AnchorReference", false);
        ParserTestHelper.assertScansTokenType("blah.#anchorName", "AnchorReference", true);
    }

    @Test
    public void parsesAnchors() throws Exception {
        ParserTestHelper.assertParses(".#anchorName", "SymbolList[AnchorReference[Text]]", injector);
        ParserTestHelper.assertParses(".# anchorName", "SymbolList[Text, Whitespace, Text]", injector);
    }

    @Test
    public void translatesAnchors() {
        ParserTestHelper.assertTranslatesTo(".#anchorName", anchorReferenceWithName("anchorName"));
        ParserTestHelper.assertTranslatesTo(".#anchorName stuff", anchorReferenceWithName("anchorName") + " stuff");
        ParserTestHelper.assertTranslatesTo("more.#anchorName stuff", "more" + anchorReferenceWithName("anchorName") + " stuff");
        ParserTestHelper.assertTranslatesTo("more\n.#anchorName stuff",
                "more" + ParserTestHelper.newLineRendered + anchorReferenceWithName("anchorName") + " stuff");
    }

    private String anchorReferenceWithName(String name) {
        return "<a href=\"#" + name + "\">.#" + name + "</a>" + HtmlElement.endl;
    }
}
