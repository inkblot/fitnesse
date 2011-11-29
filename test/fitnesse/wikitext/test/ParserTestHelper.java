package fitnesse.wikitext.test;

import com.google.inject.Injector;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParserTestHelper {
    public static final String newLineRendered = "<br/>";

    public static void assertScans(String input, String expected, Injector injector) {
        Scanner scanner = new Scanner(injector.getInstance(TestSourcePage.class), input);
        assertScans(expected, scanner);
    }

    public static void assertScansTokenType(String input, String expected, boolean found, Injector injector) {
        Scanner scanner = new Scanner(injector.getInstance(TestSourcePage.class), input);
        while (true) {
            scanner.moveNext();
            if (scanner.isEnd()) break;
            if (scanner.getCurrent().getType().toString().equals(expected)) {
                assertTrue(found);
                return;
            }
        }
        assertTrue(!found);
    }

    public static void assertScans(String expected, Scanner scanner) {
        StringBuilder result = new StringBuilder();
        while (true) {
            scanner.moveNext();
            if (scanner.isEnd()) break;
            if (result.length() > 0) result.append(",");
            Symbol current = scanner.getCurrent();
            String name = current.getType().toString();
            result.append(name);
            String content = current.getContent();
            if (content.length() > 0) result.append("=").append(content);
        }
        assertEquals(expected, result.toString());
    }

    public static void assertTranslatesTo(String input, String expected, Injector injector) {
        assertTranslatesTo(injector.getInstance(TestSourcePage.class), input, expected);
    }

    public static void assertTranslatesTo(WikiPage page, VariableSource variableSource, String expected) throws Exception {
        assertEquals(expected, translateToHtml(page, page.getData().getContent(), variableSource));
    }

    public static void assertTranslatesTo(String input, VariableSource variableSource, String expected) {
        assertEquals(expected, translateToHtml(null, input, variableSource));
    }

    public static void assertTranslatesTo(WikiPage page, String input, String expected) {
        assertEquals(expected, translateTo(page, input));
    }

    public static void assertTranslatesTo(SourcePage page, String input, String expected) {
        assertEquals(expected, translateTo(page, input));
        assertEquals("round trip", input, roundTrip(page, input));
    }

    public static void assertTranslatesTo(SourcePage page, String expected) throws Exception {
        assertEquals(expected, translateTo(page));
    }

    public static void assertTranslatesTo(WikiPage page, String expected) throws Exception {
        assertEquals(expected, translateTo(new WikiSourcePage(page)));
    }

    public static String translateTo(WikiPage page, String input) {
        return translateTo(new WikiSourcePage(page), input);
    }

    public static String translateTo(SourcePage page, String input) {
        ParsingPage parsingPage = new ParsingPage(page);
        Symbol list = Parser.make(parsingPage, input).parse();
        return new HtmlTranslator(page, parsingPage).translateTree(list);
    }

    public static String roundTrip(SourcePage page, String input) {
        Symbol list = Parser.make(new ParsingPage(page), input, SymbolProvider.refactoringProvider).parse();
        return new WikiTranslator(page).translateTree(list);
    }

    public static String translateToHtml(WikiPage page, String input, VariableSource variableSource) {
        ParsingPage parsingPage = new ParsingPage(new WikiSourcePage(page));
        Symbol list = Parser.make(parsingPage, input, variableSource, SymbolProvider.wikiParsingProvider).parse();
        return new HtmlTranslator(parsingPage.getPage(), parsingPage).translateTree(list);
    }

    public static String translateTo(WikiPage page) throws Exception {
        return translateTo(new WikiSourcePage(page));
    }

    public static String translateTo(SourcePage page, VariableSource variableSource) throws Exception {
        ParsingPage parsingPage = new ParsingPage(page);
        return new HtmlTranslator(page, parsingPage).translateTree(Parser.make(parsingPage, page.getContent(), variableSource, SymbolProvider.wikiParsingProvider).parse());
    }

    public static String translateTo(SourcePage page) throws Exception {
        ParsingPage parsingPage = new ParsingPage(page);
        return new HtmlTranslator(page, parsingPage).translateTree(Parser.make(parsingPage, page.getContent()).parse());
    }

    public static void assertParses(String input, String expected, Injector injector) throws Exception {
        WikiPage page = new TestRoot(InMemoryPage.makeRoot("root", injector)).makePage("TestPage", input);
        Symbol result = parse(page, input);
        assertEquals(expected, serialize(result));
    }

    public static Symbol parse(WikiPage page) throws Exception {
        return Parser.make(new ParsingPage(new WikiSourcePage(page)), page.getData().getContent()).parse();
    }

    public static Symbol parse(WikiPage page, String input) {
        return Parser.make(new ParsingPage(new WikiSourcePage(page)), input).parse();
    }

    public static String serialize(Symbol symbol) {
        StringBuilder result = new StringBuilder();
        result.append(symbol.getType() != null ? symbol.getType().toString() : "?no type?");
        int i = 0;
        for (Symbol child : symbol.getChildren()) {
            result.append(i == 0 ? "[" : ", ");
            result.append(serialize(child));
            i++;
        }
        if (i > 0) result.append("]");
        return result.toString();
    }

    public static String serializeContent(Symbol symbol) {
        StringBuilder result = new StringBuilder();
        if (symbol.getContent() != null) result.append(symbol.getContent());
        for (Symbol child : symbol.getChildren()) result.append(serializeContent(child));
        return result.toString();
    }

    public static String metaHtml(String message) {
        return "<span class=\"meta\">" + message + "</span>";
    }
}
