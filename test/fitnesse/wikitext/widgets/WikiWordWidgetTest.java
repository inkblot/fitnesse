// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

public class WikiWordWidgetTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private PageCrawler crawler;

    @Inject
    public void inject(@Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
    }

    @Test
    public void testIsSingleWikiWord() throws Exception {
        assertTrue(WikiWordWidget.isSingleWikiWord("WikiWord"));
        assertFalse(WikiWordWidget.isSingleWikiWord("notWikiWord"));
        assertFalse(WikiWordWidget.isSingleWikiWord("NotSingle.WikiWord"));
    }

    @Test
    public void testGoodWikiWordsAreAccepted() throws Exception {
        checkWord(true, "WikiWord");
        checkWord(true, "WordWordWord");
        checkWord(true, "RcM");
        checkWord(true, "WikiWordWithManyWords");
        checkWord(true, "WidgetRoot.ChildPage");
        checkWord(true, "GrandPa.FatheR.SoN");
        checkWord(true, ".RootPage.ChildPage");
        checkWord(true, "^SubPage");
        checkWord(true, "^SubPage.SubPage");
        checkWord(true, ">SubPage");
        checkWord(true, ">SubPage.SubPage");
        checkWord(true, "<MyPage.YourPage");
    }

    @Test
    public void testBadWikiWordsAreRejected() throws Exception {
        checkWord(false, "HelloDDouble");
        checkWord(false, "Hello");
        checkWord(false, "lowerCaseAtStart");
        checkWord(false, ">.MyPage");
        checkWord(false, "RcMMdM");
        checkWord(false, "WikiPage.");
        checkWord(false, "WikiPage. ");
    }

    @Test
    public void testWikiWordsWithSlashAndDotFail() throws Exception {
        checkWord(false, "WikiPage/SubPage");
        checkWord(false, "/WikiPage");
        checkWord(false, "WikiPage/");
        checkWord(false, "./WikiPage");
        checkWord(false, "../WikiPage");
        checkWord(false, "../../WikiPage");

        checkWord(false, "WikiWord/../WikiWord");
        checkWord(false, "./../WikiWord");
        checkWord(false, ".././WikiWord");
        checkWord(false, "WikiWord/./WikiWord");
        checkWord(false, "/../WikiWord");
        checkWord(false, "/./WikiWord");
        checkWord(false, "..");
        checkWord(false, "../..");
    }

    @Test
    public void testWikiWordRegexpWithDigits() throws Exception {
        checkWord(true, "TestPage1");
        checkWord(true, "ParentPage1.SubPage5");
        checkWord(true, "The123Page");
        checkWord(false, "123Page");
        checkWord(false, "Page123");
    }

    @Test
    public void testMakeWikiWord() {
        assertEquals("ExistingWikiWord", WikiWordWidget.makeWikiWord("ExistingWikiWord"));
        assertEquals("LowercaseworD", WikiWordWidget.makeWikiWord("lowercaseword"));
        assertEquals("AaA", WikiWordWidget.makeWikiWord("a"));
        assertEquals("AbA", WikiWordWidget.makeWikiWord("ab"));
        assertEquals("AbC", WikiWordWidget.makeWikiWord("aBc"));
    }

    @Test
    public void testHtmlForNormalLink() throws Exception {
        WikiPage page = addPage(root, "PageOne");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", page, WidgetBuilder.htmlWidgetBuilder), "WikiWord");
        assertEquals(makeExpectedNonExistentWikiWord("WikiWord", "WikiWord"), widget.render());
        page = addPage(root, "WikiWord");
        widget = new WikiWordWidget(new WidgetRoot("", page, WidgetBuilder.htmlWidgetBuilder), "WikiWord");
        assertEquals("<a href=\"WikiWord\">WikiWord</a>", widget.render());
    }

    @Test
    public void testHtmlForSetUpLink() throws Exception {
        WikiPage page = addPage(root, "PageOne");
        WikiPage pageTwo = addPage(page, "PageTwo");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", pageTwo, WidgetBuilder.htmlWidgetBuilder), ">SetUp");
        assertEquals(makeExpectedNonExistentWikiWord("&gt;SetUp", "PageOne.PageTwo.SetUp"), widget.render());
    }

    private String makeExpectedNonExistentWikiWord(String wikiWord, String fullWikiWord) {
        return wikiWord + "<a title=\"create page\" href=\"" + fullWikiWord + "?edit&nonExistent=true\">[?]</a>";
    }

    //todo the ^ widget is deprecated.  Remove it by 7/2007? (DeanW: There is no real point in removing this, as it
    // is "harmless" and it will break some user's tests.)
    @Test
    public void testSubPageWidget() throws Exception {
        WikiPage superPage = addPage(root, "SuperPage");
        PageData data = superPage.getData();
        data.setContent("^SubPage");
        superPage.commit(data);
        String renderedText = superPage.getData().getHtml();
        assertEquals(makeExpectedNonExistentWikiWord("^SubPage", "SuperPage.SubPage"), renderedText);
        addPage(superPage, "SubPage");
        renderedText = superPage.getData().getHtml();
        assertEquals("<a href=\"SuperPage.SubPage\">^SubPage</a>", renderedText);
    }

    @Test
    public void testGTSubPageWidget() throws Exception {
        WikiPage superPage = addPage(root, "SuperPage");
        PageData data = superPage.getData();
        data.setContent(">SubPage");
        superPage.commit(data);
        String renderedText = superPage.getData().getHtml();
        assertEquals(makeExpectedNonExistentWikiWord("&gt;SubPage", "SuperPage.SubPage"), renderedText);
        addPage(superPage, "SubPage");
        renderedText = superPage.getData().getHtml();
        assertEquals("<a href=\"SuperPage.SubPage\">&gt;SubPage</a>", renderedText);
    }

    @Test
    public void testBackwardSearchWidget() throws Exception {
        WikiPage top = addPage(root, "TopPage");
        WikiPage target = addPage(top, "TargetPage");
        WikiPage referer = addPage(target, "ReferingPage");
        @SuppressWarnings("unused")
        WikiPage subTarget = addPage(target, "SubTarget");

        String actual = WikiWordWidget.expandPrefix(referer, "<TargetPage.SubTarget");
        assertEquals(".TopPage.TargetPage.SubTarget", actual);

        actual = WikiWordWidget.expandPrefix(referer, "<NoSuchPage");
        assertEquals(".NoSuchPage", actual);

        PageData data = referer.getData();
        data.setContent("<TargetPage.SubTarget");
        referer.commit(data);
        String renderedLink = referer.getData().getHtml();
        assertEquals("<a href=\"TopPage.TargetPage.SubTarget\">&lt;TargetPage.SubTarget</a>", renderedLink);
    }

    @Test
    public void testHtmlForNormalLinkRegraced() throws Exception {
        WikiPage page = addPage(root, "PageOne");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", page, WidgetBuilder.htmlWidgetBuilder), "Wiki42Word");
        assertEquals(makeExpectedNonExistentWikiWord("Wiki42Word", "Wiki42Word"), widget.render());
        page = addPage(root, "Wiki42Word");
        WidgetRoot root = new WidgetRoot("", page, WidgetBuilder.htmlWidgetBuilder);
        root.addVariable(WikiWordWidget.REGRACE_LINK, "true");
        widget = new WikiWordWidget(root, "Wiki42Word");
        assertEquals("<a href=\"Wiki42Word\">Wiki 42 Word</a>", widget.render());
    }

    @Test
    public void testGTSubPageWidgetRegraced() throws Exception {
        WikiPage superPage = addPage(root, "SuperPage");
        WikiPage childPage = addPage(superPage, "SubPage");

        PageData data = superPage.getData();
        data.setContent("!define " + WikiWordWidget.REGRACE_LINK + " {true}");
        superPage.commit(data);

        data = childPage.getData();
        data.setContent(">Sub123Page");
        childPage.commit(data);

        String renderedText = childPage.getData().getHtml();
        assertEquals(makeExpectedNonExistentWikiWord("&gt;Sub123Page", "SuperPage.SubPage.Sub123Page"), renderedText);

        addPage(childPage, "Sub123Page");
        renderedText = childPage.getData().getHtml();
        assertEquals("<a href=\"SuperPage.SubPage.Sub123Page\">&gt;Sub 123 Page</a>", renderedText);
    }

    @Test
    public void testBackwardSearchWidgetRegraced() throws Exception {
        WikiPage top = addPage(root, "TopPage");
        WikiPage target = addPage(top, "TargetPage");
        WikiPage referer = addPage(target, "ReferingPage");
        @SuppressWarnings("unused")
        WikiPage subTarget = addPage(target, "SubTarget");

        PageData data = top.getData();
        data.setContent("!define " + WikiWordWidget.REGRACE_LINK + " {true}");
        top.commit(data);

        data = referer.getData();
        data.setContent("<TargetPage.SubTarget");
        referer.commit(data);
        String renderedLink = referer.getData().getHtml();
        assertEquals("<a href=\"TopPage.TargetPage.SubTarget\">&lt;Target Page .Sub Target</a>", renderedLink);
    }

    private WikiPage addPage(WikiPage parent, String childName) throws Exception {
        return crawler.addPage(parent, PathParser.parse(childName));
    }

    private void checkWord(boolean expectedMatch, String word) {
        Pattern p = Pattern.compile(WikiWordWidget.REGEXP, Pattern.DOTALL | Pattern.MULTILINE);
        Matcher match = p.matcher(word);
        final boolean matches = match.find();
        final boolean matchEquals = matches && word.equals(match.group(0));
        boolean pass = (matches && matchEquals);
        if (!expectedMatch)
            pass = !pass;

        String failureMessage = word + (matches ? (" found " + (matchEquals ? "" : "but matched " + match.group(0))) : " not found");
        assertTrue(failureMessage, pass);
    }

    @Test
    public void testIsWikiWord() throws Exception {
        assertEquals(true, WikiWordWidget.isWikiWord("HelloThere"));
        assertEquals(false, WikiWordWidget.isWikiWord("not.a.wiki.word"));
    }

    @Test
    public void testAsWikiText() throws Exception {
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", addPage(root, "SomePage"), WidgetBuilder.htmlWidgetBuilder), "OldText");
        assertEquals("OldText", widget.asWikiText());
        widget.setText("NewText");
        assertEquals("NewText", widget.asWikiText());
    }

    @Test
    public void testQualifiedReferenceToSubReference() throws Exception {
        WikiPage myPage = addPage(root, "MyPage");
        addPage(myPage, "SubPage");

        //todo ^ is deprecated, remove by 7/2007
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", myPage, WidgetBuilder.htmlWidgetBuilder), "^SubPage");
        assertEquals(">NewName", widget.makeRenamedRelativeReference(PathParser.parse(".MyPage.NewName")));

        widget = new WikiWordWidget(new WidgetRoot("", myPage, WidgetBuilder.htmlWidgetBuilder), ">SubPage");
        assertEquals(">NewName", widget.makeRenamedRelativeReference(PathParser.parse(".MyPage.NewName")));
    }

    @Test
    public void testQualifiedReferenceToRelativeReference() throws Exception {
        WikiPage myPage = addPage(root, "MyPage");
        addPage(root, "MyBrother");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", myPage, WidgetBuilder.htmlWidgetBuilder), "MyBrother");
        assertEquals("MyBrother", widget.makeRenamedRelativeReference(PathParser.parse(".MyBrother")));

        WikiPage subPageOne = addPage(myPage, "SubPageOne");
        addPage(myPage, "SubPageTwo");
        widget = new WikiWordWidget(new WidgetRoot("", subPageOne, WidgetBuilder.htmlWidgetBuilder), "SubPageTwo");
        assertEquals("SubPageTwo", widget.makeRenamedRelativeReference(PathParser.parse(".MyPage.SubPageTwo")));
    }

    @Test
    public void testRefersTo() throws Exception {
        assertTrue(WikiWordWidget.refersTo(".PageOne", ".PageOne"));
        assertTrue(WikiWordWidget.refersTo(".PageOne.PageTwo", ".PageOne"));
        assertFalse(WikiWordWidget.refersTo(".PageOne.PageTwo", ".PageOne.PageTw"));
    }

    @Test
    public void testSimpleRenamePage() throws Exception {
        WikiPage pageToRename = addPage(root, "OldPageName");
        WikiPage p1 = addPage(root, "PageOne");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", p1, WidgetBuilder.htmlWidgetBuilder), "OldPageName");
        widget.renamePageIfReferenced(pageToRename, "NewPageName");
        assertEquals("NewPageName", widget.getText());
    }

    @Test
    public void testRenamePageInMiddleOfPath() throws Exception {
        WikiPage topPage = addPage(root, "TopPage");
        WikiPage pageToRename = addPage(topPage, "OldPageName");
        @SuppressWarnings("unused")
        WikiPage lastPage = addPage(pageToRename, "LastPage");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", topPage, WidgetBuilder.htmlWidgetBuilder), "TopPage.OldPageName.LastPage");
        widget.renamePageIfReferenced(pageToRename, "NewPageName");
        assertEquals("TopPage.NewPageName.LastPage", widget.getText());
    }

    @Test
    public void testRenamePageInMiddleOfAbsolutePath() throws Exception {
        WikiPage topPage = addPage(root, "TopPage");
        WikiPage pageToRename = addPage(topPage, "OldPageName");
        @SuppressWarnings("unused")
        WikiPage lastPage = addPage(pageToRename, "LastPage");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", topPage, WidgetBuilder.htmlWidgetBuilder), ".TopPage.OldPageName.LastPage");
        widget.renamePageIfReferenced(pageToRename, "NewPageName");
        assertEquals(".TopPage.NewPageName.LastPage", widget.getText());
    }

    @Test
    public void testRenameSubPage() throws Exception {
        WikiPage topPage = addPage(root, "TopPage");
        WikiPage pageToRename = addPage(topPage, "OldPageName");
        @SuppressWarnings("unused")
        WikiPage lastPage = addPage(pageToRename, "LastPage");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", topPage, WidgetBuilder.htmlWidgetBuilder), "^OldPageName.LastPage");
        widget.renamePageIfReferenced(pageToRename, "NewPageName");
        assertEquals(">NewPageName.LastPage", widget.getText());
    }

    @Test
    public void testRenamePageReferencedByBackwardSearch() throws Exception {
        WikiPage topPage = addPage(root, "TopPage");
        WikiPage pageToRename = addPage(topPage, "OldPageName");
        WikiPage lastPage = addPage(pageToRename, "LastPage");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", lastPage, WidgetBuilder.htmlWidgetBuilder), "<TopPage.OldPageName");
        widget.renamePageIfReferenced(pageToRename, "NewPageName");
        assertEquals("<TopPage.NewPageName", widget.getText());
    }

    @Test
    public void testBuildBackwardsSearchReferenceHandlesReferentRename() throws Exception {
        WikiPagePath parent = PathParser.parse(".AaA.BbB.CcC");
        WikiPagePath renamedPathToReferent = PathParser.parse(".AaA.BbB.CcC.NeW");
        assertEquals("<CcC.NeW", WikiWordWidget.buildBackwardSearchReference(parent, renamedPathToReferent));
    }

    @Test
    public void testBuildBackwardsSearchReferenceHandlesReferentRenameOfFirstName() throws Exception {
        WikiPagePath parent = PathParser.parse(".AaA");
        WikiPagePath renamedPathToReferent = PathParser.parse(".RrR.BbB");
        assertEquals("<RrR.BbB", WikiWordWidget.buildBackwardSearchReference(parent, renamedPathToReferent));
    }

    @Test
    public void testRenameMovedPageIfReferenced1() throws Exception {
        WikiPage page1 = addPage(root, "PageOne");
        WikiPage page2 = addPage(root, "PageTwo");

        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", page1, WidgetBuilder.htmlWidgetBuilder), "PageTwo");
        widget.renameMovedPageIfReferenced(page2, "PageOne");
        assertEquals(".PageOne.PageTwo", widget.getText());
    }

    @Test
    public void testRenameMovedPageIfReferenced2() throws Exception {
        WikiPage page1 = addPage(root, "PageOne");
        WikiPage page2 = addPage(page1, "PageTwo");

        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", page1, WidgetBuilder.htmlWidgetBuilder), ">PageTwo");
        widget.renameMovedPageIfReferenced(page2, "");
        assertEquals(".PageTwo", widget.getText());
    }

    //TODO -MDM- bug I descovered while trying to refactor.
    @Test
    public void testmakeRenamedRelativeReference() throws Exception {
        addPage(root, "FitNesse");
        addPage(root, "FitNesse.SuiteAcceptanceTests");
        WikiPage parentPage = addPage(root, "FitNesse.SuiteAcceptanceTests.SuiteWikiPageResponderTests");
        WikiWordWidget widget = new WikiWordWidget(new WidgetRoot("", parentPage, WidgetBuilder.htmlWidgetBuilder), "WikiWord");
        widget.parentPage = parentPage;

        try {
            widget.makeRenamedRelativeReference(PathParser.parse(".FitNesse.SuiteAcceptanceTests.SuiteWidgetTests.WikiWord"));
            //Not sure what the result should be but it's the exception that's causing trouble.
        } catch (Exception e) {
            fail(e.getMessage());
        }

    }
}
