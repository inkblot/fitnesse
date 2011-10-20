// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
//EvaluatorWidget: Test module
package fitnesse.wikitext.widgets;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertSubString;

public class EvaluatorWidgetTest extends WidgetTestCase {
    private PageCrawler crawler;
    private WikiPage page;
    private ParentWidget widgetRoot;
    private WikiPage root;

    @Inject
    public void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();

        String content = "!define ONE {1}\n"
                + "!define TWO {2}\n"
                + "!define FMT {%03x}\n"
                + "!define FmtCOLON {%03X:}";

        page = crawler.addPage(root, PathParser.parse("MyPage"), content);
        widgetRoot = new WidgetRoot("", page);
        Locale.setDefault(Locale.US);
    }

    @Override
    protected String getRegexp() {
        return EvaluatorWidget.REGEXP;
    }

    @Test
    public void testMatches() throws Exception {
        assertMatch("${=X=}");
        assertMatch("${=xyz=}");
        assertMatch("${=  X  =}");
        assertMatch("${= 1 + 1 =}");
        assertMatch("${= ${ONE} + ${TWO} =}");
        assertMatch("${=%d:2.3=}");
        assertMatch("${= %02X : 27 =}");

        assertMatch("${=%30s:123=}");
        assertMatch("${=%-30s:123=}");

        assertMatch("${= %d : 3.2           =}");
        assertMatch("${= %03o : 18 =}");
    }

    @Test
    public void testSimpleTermOneDigit() throws Exception {
        assertEquals("8", new EvaluatorWidget(widgetRoot, "${= 8 =}").render());
    }

    @Test
    public void testSimpleTermMultiDigit() throws Exception {
        assertEquals("42", new EvaluatorWidget(widgetRoot, "${= 42 =}").render());
    }

    @Test
    public void testSimpleTermMultiDigitDecimal() throws Exception {
        assertEquals("42.24", new EvaluatorWidget(widgetRoot, "${= 42.24 =}").render());
    }

    @Test
    public void testSimpleTermScientific() throws Exception {
        assertEquals("1200", new EvaluatorWidget(widgetRoot, "${= 1.2E+3 =}").render());
    }

    @Test
    public void testSimpleTermSigned() throws Exception {
        assertEquals("-123", new EvaluatorWidget(widgetRoot, "${= -123 =}").render());
    }

    @Test
    public void testFormatting() throws Exception {
        assertEquals("3", new EvaluatorWidget(widgetRoot, "${=%d:3.2           =}").render());
        assertEquals("3", new EvaluatorWidget(widgetRoot, "${= %d :3.2           =}").render());
        assertEquals("3", new EvaluatorWidget(widgetRoot, "${= %d:3.2           =}").render());
        assertEquals("3", new EvaluatorWidget(widgetRoot, "${=%d :3.2           =}").render());
        assertSubString("invalid expression: %3 d :3.2           ", new EvaluatorWidget(widgetRoot, "${=%3 d :3.2           =}").render());
        assertEquals("022", new EvaluatorWidget(widgetRoot, "${=%03o: 18 =}").render());
        assertEquals("01b", new EvaluatorWidget(widgetRoot, "${=%03x: 27 =}").render());
        assertEquals("01C", new EvaluatorWidget(widgetRoot, "${=%03X: 28 =}").render());
        assertEquals("0.4041", new EvaluatorWidget(widgetRoot, "${=%5.4f: 0.8082 / 2 =}").render());
    }

    @Test
    public void testAddition() throws Exception {
        assertEquals("3", new EvaluatorWidget(widgetRoot, "${= 1 + 2 =}").render());
    }

    @Test
    public void testAdditionWithNegativeUnarySigns() throws Exception {
        assertEquals("-3", new EvaluatorWidget(widgetRoot, "${= -1 + -2 =}").render());
    }

    @Test
    public void testAdditionWithMixedSigns() throws Exception {
        assertEquals("-1", new EvaluatorWidget(widgetRoot, "${= 1 + -2 =}").render());
    }

    @Test
    public void testSubtraction() throws Exception {
        assertEquals("2", new EvaluatorWidget(widgetRoot, "${= 3 - 1 =}").render());
    }

    @Test
    public void testMultiplication() throws Exception {
        assertEquals("12", new EvaluatorWidget(widgetRoot, "${= 3 * 4 =}").render());
    }

    @Test
    public void testDivision() throws Exception {
        assertEquals("2.5", new EvaluatorWidget(widgetRoot, "${= 5 / 2 =}").render());
    }

    @Test
    public void testExponent() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%d: 3^3 =}");
        assertEquals("27", eval.render());
    }

    @Test
    public void testNegativeExponent() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%.2f: 10.0^-1 =}");
        assertEquals("0.10", eval.render());
    }

    @Test
    public void testNegativeMantissa() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%.2f: (-10.0)^-1 =}");
        assertEquals("-0.10", eval.render());
    }

    @Test
    public void testFractionalExponentAndNegativeMantissaIsNaN() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%.2f: (-10.0)^(1/2) =}");
        assertEquals("NaN", eval.render());
    }

    @Test
    public void testNegativeMantissaAndPositiveIntegralExponent() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%.2f: (-10.0)^2 =}");
        assertEquals("100.00", eval.render());
    }

    @Test
    public void testNegativeMantissaAndNegativeIntegralExponent() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%.2f: (-10.0)^(-2) =}");
        assertEquals("0.01", eval.render());
    }

    @Test
    public void testNegativeMantissaExponentIsZero() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%d: (-10)^0 =}");
        assertEquals("1", eval.render());
    }

    @Test
    public void testSine() throws Exception {
        assertEquals("1.8509", new EvaluatorWidget(widgetRoot, "${=%5.4f: 1 + sin 45 =}").render());
    }

    @Test
    public void testCosine() throws Exception {
        assertEquals("1.1543", new EvaluatorWidget(widgetRoot, "${=%5.4f: 1 + cos 30 =}").render());
    }

    @Test
    public void testTangent() throws Exception {
        assertEquals("-5.4053", new EvaluatorWidget(widgetRoot, "${=%5.4f: 1 + tan 30 =}").render());
    }

    @Test
    public void testParentheses() throws Exception {
        assertEquals("9", new EvaluatorWidget(widgetRoot, "${= (1 + 2) * 3 =}").render());
    }

    @Test
    public void testNoParentheses() throws Exception {
        assertEquals("7", new EvaluatorWidget(widgetRoot, "${= 1 + 2 * 3 =}").render());
    }

    @Test
    public void testInvalidExpression() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= x =}");
        assertSubString("invalid expression:  x ", eval.render());
    }

    @Test
    public void testVariableSubstitutionPlain() throws Exception {
        WikiPage page2 = crawler.addPage(page,
                PathParser.parse("MyVarSubPage"),
                "~vs1:${=${ONE}=}~\n"
                        + "~vs2:${=${ONE}+${TWO}=}~\n"
                        + "~vs3:${= ${ONE} + ${TWO} * ${TWO} =}~\n"
                        + "~vs4:${=(${ONE} + ${TWO}) * ${TWO}=}~\n"
        );

        String result = page2.getData().getHtml();
        assertSubString("~vs1:1~", result);
        assertSubString("~vs2:3~", result);
        assertSubString("~vs3:5~", result);
        assertSubString("~vs4:6~", result);
    }


    @Test
    public void testRenderTwice() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= 2 + 2 =}");
        assertEquals("4", eval.render());
        assertEquals("4", eval.render());
    }

    @Test
    public void testAsWikiText() throws Exception {
        EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= 1 + 2 * 3 / 4 =}");
        assertEquals("${= 1 + 2 * 3 / 4 =}", eval.asWikiText());
    }
}
