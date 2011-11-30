package fitnesse.wikitext.test;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class SeeTest extends WikiBaseTestCase {
    @Test
    public void scansSees() {
        ParserTestHelper.assertScansTokenType("!see Stuff", "See", true, injector);
        ParserTestHelper.assertScansTokenType("!seeStuff", "See", false, injector);
    }

    @Test
    public void parsesSees() throws Exception {
        ParserTestHelper.assertParses("!see SomeStuff", "SymbolList[See[WikiWord]]", injector);
        ParserTestHelper.assertParses("!see ya", "SymbolList[Text, Whitespace, Text]", injector);
    }

    @Test
    public void translatesSees() throws Exception {
        TestRoot root = new TestRoot(InMemoryPage.makeRoot("root", injector));
        WikiPage page = root.makePage("PageOne", "!see PageTwo");
        root.makePage("PageTwo", "hi");
        ParserTestHelper.assertTranslatesTo(page, "<b>See: <a href=\"PageTwo\">PageTwo</a></b>");
    }
}
