package fitnesse.wikitext.test;

import com.google.inject.Inject;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import fitnesse.wikitext.WikiWordUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class WikiWordTest extends WikiBaseTestCase {
    private TestRoot root;
    private WikiPage pageOne;
    private WikiPage pageOneTwo;
    private WikiPage pageOneTwoThree;
    private WikiPage pageOneThree;

    @Inject
    public void inject(TestRoot root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        pageOne = root.makePage("PageOne");
        pageOneTwo = root.makePage(pageOne, "PageOne2");
        pageOneTwoThree = root.makePage(pageOneTwo, "PageThree");
        pageOneThree = root.makePage(pageOne, "PageThree");
    }

    @Test
    public void translatesWikiWords() throws Exception {
        ParserTestHelper.assertTranslatesTo(pageOne, "PageOne", wikiLink("PageOne", "PageOne"));
        ParserTestHelper.assertTranslatesTo(pageOneTwo, "PageOne2", wikiLink("PageOne.PageOne2", "PageOne2"));
        ParserTestHelper.assertTranslatesTo(pageOneThree, ".PageOne", wikiLink("PageOne", ".PageOne"));
        ParserTestHelper.assertTranslatesTo(pageOne, ">PageOne2", wikiLink("PageOne.PageOne2", "&gt;PageOne2"));
        ParserTestHelper.assertTranslatesTo(pageOneTwoThree, "<PageOne", wikiLink("PageOne", "&lt;PageOne"));
    }

    @Test
    public void translatesMissingWikiWords() throws Exception {
        ParserTestHelper.assertTranslatesTo(pageOne, "PageNine",
                "PageNine<a title=\"create page\" href=\"PageNine?edit&nonExistent=true\">[?]</a>");
    }

    @Test
    public void regracesWikiWords() throws Exception {
        root.setPageData(pageOne, "!define " + WikiWordUtil.REGRACE_LINK + " {true}\nPageOne\n!define " + WikiWordUtil.REGRACE_LINK + " {false}\n");
        assertTrue(ParserTestHelper.translateTo(pageOne).contains(wikiLink("PageOne", "Page One")));
    }

    private String wikiLink(String link, String text) {
        return "<a href=\"" + link + "\">" + text + "</a>";
    }
}
