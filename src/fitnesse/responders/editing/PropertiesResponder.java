// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;
import org.json.JSONObject;

import java.util.Set;

import static fitnesse.wiki.PageData.*;
import static fitnesse.wiki.PageType.SUITE;
import static fitnesse.wiki.PageType.TEST;
import static org.apache.commons.lang.StringUtils.isEmpty;

public class PropertiesResponder implements SecureResponder {
    private WikiPage page;
    public PageData pageData;
    private String resource;
    private SimpleResponse response;
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public PropertiesResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(Request request)
            throws Exception {
        response = new SimpleResponse();
        resource = request.getResource();
        WikiPagePath path = PathParser.parse(resource);
        PageCrawler crawler = root.getPageCrawler();
        if (!crawler.pageExists(root, path))
            crawler.setDeadEndStrategy(new MockingPageCrawler());
        page = crawler.getPage(root, path);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(request);

        pageData = page.getData();
        makeContent(request);
        response.setMaxAge(0);
        return response;
    }

    private void makeContent(Request request)
            throws Exception {
        if ("json".equals(request.getInput("format"))) {
            JSONObject jsonObject = makeJson();
            response.setContent(jsonObject.toString(1));
        } else {
            String html = makeHtml();
            response.setContent(html);
        }
    }

    private JSONObject makeJson() throws Exception {
        response.setContentType("text/json");
        JSONObject jsonObject = new JSONObject();
        String attributes[] = new String[]{TEST.toString(), PropertySEARCH,
                PropertyEDIT, PropertyPROPERTIES, PropertyVERSIONS, PropertyREFACTOR,
                PropertyWHERE_USED, PropertyRECENT_CHANGES, SUITE.toString(),
                PropertyPRUNE, PropertySECURE_READ, PropertySECURE_WRITE,
                PropertySECURE_TEST};
        for (String attribute : attributes)
            addJsonAttribute(jsonObject, attribute);

        return jsonObject;
    }

    private void addJsonAttribute(JSONObject jsonObject, String attribute)
            throws Exception {
        jsonObject.put(attribute, pageData.hasAttribute(attribute));
    }

    private String makeHtml() throws Exception {
        HtmlPage page = htmlPageFactory.newPage();
        page.title.use("Properties: " + resource);
        page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource,
                "Page Properties"));
        page.main.use(makeLastModifiedTag());
        page.main.add(makeFormSections());

        return page.html();
    }

    private HtmlTag makeAttributeCheckbox(String attribute, PageData pageData)
            throws Exception {
        HtmlTag checkbox = makeCheckbox(attribute);
        if (pageData.hasAttribute(attribute))
            checkbox.addAttribute("checked", "true");
        return checkbox;
    }

    private HtmlTag makeCheckbox(String attribute) {
        HtmlTag checkbox = HtmlUtil.makeInputTag("checkbox", attribute);
        checkbox.tail = " - " + attribute;
        return checkbox;
    }

    private HtmlTag makeLastModifiedTag() throws Exception {
        HtmlTag tag = HtmlUtil.makeDivTag("right");
        String username = pageData.getAttribute(LAST_MODIFYING_USER);
        if (isEmpty(username))
            tag.use("Last modified anonymously");
        else
            tag.use("Last modified by " + username);
        return tag;
    }

    private HtmlTag makeFormSections() throws Exception {
        TagGroup html = new TagGroup();
        html.add(makePropertiesForm());
        html.add(makeSymbolicLinkSection());
        return html;
    }

    private HtmlTag makePropertiesForm() throws Exception {
        HtmlTag form = HtmlUtil.makeFormTag("post", resource);
        form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveProperties"));

        HtmlTag trisection = new HtmlTag("div");
        trisection.addAttribute("style", "width:100%");
        HtmlTag checkBoxesSection = new HtmlTag("div");
        checkBoxesSection.addAttribute("class", "properties");
        checkBoxesSection.add(makePageTypeRadiosHtml(pageData));
        checkBoxesSection.add(makeTestActionCheckboxesHtml(pageData));
        checkBoxesSection.add(makeNavigationCheckboxesHtml(pageData));
        checkBoxesSection.add(makeSecurityCheckboxesHtml(pageData));
        HtmlTag virtualWikiSection = new HtmlTag("div");
        virtualWikiSection.addAttribute("class", "virtual-wiki-properties");
        virtualWikiSection.add(makeTagsHtml(pageData));
        virtualWikiSection.add(makeHelpTextHtml(pageData));
        trisection.add(checkBoxesSection);
        trisection.add(virtualWikiSection);
        form.add(trisection);

        HtmlTag buttonSection = new HtmlTag("div");
        buttonSection.add(HtmlUtil.BR);
        HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "Save",
                "Save Properties");
        saveButton.addAttribute("accesskey", "s");
        buttonSection.add(saveButton);
        form.add(buttonSection);
        return form;
    }

    public HtmlTag makePageTypeRadiosHtml(PageData pageData) throws Exception {
        return makeAttributeRadiosHtml("Page type: ",
                PAGE_TYPE_ATTRIBUTES, PAGE_TYPE_ATTRIBUTE, pageData);
    }

    private HtmlTag makeAttributeRadiosHtml(String label, String[] attributes,
                                            String radioGroup, PageData pageData) throws Exception {
        HtmlTag div = new HtmlTag("div");
        div.addAttribute("style", "float: left; width: 150px;");

        div.add(label);
        String checkedAttribute = getCheckedAttribute(pageData, attributes);
        for (String attribute : attributes) {
            div.add(HtmlUtil.BR);
            div.add(makeAttributeRadio(radioGroup, attribute, attribute
                    .equals(checkedAttribute)));
        }
        div.add(HtmlUtil.BR);
        div.add(HtmlUtil.BR);
        return div;
    }

    private String getCheckedAttribute(PageData pageData, String[] attributes)
            throws Exception {
        for (int i = attributes.length - 1; i > 0; i--) {
            if (pageData.hasAttribute(attributes[i]))
                return attributes[i];
        }
        return attributes[0];
    }

    private HtmlTag makeAttributeRadio(String group, String attribute,
                                       boolean checked) throws Exception {
        HtmlTag radioButton = makeRadioButton(group, attribute);
        if (checked)
            radioButton.addAttribute("checked", "checked");
        return radioButton;
    }

    private HtmlTag makeRadioButton(String group, String attribute) {
        HtmlTag checkbox = HtmlUtil.makeInputTag("radio", group);
        checkbox.addAttribute("value", attribute);
        checkbox.tail = " - " + attribute;
        return checkbox;
    }

    private HtmlTag makeSymbolicLinkSection() throws Exception {
        HtmlTag form = HtmlUtil.makeFormTag("get", resource, "symbolics");
        form.add(HtmlUtil.HR);
        form.add(HtmlUtil.makeInputTag("hidden", "responder", "symlink"));
        form.add(new HtmlTag("strong", "Symbolic Links"));

        HtmlTableListingBuilder table = new HtmlTableListingBuilder();
        table.getTable().addAttribute("style", "width:80%");
        table.addRow(new HtmlElement[]{new HtmlTag("strong", "Name"),
                new HtmlTag("strong", "Path to Page"), new HtmlTag("strong", "Actions")
                // , new HtmlTag("strong", "New Name")
        });
        addSymbolicLinkRows(table);
        addFormRow(table);
        form.add(table.getTable());

        return form;
    }

    private void addFormRow(HtmlTableListingBuilder table) throws Exception {
        HtmlTag nameInput = HtmlUtil.makeInputTag("text", "linkName");
        nameInput.addAttribute("size", "16%");
        HtmlTag pathInput = HtmlUtil.makeInputTag("text", "linkPath");
        pathInput.addAttribute("size", "60%");
        HtmlTag submitButton = HtmlUtil.makeInputTag("submit", "submit",
                "Create/Replace");
        submitButton.addAttribute("style", "width:8em");
        table.addRow(new HtmlElement[]{nameInput, pathInput, submitButton});
    }

    private void addSymbolicLinkRows(HtmlTableListingBuilder table)
            throws Exception {
        WikiPageProperty symLinksProperty = pageData.getProperties().getProperty(
                SymbolicPage.PROPERTY_NAME);
        if (symLinksProperty == null)
            return;
        Set<String> symbolicLinkNames = symLinksProperty.keySet();
        for (String linkName : symbolicLinkNames) {
            HtmlElement nameItem = new RawHtml(linkName);
            HtmlElement pathItem = makeHtmlForSymbolicPath(symLinksProperty, linkName);
            // ---Unlink---
            HtmlTag actionItems = HtmlUtil.makeLink(resource
                    + "?responder=symlink&removal=" + linkName, "Unlink&nbsp;");
            // ---Rename---
            String callScript = "javascript:symbolicLinkRename('" + linkName + "','"
                    + resource + "');";
            actionItems.tail = HtmlUtil.makeLink(callScript, "&nbsp;Rename:").html(); // ..."linked list"

            HtmlTag newNameInput = HtmlUtil.makeInputTag("text", linkName);
            newNameInput.addAttribute("size", "16%");
            table.addRow(new HtmlElement[]{nameItem, pathItem, actionItems,
                    newNameInput});
        }
    }

    private HtmlElement makeHtmlForSymbolicPath(
            WikiPageProperty symLinksProperty, String linkName) throws Exception {
        String linkPath = symLinksProperty.get(linkName);
        WikiPagePath wikiPagePath = PathParser.parse(linkPath);

        if (wikiPagePath != null) {
            // TODO -AcD- a better way?
            WikiPage parent = wikiPagePath.isRelativePath() ? page.getParent() : page;
            PageCrawler crawler = parent.getPageCrawler();
            WikiPage target = crawler.getPage(parent, wikiPagePath);
            WikiPagePath fullPath;
            if (target != null) {
                fullPath = crawler.getFullPath(target);
                fullPath.makeAbsolute();
            } else
                fullPath = new WikiPagePath();
            return HtmlUtil.makeLink(fullPath.toString(), Utils.escapeHTML(linkPath));
        } else
            return new RawHtml(linkPath);
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }

    public HtmlTag makeTestActionCheckboxesHtml(PageData pageData)
            throws Exception {
        return makeAttributeCheckboxesHtml("Actions:", ACTION_ATTRIBUTES,
                pageData);
    }

    public HtmlElement makeNavigationCheckboxesHtml(PageData pageData)
            throws Exception {
        return makeAttributeCheckboxesHtml("Navigation:",
                NAVIGATION_ATTRIBUTES, pageData);
    }

    public HtmlTag makeSecurityCheckboxesHtml(PageData pageData) throws Exception {
        return makeAttributeCheckboxesHtml("Security:",
                SECURITY_ATTRIBUTES, pageData);
    }

    public HtmlTag makeTagsHtml(PageData pageData) throws Exception {
        HtmlTag div = new HtmlTag("div");
        div.addAttribute("style", "float: left; padding-right: 5px");

        div.add(makeInputField("Tags:", PropertySUITES, PropertySUITES,
                40, pageData));
        return div;
    }

    public HtmlTag makeHelpTextHtml(PageData pageData) throws Exception {
        return makeInputField("Help Text:", PropertyHELP, "HelpText", 90,
                pageData);
    }

    public HtmlTag makeInputField(String label, String propertyName,
                                  String fieldId, int size, PageData pageData) throws Exception {
        HtmlTag div = new HtmlTag("div");
        div.addAttribute("style", "float: left;");
        div.add(label);

        String textValue = "";
        WikiPageProperty theProp = pageData.getProperties().getProperty(
                propertyName);
        if (theProp != null) {
            String propValue = theProp.getValue();
            if (propValue != null)
                textValue = propValue;
        }

        div.add(HtmlUtil.BR);
        HtmlTag input = HtmlUtil.makeInputTag("text", fieldId, textValue);
        input.addAttribute("size", Integer.toString(size));
        div.add(input);
        return div;
    }

    private HtmlTag makeAttributeCheckboxesHtml(String label,
                                                String[] attributes, PageData pageData) throws Exception {
        HtmlTag div = new HtmlTag("div");
        div.addAttribute("style", "float: left; width: 180px;");

        div.add(label);
        for (String attribute : attributes) {
            div.add(HtmlUtil.BR);
            div.add(makeAttributeCheckbox(attribute, pageData));
        }
        div.add(HtmlUtil.BR);
        div.add(HtmlUtil.BR);
        return div;
    }

}
