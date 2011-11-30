package fitnesse.wikitext.test;

import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

public class StyleTest extends WikiBaseTestCase {
    @Test
    public void scansParenthesisStyle() throws Exception {
        ParserTestHelper.assertScansTokenType("!style_x(my text)", "Style", true, injector);
        ParserTestHelper.assertScansTokenType("!style_style(my text)", "Style", true, injector);
        ParserTestHelper.assertScansTokenType("!style(Hi)", "Style", false, injector);
        ParserTestHelper.assertScansTokenType("!style_(Hi)", "Style", false, injector);
        ParserTestHelper.assertScansTokenType("!style_myStyle(hi))", "Style", true, injector);
    }

    @Test
    public void scansBraceStyle() throws Exception {
        ParserTestHelper.assertScansTokenType("!style_x{my text}", "Style", true, injector);
        ParserTestHelper.assertScansTokenType("!style_style{my text}", "Style", true, injector);
        ParserTestHelper.assertScansTokenType("!style{Hi}", "Style", false, injector);
        ParserTestHelper.assertScansTokenType("!style_{Hi}", "Style", false, injector);
        ParserTestHelper.assertScansTokenType("!style_myStyle{hi}}", "Style", true, injector);
    }

    @Test
    public void scansBracketStyle() throws Exception {
        ParserTestHelper.assertScansTokenType("!style_x[my text]", "Style", true, injector);
        ParserTestHelper.assertScansTokenType("!style_style[my text]", "Style", true, injector);
        ParserTestHelper.assertScansTokenType("!style[Hi]", "Style", false, injector);
        ParserTestHelper.assertScansTokenType("!style_[Hi]", "Style", false, injector);
        ParserTestHelper.assertScansTokenType("!style_myStyle[hi]]", "Style", true, injector);
    }

    @Test
    public void translatesStyle() {
        ParserTestHelper.assertTranslatesTo("!style_myStyle(wow zap)", "<span class=\"myStyle\">wow zap</span>", injector);
        ParserTestHelper.assertTranslatesTo("!style_myStyle[wow zap]", "<span class=\"myStyle\">wow zap</span>", injector);
        ParserTestHelper.assertTranslatesTo("!style_myStyle[)]", "<span class=\"myStyle\">)</span>", injector);
        ParserTestHelper.assertTranslatesTo("!style_myStyle{wow zap}", "<span class=\"myStyle\">wow zap</span>", injector);
    }

    @Test
    public void ignoresMismatchedStyle() {
        ParserTestHelper.assertTranslatesTo("!style_myStyle[stuff)", "!style_myStyle[stuff)", injector);
    }

    @Test
    public void translatesNestedStyle() {
        ParserTestHelper.assertTranslatesTo("!style_myStyle(!style_otherStyle(stuff))",
                "<span class=\"myStyle\"><span class=\"otherStyle\">stuff</span></span>", injector);
    }

    @Test
    public void translatesOverlappedStyle() {
        ParserTestHelper.assertTranslatesTo("!style_red(!style_blue{a)}",
                "!style_red(<span class=\"blue\">a)</span>", injector);
    }
}
