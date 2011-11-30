package fitnesse.wikitext.test;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class AliasTest extends WikiBaseTestCase {

    private WikiPage root;
    private TestSourcePage sourcePage;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root, TestSourcePage sourcePage) {
        this.root = root;
        this.sourcePage = sourcePage;
    }

    @Test
    public void scansAliases() {
        ParserTestHelper.assertScansTokenType("[[tag][link]]", "Alias", true, injector);
        ParserTestHelper.assertScansTokenType("[ [tag][link]]", "Alias", false, injector);
    }

    @Test
    public void parsesAliases() throws Exception {
        ParserTestHelper.assertParses("[[tag][PageOne]]", "SymbolList[Alias[SymbolList[Text], SymbolList[Text]]]", injector);
        ParserTestHelper.assertParses("[[PageOne][PageOne]]", "SymbolList[Alias[SymbolList[WikiWord], SymbolList[Text]]]", injector);
        ParserTestHelper.assertParses("[[PageOne][PageOne?edit]]", "SymbolList[Alias[SymbolList[WikiWord], SymbolList[Text]]]", injector);
    }

    @Test
    public void translatesAliases() throws Exception {
        TestSourcePage page = sourcePage.withTarget("PageOne");
        ParserTestHelper.assertTranslatesTo(page, "[[tag][link]]", link("tag", "link"));
        ParserTestHelper.assertTranslatesTo(page, "[[tag][#anchor]]", link("tag", "#anchor"));
        ParserTestHelper.assertTranslatesTo(page, "[[tag][PageOne]]", link("tag", "PageOne"));
        ParserTestHelper.assertTranslatesTo(page, "[[''tag''][PageOne]]", link("<i>tag</i>", "PageOne"));
        ParserTestHelper.assertTranslatesTo(page, "[[you're it][PageOne]]", link("you're it", "PageOne"));
        ParserTestHelper.assertTranslatesTo(page, "[[PageOne][IgnoredPage]]", link("PageOne", "PageOne"));
        ParserTestHelper.assertTranslatesTo(page, "[[tag][PageOne?edit]]", link("tag", "PageOne?edit"));
        ParserTestHelper.assertTranslatesTo(page, "[[tag][http://files/myfile]]", link("tag", "/files/myfile"));
    }

    @Test
    public void translatesLinkToNonExistent() {
        ParserTestHelper.assertTranslatesTo(sourcePage.withUrl("NonExistentPage"), "[[tag][NonExistentPage]]",
                "tag<a title=\"create page\" href=\"NonExistentPage?edit&nonExistent=true\">[?]</a>");
    }

    @Test
    public void evaluatesVariablesInLink() throws Exception {
        TestRoot root = new TestRoot(this.root);
        WikiPage page = root.makePage("PageOne", "[[tag][PageTwo${x}]]");
        root.makePage("PageTwo3", "hi");
        ParserTestHelper.assertTranslatesTo(page, new TestVariableSource("x", "3"), link("tag", "PageTwo3"));
    }

    private String link(String body, String href) {
        return "<a href=\"" + href + "\">" + body + "</a>";
    }

}
