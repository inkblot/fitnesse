package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import fitnesse.wikitext.parser.Parser;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.WikiSourcePage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefineTest extends WikiBaseTestCase {
    @Test
    public void scansDefine() {
        ParserTestHelper.assertScansTokenType("!define x {y}", "Define", true, injector);
        ParserTestHelper.assertScansTokenType("|!define x {y}|/n", "Define", true, injector);
    }

    @Test
    public void translatesDefines() throws Exception {
        assertTranslatesDefine("!define x {y}", "x=y");
        assertTranslatesDefine("!define BoBo {y}", "BoBo=y");
        assertTranslatesDefine("!define BoBo  {y}", "BoBo=y");
        assertTranslatesDefine("!define x {}", "x=");
        assertTranslatesDefine("!define x_x {y}", "x_x=y");
        assertTranslatesDefine("!define x.x {y}", "x.x=y");
        assertTranslatesDefine("!define x (y)", "x=y");
        assertTranslatesDefine("!define x [y]", "x=y");
        assertTranslatesDefine("!define x {''y''}", "x=''y''");
    }

    @Test
    public void definesValues() throws Exception {
        assertDefinesValue("!define x {y}", "x", "y");
        assertDefinesValue("|!define x {y}|\n", "x", "y");
        //todo: move to variableTest?
        //assertDefinesValue("!define x {''y''}", "x", "<i>y</i>");
        //assertDefinesValue("!define x {!note y\n}", "x", "<span class=\"note\">y</span><br/>");
        //assertDefinesValue("!define z {y}\n!define x {${z}}", "x", "y");
        //assertDefinesValue("!define z {''y''}\n!define x {${z}}", "x", "<i>y</i>");
        //assertDefinesValue("!define z {y}\n!define x {''${z}''}", "x", "<i>y</i>");
    }

    private void assertDefinesValue(String input, String name, String definedValue) throws Exception {
        WikiPage pageOne = new TestRoot(InMemoryPage.makeRoot("root", injector)).makePage("PageOne", input);
        ParsingPage page = new ParsingPage(new WikiSourcePage(pageOne));
        Parser.make(page, input).parse();
        assertEquals(definedValue, page.findVariable(name).getValue());
    }

    private void assertTranslatesDefine(String input, String definition) throws Exception {
        WikiPage pageOne = new TestRoot(InMemoryPage.makeRoot("root", injector)).makePage("PageOne");
        ParserTestHelper.assertTranslatesTo(pageOne, input,
                "<span class=\"meta\">variable defined: " + definition + "</span>" + HtmlElement.endl);
    }

}
