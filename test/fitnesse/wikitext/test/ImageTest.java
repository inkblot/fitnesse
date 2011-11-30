package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class ImageTest extends WikiBaseTestCase {
    @Test
    public void scansImages() {
        ParserTestHelper.assertScansTokenType("!img name", "Image", true, injector);
    }

    @Test
    public void parsesImages() throws Exception {
        ParserTestHelper.assertParses("!img name", "SymbolList[Link[SymbolList[Text]]]", injector);
        ParserTestHelper.assertParses("!img http://name", "SymbolList[Link[SymbolList[Text]]]", injector);
        ParserTestHelper.assertParses("!imgx name", "SymbolList[Text, Whitespace, Text]", injector);
        ParserTestHelper.assertParses("!img-l name", "SymbolList[Link[SymbolList[Text]]]", injector);
        ParserTestHelper.assertParses("!img-r name", "SymbolList[Link[SymbolList[Text]]]", injector);
    }

    @Test
    public void translatesImages() {
        ParserTestHelper.assertTranslatesTo("!img name", "<img src=\"name\"/>", injector);
        ParserTestHelper.assertTranslatesTo("!img http://name", "<img src=\"http://name\"/>", injector);
        ParserTestHelper.assertTranslatesTo("!img-l name", "<img src=\"name\" class=\"left\"/>", injector);
        ParserTestHelper.assertTranslatesTo("!img-r name", "<img src=\"name\" class=\"right\"/>", injector);
    }
}
