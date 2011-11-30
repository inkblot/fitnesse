package fitnesse.wikitext.test;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class HelpTest extends WikiBaseTestCase {
    @Test
    public void parsesHelp() throws Exception {
        ParserTestHelper.assertParses("!help", "SymbolList[Help]", injector);
        ParserTestHelper.assertParses("!help -editable", "SymbolList[Help]", injector);
        ParserTestHelper.assertParses("!help -garbage", "SymbolList[Help, Whitespace, Text]", injector);
    }

    @Test
    public void translatesHelp() throws Exception {
        assertTranslates("help me", "!help", PageData.PropertyHELP);
        assertTranslates("", "!help", PageData.PropertySUITES);
        assertTranslates("help me <a href=\"TestHelp?properties\">(edit)</a>", "!help -editable", PageData.PropertyHELP);
        assertTranslates(" <a href=\"TestHelp?properties\">(edit help text)</a>", "!help -editable", PageData.PropertySUITES);
    }

    private void assertTranslates(String expected, String content, String property) throws Exception {
        WikiPage pageWithHelp = new TestRoot(InMemoryPage.makeRoot("root", injector)).makePage("TestHelp", content);
        PageData pageData = pageWithHelp.getData();
        pageData.setAttribute(property, "help me");
        pageWithHelp.commit(pageData);
        ParserTestHelper.assertTranslatesTo(pageWithHelp, expected);
    }
}
