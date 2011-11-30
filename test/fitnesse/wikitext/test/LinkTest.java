package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class LinkTest extends WikiBaseTestCase {
    @Test
    public void scansLinks() {
        ParserTestHelper.assertScansTokenType("http://mysite.org", "Link", true, injector);
        ParserTestHelper.assertScansTokenType("https://mysite.org", "Link", true, injector);
        ParserTestHelper.assertScansTokenType("http:/mysite.org", "Link", false, injector);
        ParserTestHelper.assertScansTokenType("httpx://mysite.org", "Link", false, injector);
    }

    @Test
    public void parsesLinks() throws Exception {
        ParserTestHelper.assertParses("http://mysite.org", "SymbolList[Link[SymbolList[Text]]]", injector);
    }

    @Test
    public void translatesLinks() {
        ParserTestHelper.assertTranslatesTo("http://mysite.org", "<a href=\"http://mysite.org\">http://mysite.org</a>", injector);
        ParserTestHelper.assertTranslatesTo("http://files/myfile", "<a href=\"/files/myfile\">http://files/myfile</a>", injector);
        ParserTestHelper.assertTranslatesTo("''http://files/myfile''", "<i><a href=\"/files/myfile\">http://files/myfile</a></i>", injector);
    }

    @Test
    public void translatesLinkWithVariable() {
        ParserTestHelper.assertTranslatesTo("http://${site}", new TestVariableSource("site", "mysite.org"), "<a href=\"http://mysite.org\">http://mysite.org</a>");
    }

    @Test
    public void translatesImageLinks() {
        ParserTestHelper.assertTranslatesTo("http://some.jpg", "<img src=\"http://some.jpg\"/>", injector);
    }
}
