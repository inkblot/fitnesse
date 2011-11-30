package fitnesse.wikitext.test;

import com.google.inject.Inject;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

import static fitnesse.wikitext.test.ParserTestHelper.assertParses;
import static org.junit.Assert.assertTrue;

public class IncludeTest extends WikiBaseTestCase {

    private TestRoot root;

    @Inject
    public void inject(TestRoot testRoot) {
        this.root = testRoot;
    }

    @Test
    public void scansIncludes() {
        ParserTestHelper.assertScansTokenType("!include name", "Include", true, injector);
    }

    @Test
    public void parsesIncludes() throws Exception {
        assertParses("!include PageTwo\n", "SymbolList[Include[Text, WikiWord, Meta[Text]], Newline]", injector);
        assertParses("|!include PageTwo|\n", "SymbolList[Table[SymbolList[SymbolList[Include[Text, WikiWord, Meta[Text]]]]]]", injector);
        assertParses("!include PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]", injector);
        assertParses("!include -c PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]", injector);
        assertParses("!include <PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]", injector);
        assertParses("!include <PageTwo>", "SymbolList[Include[Text, WikiWord, Meta[Text]], Text]", injector);
        assertParses("!include -setup PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]", injector);
        assertParses("!include -teardown PageTwo", "SymbolList[Include[Text, WikiWord, Meta[Text]]]", injector);
    }

    @Test
    public void translatesIncludedSibling() throws Exception {
        WikiPage currentPage = root.makePage("PageOne", "!include PageTwo");
        root.makePage("PageTwo", "page ''two''");

        String result = ParserTestHelper.translateTo(currentPage);

        assertContains(result, "class=\"collapsable\"");
        assertContains(result, "Included page: <a href=\"PageTwo\">PageTwo</a> <a href=\"PageTwo?edit&amp;redirectToReferer=true&amp;redirectAction=\">(edit)</a>");
        assertContains(result, "page <i>two</i>");
    }

    @Test
    public void translatesIncludeWithChildReference() throws Exception {
        WikiPage currentPage = root.makePage("PageOne", "!include PageTwo");
        WikiPage pageTwo = root.makePage("PageTwo", ">PageTwoChild");
        root.makePage(pageTwo, "PageTwoChild", "stuff");
        String result = ParserTestHelper.translateTo(currentPage);
        assertContains(result, "PageTwo.PageTwoChild");
    }

    @Test
    public void translatesRelativeInclude() throws Exception {
        WikiPage currentPage = root.makePage("PageOne", "!include >PageOneChild");
        root.makePage(currentPage, "PageOneChild", "stuff");
        String result = ParserTestHelper.translateTo(currentPage);
        assertContains(result, "stuff");
    }

    @Test
    public void translatesNestedRelativeInclude() throws Exception {
        WikiPage currentPage = root.makePage("PageOne", "!include >PageOneChild");
        WikiPage pageOneChild = root.makePage(currentPage, "PageOneChild", "!include >PageOneGrandChild");
        root.makePage(pageOneChild, "PageOneGrandChild", "stuff");
        String result = ParserTestHelper.translateTo(currentPage);
        assertContains(result, "stuff");
    }

    @Test
    public void setupsAreHidden() throws Exception {
        String result = ParserTestHelper.translateTo(makePageThatIncludesSetup());

        assertContains(result, "class=\"hidden\"");
        assertContains(result, "<a href=\"PageTwo.SetUp\">");
    }

    @Test
    public void teardownsAreHidden() throws Exception {
        String result = ParserTestHelper.translateTo(makePageThatIncludesTeardown());

        assertContains(result, "class=\"hidden\"");
        assertContains(result, "<a href=\"PageTwo.TearDown\">");
    }

    private TestSourcePage makePageThatIncludesSetup() {
        return injector.getInstance(TestSourcePage.class)
                .withContent("!include -setup >SetUp")
                .withTarget("PageTwo.SetUp")
                .withIncludedPage(injector.getInstance(TestSourcePage.class).withContent("setup"));
    }

    private TestSourcePage makePageThatIncludesTeardown() {
        return injector.getInstance(TestSourcePage.class)
                .withContent("!include -teardown >TearDown")
                .withTarget("PageTwo.TearDown")
                .withIncludedPage(injector.getInstance(TestSourcePage.class).withContent("teardown"));
    }

    @Test
    public void translatesSetupWithoutCollapse() throws Exception {
        String result = ParserTestHelper.translateTo(makePageThatIncludesSetup(), new TestVariableSource("COLLAPSE_SETUP", "false"));

        assertContains(result, "class=\"collapsable\"");
        assertContains(result, "<a href=\"PageTwo.SetUp\">");
    }

    @Test
    public void translatesCollapsed() throws Exception {
        WikiPage includingPage = root.makePage("PageOne", "!include -c PageTwo");
        root.makePage("PageTwo", "two");

        String result = ParserTestHelper.translateTo(includingPage);

        assertContains(result, "class=\"hidden\"");
    }

    @Test
    public void translatesSeamless() throws Exception {
        WikiPage includingPage = root.makePage("PageOne", "!include -seamless PageTwo");
        root.makePage("PageTwo", "two");

        ParserTestHelper.assertTranslatesTo(includingPage, "two");
    }

    @Test
    public void doesNotIncludeParent() throws Exception {
        WikiPage parent = root.makePage("ParentPage", "stuff");
        WikiPage currentPage = root.makePage(parent, "PageOne", "!include <ParentPage");
        ParserTestHelper.assertTranslatesTo(currentPage,
                "<span class=\"meta\">Error! Cannot include parent page (&lt;ParentPage).\n</span>");

    }

    private void assertContains(String result, String substring) {
        assertTrue(result, result.contains(substring));
    }

}
