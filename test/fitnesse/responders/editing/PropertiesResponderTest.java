// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.WikiImportProperty;
import fitnesse.wiki.*;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.*;

public class PropertiesResponderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private PageCrawler crawler;
    private MockRequest request;
    private Responder responder;
    private String content;
    private FitNesseContext context;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
    }

    @Test
    public void testResponse() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
        PageData data = page.getData();
        data.setContent("some content");
        WikiPageProperties properties = data.getProperties();
        properties.set("Test", "true");
        properties.set(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://www.fitnesse.org");
        page.commit(data);

        MockRequest request = new MockRequest();
        request.setResource("PageOne");

        Responder responder = new PropertiesResponder(htmlPageFactory, root);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals("max-age=0", response.getHeader("Cache-Control"));

        String content = response.getContent();
        assertSubString("PageOne", content);
        assertSubString("value=\"http://www.fitnesse.org\"", content);
        assertDoesNotHaveRegexp("textarea name=\"extensionXml\"", content);
        assertHasRegexp("<input.*value=\"Save Properties\".*>", content);

        assertHasRegexp("<input.*value=\"saveProperties\"", content);
        for (String attribute : new String[]{"Search", "Edit", "Properties", "Versions", "Refactor", "WhereUsed", "RecentChanges"})
            assertCheckboxChecked(attribute, content);

        for (String attribute : new String[]{"Prune", PageData.PropertySECURE_READ, PageData.PropertySECURE_WRITE, PageData.PropertySECURE_TEST})
            assertCheckboxNotChecked(content, attribute);
    }

    private void assertCheckboxNotChecked(String content, String attribute) {
        assertSubString("<input type=\"checkbox\" name=\"" + attribute + "\"/>", content);
    }

    private void assertCheckboxChecked(String attribute, String content) {
        assertSubString("<input type=\"checkbox\" name=\"" + attribute + "\" checked=\"true\"/>", content);
    }

    @Test
    public void testJsonResponse() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
        PageData data = page.getData();
        data.setContent("some content");
        WikiPageProperties properties = data.getProperties();
        properties.set("Test", "true");
        page.commit(data);

        MockRequest request = new MockRequest();
        request.setResource("PageOne");
        request.addInput("format", "json");

        Responder responder = new PropertiesResponder(htmlPageFactory, root);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        assertEquals("text/json", response.getContentType());
        String jsonText = response.getContent();
        JSONObject jsonObject = new JSONObject(jsonText);
        assertTrue(jsonObject.getBoolean("Test"));
        assertTrue(jsonObject.getBoolean("Search"));
        assertTrue(jsonObject.getBoolean("Edit"));
        assertTrue(jsonObject.getBoolean("Properties"));
        assertTrue(jsonObject.getBoolean("Versions"));
        assertTrue(jsonObject.getBoolean("Refactor"));
        assertTrue(jsonObject.getBoolean("WhereUsed"));
        assertTrue(jsonObject.getBoolean("RecentChanges"));

        assertFalse(jsonObject.getBoolean("Suite"));
        assertFalse(jsonObject.getBoolean("Prune"));
        assertFalse(jsonObject.getBoolean(PageData.PropertySECURE_READ));
        assertFalse(jsonObject.getBoolean(PageData.PropertySECURE_WRITE));
        assertFalse(jsonObject.getBoolean(PageData.PropertySECURE_TEST));
    }

    @Test
    public void testGetVirtualWikiValue() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("PageOne"));
        PageData data = page.getData();

        assertEquals("", PropertiesResponder.getVirtualWikiValue(data));

        data.setAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE, "http://www.objectmentor.com");
        assertEquals("http://www.objectmentor.com", PropertiesResponder.getVirtualWikiValue(data));
    }

    @Test
    public void testUsernameDisplayed() throws Exception {
        WikiPage page = getContentFromSimplePropertiesPage();

        assertSubString("Last modified anonymously", content);

        PageData data = page.getData();
        data.setAttribute(PageData.LAST_MODIFYING_USER, "Bill");
        page.commit(data);

        request.setResource("SomePage");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        content = response.getContent();

        assertSubString("Last modified by Bill", content);
    }

    private WikiPage getContentFromSimplePropertiesPage() throws Exception {
        WikiPage page = root.addChildPage("SomePage");

        return getPropertiesContentFromPage(page);
    }

    private WikiPage getPropertiesContentFromPage(WikiPage page) throws Exception {
        request = new MockRequest();
        request.setResource(page.getName());
        responder = new PropertiesResponder(htmlPageFactory, root);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        content = response.getContent();
        return page;
    }

    @Test
    public void testWikiImportForm() throws Exception {
        getContentFromSimplePropertiesPage();

        checkUpdateForm();
        assertSubString("Wiki Import.", content);
        assertSubString("value=\"Import\"", content);
        assertSubString("type=\"text\"", content);
        assertSubString("name=\"remoteUrl\"", content);
    }

    private void checkUpdateForm() {
        assertSubString("<form", content);
        assertSubString("action=\"SomePage#end\"", content);
        assertSubString("<input", content);
        assertSubString("type=\"hidden\"", content);
        assertSubString("name=\"responder\"", content);
        assertSubString("value=\"import\"", content);
    }

    @Test
    public void testWikiImportUpdate() throws Exception {
        WikiImportProperty property = new WikiImportProperty("http://my.host.com/PageRoot");
        property.setRoot(true);
        testWikiImportUpdateWith(property);
        assertSubString(" imports its subpages from ", content);
        assertSubString("value=\"Update Subpages\"", content);

        assertSubString("Automatically update imported content when executing tests", content);
    }

    @Test
    public void testWikiImportUpdateNonroot() throws Exception {
        testWikiImportUpdateWith(new WikiImportProperty("http://my.host.com/PageRoot"));
        assertSubString(" imports its content and subpages from ", content);
        assertSubString("value=\"Update Content and Subpages\"", content);

        assertSubString("Automatically update imported content when executing tests", content);
    }

    private void testWikiImportUpdateWith(WikiImportProperty property) throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        property.addTo(data.getProperties());
        page.commit(data);

        getPropertiesContentFromPage(page);
        checkUpdateForm();
        assertSubString("Wiki Import Update", content);
        assertSubString("<a href=\"http://my.host.com/PageRoot\">http://my.host.com/PageRoot</a>", content);

        assertNotSubString("value=\"Import\"", content);
    }

    @Test
    public void testSymbolicLinkForm() throws Exception {
        getContentFromSimplePropertiesPage();

        assertSubString("Symbolic Links", content);
        assertSubString("<input type=\"hidden\" name=\"responder\" value=\"symlink\"", content);
        assertSubString("<input type=\"text\" name=\"linkName\"", content);
        assertSubString("<input type=\"text\" name=\"linkPath\"", content);
        assertSubString("<input type=\"submit\" name=\"submit\" value=\"Create/Replace\"", content);
    }

    @Test
    public void testSymbolicLinkListing() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        page.addChildPage("SomeChild");
        WikiPage pageOne = root.addChildPage("PageOne"); //...page must exist!
        pageOne.addChildPage("ChildOne");                //...page must exist!

        PageData data = page.getData();
        WikiPageProperties props = data.getProperties();
        WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
        symProp.set("InternalAbsPage", ".PageOne.ChildOne");
        symProp.set("InternalRelPage", "PageOne.ChildOne");
        symProp.set("InternalSubPage", ">SomeChild");
        symProp.set("ExternalPage", "file://some/page");
        page.commit(data);

        getPropertiesContentFromPage(page);

        assertSubString("<td>InternalAbsPage</td>", content);
        assertSubString("<input type=\"text\" name=\"InternalAbsPage\"", content);
        assertSubString("<a href=\".PageOne.ChildOne\">.PageOne.ChildOne</a>", content);
        assertMatches("<a href=\".*\">&nbsp;Rename:</a>", content);

        assertSubString("<td>InternalRelPage</td>", content);
        assertSubString("<input type=\"text\" name=\"InternalRelPage\"", content);
        assertSubString("<a href=\".PageOne.ChildOne\">PageOne.ChildOne</a>", content);

        assertSubString("<td>InternalSubPage</td>", content);
        assertSubString("<input type=\"text\" name=\"InternalSubPage\"", content);
        assertSubString("<a href=\".SomePage.SomeChild\">&gt;SomeChild</a>", content);

        assertSubString("<td>file://some/page</td>", content);
    }

    @Test
    public void testSymbolicLinkListingForBackwardPath() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        WikiPage child = page.addChildPage("SomeChild");
        page.addChildPage("OtherChild");

        PageData data = child.getData();
        WikiPageProperties props = data.getProperties();
        WikiPageProperty symProp = props.set(SymbolicPage.PROPERTY_NAME);
        symProp.set("InternalBackPage", "<SomePage.OtherChild");
        page.commit(data);

        getPropertiesContentFromPage(page);

        assertSubString("InternalBackPage", content);
        assertSubString("<a href=\".SomePage.OtherChild\">&lt;SomePage.OtherChild</a>", content);
    }

    @Test
    public void testPageTypePropertiesHtml() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        String html = new PropertiesResponder(htmlPageFactory, root).makePageTypeRadiosHtml(data).html();
        assertSubString("<div style=\"float: left; width: 150px;\">Page type:", html);
        assertSubString("Page type:", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Normal\" checked=\"checked\"/> - Normal", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Test\"/> - Test", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Suite\"/> - Suite", html);
    }

    @Test
    public void testPageTypePropertiesSuiteHtml() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        data.setAttribute("Suite");
        String html = new PropertiesResponder(htmlPageFactory, root).makePageTypeRadiosHtml(data).html();
        assertSubString("<div style=\"float: left; width: 150px;\">Page type:", html);
        assertSubString("Page type:", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Normal\"/> - Normal", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Test\"/> - Test", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Suite\" checked=\"checked\"/> - Suite", html);
    }

    @Test
    public void testPageTypePropertiesTestHtml() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        data.setAttribute("Test");
        String html = new PropertiesResponder(htmlPageFactory, root).makePageTypeRadiosHtml(data).html();
        assertSubString("<div style=\"float: left; width: 150px;\">Page type:", html);
        assertSubString("Page type:", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Normal\"/> - Normal", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Test\" checked=\"checked\"/> - Test", html);
        assertSubString("<input type=\"radio\" name=\"PageType\" value=\"Suite\"/> - Suite", html);
    }

    @Test
    public void testActionPropertiesHtml() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        String html = new PropertiesResponder(htmlPageFactory, root).makeTestActionCheckboxesHtml(data).html();
        assertSubString("<div style=\"float: left; width: 180px;\">Actions:", html);
        assertSubString("Actions:", html);
        assertSubString("<input type=\"checkbox\" name=\"Edit\" checked=\"true\"/> - Edit", html);
        assertSubString("<input type=\"checkbox\" name=\"Versions\" checked=\"true\"/> - Versions", html);
        assertSubString("<input type=\"checkbox\" name=\"Properties\" checked=\"true\"/> - Properties", html);
        assertSubString("<input type=\"checkbox\" name=\"Refactor\" checked=\"true\"/> - Refactor", html);
        assertSubString("<input type=\"checkbox\" name=\"WhereUsed\" checked=\"true\"/> - WhereUsed", html);
    }

    @Test
    public void testMakeNavigationPropertiesHtml() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        String html = new PropertiesResponder(htmlPageFactory, root).makeNavigationCheckboxesHtml(data).html();
        assertSubString("<div style=\"float: left; width: 180px;\">Navigation:", html);
        assertSubString("<input type=\"checkbox\" name=\"Files\" checked=\"true\"/> - Files", html);
        assertSubString("<input type=\"checkbox\" name=\"RecentChanges\" checked=\"true\"/> - RecentChanges", html);
        assertSubString("<input type=\"checkbox\" name=\"Search\" checked=\"true\"/> - Search", html);
        assertSubString("<input type=\"checkbox\" name=\"Prune\"/> - Prune", html);
    }

    @Test
    public void testMakeSecurityPropertiesHtml() throws Exception {
        WikiPage page = root.addChildPage("SomePage");
        PageData data = page.getData();
        String html = new PropertiesResponder(htmlPageFactory, root).makeSecurityCheckboxesHtml(data).html();
        assertSubString("<div style=\"float: left; width: 180px;\">Security:", html);
        assertSubString("<input type=\"checkbox\" name=\"secure-read\"/> - secure-read", html);
        assertSubString("<input type=\"checkbox\" name=\"secure-write\"/> - secure-write", html);
        assertSubString("<input type=\"checkbox\" name=\"secure-test\"/> - secure-test", html);
    }

    @Test
    public void testEmptySuitesForm() throws Exception {
        getContentFromSimplePropertiesPage();

        assertSubString("Suites", content);
        assertSubString("<input type=\"text\" name=\"Suites\" value=\"\" size=\"40\"/>", content);
    }

    @Test
    public void testSuitesDisplayed() throws Exception {
        WikiPage page = getContentFromSimplePropertiesPage();
        PageData data = page.getData();
        data.setAttribute(PageData.PropertySUITES, "smoke");
        page.commit(data);

        getPropertiesContentFromPage(page);

        assertSubString("Suites", content);
        assertSubString("<input type=\"text\" name=\"Suites\" value=\"smoke\" size=\"40\"/>", content);
    }

    @Test
    public void testEmptyHelpTextForm() throws Exception {
        getContentFromSimplePropertiesPage();

        assertSubString("Help Text", content);
        assertSubString("<input type=\"text\" name=\"HelpText\" value=\"\" size=\"90\"/>", content);
    }

    @Test
    public void testHelpTextDisplayed() throws Exception {
        WikiPage page = getContentFromSimplePropertiesPage();
        PageData data = page.getData();
        data.setAttribute(PageData.PropertyHELP, "help text");
        page.commit(data);

        getPropertiesContentFromPage(page);

        assertSubString("Help Text", content);
        assertSubString("<input type=\"text\" name=\"HelpText\" value=\"help text\" size=\"90\"/>", content);
    }

}
