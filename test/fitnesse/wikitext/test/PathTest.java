package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import fitnesse.wikitext.parser.HtmlTranslator;
import fitnesse.wikitext.parser.ParsingPage;
import fitnesse.wikitext.parser.Paths;
import fitnesse.wikitext.parser.WikiSourcePage;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class PathTest extends WikiBaseTestCase {
    @Test
    public void scansPaths() {
        ParserTestHelper.assertScansTokenType("!path stuff", "Path", true, injector);
    }

    @Test
    public void translatesPaths() throws Exception {
        ParserTestHelper.assertTranslatesTo("!path stuff", "<span class=\"meta\">classpath: stuff</span>", injector);
        ParserTestHelper.assertTranslatesTo("!path stuff\n",
                "<span class=\"meta\">classpath: stuff</span>" + ParserTestHelper.newLineRendered, injector);
    }

    @Test
    public void translatesVariableInPath() throws Exception {
        WikiPage page = new TestRoot(InMemoryPage.makeRoot("root", injector)).makePage("TestPage", "!define x {stuff}\n!path ${x}y\n");
        ParserTestHelper.assertTranslatesTo(page,
                "<span class=\"meta\">variable defined: x=stuff</span>" + HtmlElement.endl +
                        ParserTestHelper.newLineRendered + "<span class=\"meta\">classpath: stuffy</span>" + ParserTestHelper.newLineRendered);
    }

    @Test
    public void findsDefinitions() throws Exception {
        WikiPage page = new TestRoot(InMemoryPage.makeRoot("root", injector)).makePage("TestPage", "!path stuff\n!note and\n!path nonsense");
        ParsingPage parsingPage = new ParsingPage(new WikiSourcePage(page));
        List<String> paths = new Paths(new HtmlTranslator(parsingPage.getPage(), parsingPage)).getPaths(ParserTestHelper.parse(page));
        assertEquals(2, paths.size());
        assertEquals("stuff", paths.get(0));
        assertEquals("nonsense", paths.get(1));
    }
}
