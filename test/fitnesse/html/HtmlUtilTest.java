package fitnesse.html;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static util.RegexAssertions.assertSubString;

public class HtmlUtilTest extends WikiBaseTestCase {
    private static final String endl = HtmlElement.endl;

    private WikiPage root;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Test
    public void testBreadCrumbsWithCurrentPageLinked() throws Exception {
        String trail = "1.2.3.4";
        HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithCurrentPageLinked(trail);
        String expected = getBreadCrumbsWithLastOneLinked();
        assertEquals(expected, breadcrumbs.html());
    }

    @Test
    public void testBreadCrumbsWithCurrentPageNotLinked() throws Exception {
        String trail = "1.2.3.4";
        HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(trail);
        String expected = getBreadCrumbsWithLastOneNotLinked();
        assertEquals(expected, breadcrumbs.html());
    }

    @Test
    public void testBreadCrumbsWithPageType() throws Exception {
        String trail = "1.2.3.4";
        HtmlTag breadcrumbs = HtmlUtil.makeBreadCrumbsWithPageType(trail, "Some Type");
        String expected = getBreadCrumbsWithLastOneLinked() +
                "<br/><span class=\"page_type\">Some Type</span>" + endl;
        assertEquals(expected, breadcrumbs.html());
    }

    private String getBreadCrumbsWithLastOneLinked() {
        return getFirstThreeBreadCrumbs() +
                "<br/><a href=\"/1.2.3.4\" class=\"page_title\">4</a>" + endl;
    }

    private String getBreadCrumbsWithLastOneNotLinked() {
        return getFirstThreeBreadCrumbs() +
                "<br/><span class=\"page_title\">4</span>" + endl;
    }

    private String getFirstThreeBreadCrumbs() {
        return "<a href=\"/1\">1</a>." + endl +
                "<a href=\"/1.2\">2</a>." + endl +
                "<a href=\"/1.2.3\">3</a>." + endl;
    }

    @Test
    public void testMakeFormTag() throws Exception {
        HtmlTag formTag = HtmlUtil.makeFormTag("method", "action");
        assertSubString("method", formTag.getAttribute("method"));
        assertSubString("action", formTag.getAttribute("action"));
    }

    @Test
    public void testMakeDivTag() throws Exception {
        String expected = "<div class=\"myClass\"></div>" + HtmlElement.endl;
        assertEquals(expected, HtmlUtil.makeDivTag("myClass").html());
    }

    @Test
    public void testMakeBreadCrumbsWithCurrentPageLinkedWithEmptyArray() throws Exception {
        try {
            HtmlUtil.makeBreadCrumbsWithCurrentPageLinked(".");
            HtmlUtil.makeBreadCrumbsWithCurrentPageLinked("");
        } catch (Exception e) {
            fail("should not throw exception");
        }
    }

    @Test
    public void testMakeDefaultActions() throws Exception {
        String pageName = "SomePage";
        String html = getActionsHtml(pageName);
        verifyDefaultLinks(html, "SomePage");
    }

    @Test
    public void testMakeActionsWithTestButtonWhenNameStartsWithTest() throws Exception {
        String pageName = "TestSomething";
        String html = getActionsHtml(pageName);
        verifyDefaultLinks(html, pageName);
        assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
    }

    @Test
    public void testMakeActionsWithSuffixButtonWhenNameEndsWithTest() throws Exception {
        String pageName = "SomethingTest";
        String html = getActionsHtml(pageName);
        verifyDefaultLinks(html, pageName);
        assertSubString("<a href=\"" + pageName + "?test\" accesskey=\"t\">Test</a>", html);
    }

    @Test
    public void testMakeActionsWithSuiteButtonWhenNameStartsWithSuite() throws Exception {
        String pageName = "SuiteNothings";
        String html = getActionsHtml(pageName);
        verifyDefaultLinks(html, pageName);
        assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
    }

    @Test
    public void testMakeActionsWithSuiteButtonWhenNameEndsWithSuite() throws Exception {
        String pageName = "NothingsSuite";
        String html = getActionsHtml(pageName);
        verifyDefaultLinks(html, pageName);
        assertSubString("<a href=\"" + pageName + "?suite\" accesskey=\"\">Suite</a>", html);
    }

    private String getActionsHtml(String pageName) throws Exception {
        root.addChildPage(pageName);
        return HtmlUtil.makeActions(root.getChildPage(pageName).getActions()).html();
    }

    private void verifyDefaultLinks(String html, String pageName) {
        assertSubString("<a href=\"" + pageName + "?edit\" accesskey=\"e\">Edit</a>", html);
        assertSubString("<a href=\"" + pageName + "?versions\" accesskey=\"v\">Versions</a>", html);
        assertSubString("<a href=\"" + pageName + "?properties\" accesskey=\"p\">Properties</a>", html);
        assertSubString("<a href=\"" + pageName + "?refactor\" accesskey=\"r\">Refactor</a>", html);
        assertSubString("<a href=\"" + pageName + "?whereUsed\" accesskey=\"w\">Where Used</a>", html);
        assertSubString("<a href=\"/files\" accesskey=\"f\">Files</a>", html);
        assertSubString("<a href=\"?searchForm\" accesskey=\"s\">Search</a>", html);
        assertSubString("<a href=\".FitNesse.UserGuide\" accesskey=\"\">User Guide</a>", html);
    }

    @Test
    public void testMakeReplaceElementScript() throws Exception {
        String newText = "<p>My string has \"quotes\" and \r \n</p>";
        HtmlTag scriptTag = HtmlUtil.makeReplaceElementScript("element-name", newText);
        String expected = "<script>document.getElementById(\"element-name\").innerHTML = " +
                "\"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";</script>";
        assertSubString(expected, scriptTag.html());
    }

    @Test
    public void testMakeAppendElementScript() throws Exception {
        String appendText = "<p>My string has \"quotes\" and \r \n</p>";
        HtmlTag scriptTag = HtmlUtil.makeAppendElementScript("element-name", appendText);
        String expected1 = "<script>var existingContent = document.getElementById(\"element-name\").innerHTML;";
        String expected2 = "document.getElementById(\"element-name\").innerHTML = " +
                "existingContent + \"<p>My string has \\\"quotes\\\" and \\r \\n</p>\";";
        String expected3 = "</script>";
        assertSubString(expected1, scriptTag.html());
        assertSubString(expected2, scriptTag.html());
        assertSubString(expected3, scriptTag.html());
    }

    @Test
    public void testMakeSilentLink() throws Exception {
        HtmlTag tag = HtmlUtil.makeSilentLink("test?responder", new RawHtml("string with \"quotes\""));
        assertSubString("<a href=\"#\" onclick=\"doSilentRequest('test?responder')\">string with \"quotes\"</a>", tag.html());
    }
}
