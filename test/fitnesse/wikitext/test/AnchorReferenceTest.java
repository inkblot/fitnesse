package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class AnchorReferenceTest extends WikiBaseTestCase {
    @Test
    public void scansAnchors() {
        ParserTestHelper.assertScansTokenType(".#anchorName", "AnchorReference", true, injector);
        ParserTestHelper.assertScansTokenType(".# anchorName", "AnchorReference", true, injector);
        ParserTestHelper.assertScansTokenType(". #anchor Name", "AnchorReference", false, injector);
        ParserTestHelper.assertScansTokenType("blah.#anchorName", "AnchorReference", true, injector);
    }

    @Test
    public void parsesAnchors() throws Exception {
        ParserTestHelper.assertParses(".#anchorName", "SymbolList[AnchorReference[Text]]", injector);
        ParserTestHelper.assertParses(".# anchorName", "SymbolList[Text, Whitespace, Text]", injector);
    }

    @Test
    public void translatesAnchors() {
        ParserTestHelper.assertTranslatesTo(".#anchorName", anchorReferenceWithName("anchorName"), injector);
        ParserTestHelper.assertTranslatesTo(".#anchorName stuff", anchorReferenceWithName("anchorName") + " stuff", injector);
        ParserTestHelper.assertTranslatesTo("more.#anchorName stuff", "more" + anchorReferenceWithName("anchorName") + " stuff", injector);
        ParserTestHelper.assertTranslatesTo("more\n.#anchorName stuff",
                "more" + ParserTestHelper.newLineRendered + anchorReferenceWithName("anchorName") + " stuff", injector);
    }

    private String anchorReferenceWithName(String name) {
        return "<a href=\"#" + name + "\">.#" + name + "</a>" + HtmlElement.endl;
    }
}
