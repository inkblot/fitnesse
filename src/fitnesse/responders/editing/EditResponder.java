// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContextModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SaveRecorder;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import fitnesse.wikitext.Utils;
import util.Clock;

import java.io.IOException;
import java.util.Properties;

public class EditResponder implements SecureResponder {
    public static final String CONTENT_INPUT_NAME = "pageContent";
    public static final String CONTENT_INPUT_ID = "pageContentId";
    public static final String TIME_STAMP = "editTime";
    public static final String TICKET_ID = "ticketId";

    public static final String DEFAULT_PAGE_CONTENT_PROPERTY = "newpage.default.content";
    public static final String DEFAULT_PAGE_CONTENT = "!contents -R2 -g -p -f -h";

    private final Properties properties;
    private final HtmlPageFactory htmlPageFactory;
    private final Clock clock;

    @Inject
    public EditResponder(@Named(FitNesseContextModule.PROPERTIES_FILE) Properties properties, HtmlPageFactory htmlPageFactory, Clock clock) {
        this.properties = properties;
        this.htmlPageFactory = htmlPageFactory;
        this.clock = clock;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws IOException {
        boolean nonExistent = request.hasInput("nonExistent");
        return doMakeResponse(request, nonExistent, context.root, htmlPageFactory, getDefaultPageContent(), clock);
    }

    private String getDefaultPageContent() {
        return properties.getProperty(DEFAULT_PAGE_CONTENT_PROPERTY, DEFAULT_PAGE_CONTENT);
    }

    public static Response makeResponseForNonExistentPage(Request request,
                                                          HtmlPageFactory htmlPageFactory, WikiPage root, String defaultContent, Clock clock) throws IOException {
        return doMakeResponse(request, true, root, htmlPageFactory, defaultContent, clock);
    }

    private static Response doMakeResponse(Request request, boolean firstTimeForNewPage, WikiPage root,
                                           HtmlPageFactory htmlPageFactory, String defaultContent, Clock clock) throws IOException {
        SimpleResponse response = new SimpleResponse();
        String resource = request.getResource();
        WikiPagePath path = PathParser.parse(resource);
        PageCrawler crawler = root.getPageCrawler();
        WikiPage page;
        if (!crawler.pageExists(root, path)) {
            crawler.setDeadEndStrategy(new MockingPageCrawler());
            page = crawler.getPage(root, path);
        } else
            page = crawler.getPage(root, path);

        PageData pageData = page.getData();
        String content = pageData.getContent();

        if (firstTimeForNewPage) {
            response.setContent(doMakeHtml(resource, request, defaultContent, "Page doesn't exist. Edit ", htmlPageFactory, clock));
        } else {
            response.setContent(doMakeHtml(resource, request, content, "Edit ", htmlPageFactory, clock));
        }

        response.setMaxAge(0);

        return response;
    }


    private static String doMakeHtml(String resource, Request request, String content, String title, HtmlPageFactory htmlPageFactory, Clock clock)
            throws IOException {
        HtmlPage html = htmlPageFactory.newPage();
        html.title.use(title + resource + ":");

        html.body.addAttribute("onLoad", "document.f." + CONTENT_INPUT_NAME + ".focus()");
        HtmlTag header = makeHeader(resource, title);
        html.header.use(header);
        html.main.use(makeEditForm(resource, request, content, clock));

        return html.html();
    }

    private static HtmlTag makeHeader(String resource, String title) throws IOException {
        return HtmlUtil.makeBreadCrumbsWithPageType(resource, title + "Page:");
    }

    private static HtmlTag makeEditForm(String resource, Request request, String content, Clock clock) throws IOException {
        HtmlTag form = new HtmlTag("form");
        form.addAttribute("name", "f");
        form.addAttribute("action", resource);
        form.addAttribute("method", "post");
        form.add(HtmlUtil.makeInputTag("hidden", "responder", "saveData"));
        form.add(HtmlUtil.makeInputTag("hidden", TIME_STAMP, String.valueOf(clock.currentClockTimeInMillis())));
        form.add(HtmlUtil.makeInputTag("hidden", TICKET_ID, String.valueOf((SaveRecorder.newTicket()))));
        if (request.hasInput("redirectToReferer") && request.hasHeader("Referer")) {
            String redirectUrl = request.getHeader("Referer").toString();
            int questionMarkIndex = redirectUrl.indexOf("?");
            if (questionMarkIndex > 0)
                redirectUrl = redirectUrl.substring(0, questionMarkIndex);
            redirectUrl += "?" + request.getInput("redirectAction").toString();
            form.add(HtmlUtil.makeInputTag("hidden", "redirect", redirectUrl));
        }

        form.add(createTextarea(content));
        form.add(createButtons());
        form.add(createOptions());
        form.add("<div class=\"hints\"><br />Hints:\n<ul>" +
                "<li>Use alt+s (Windows) or control+s (Mac OS X) to save your changes. Or, tab from the text area to the \"Save\" button!</li>\n" +
                "<li>Grab the lower-right corner of the text area to increase its size (works with some browsers).</li>\n" +
                "</ul></div>");

        TagGroup group = new TagGroup();
        group.add(form);

        return group;
    }

    private static HtmlTag createOptions() {
        HtmlTag options = HtmlUtil.makeDivTag("edit_options");
        options.add(makeScriptOptions());
        return options;
    }

    private static HtmlTag makeScriptOptions() {
        TagGroup scripts = new TagGroup();

        includeJavaScriptFile("/files/javascript/textareaWrapSupport.js", scripts);

        return scripts;
    }

    private static HtmlTag createButtons() {
        HtmlTag buttons = HtmlUtil.makeDivTag("edit_buttons");
        buttons.add(makeSaveButton());
        buttons.add(makeScriptButtons());
        return buttons;
    }

    private static HtmlTag makeScriptButtons() {
        TagGroup scripts = new TagGroup();

        includeJavaScriptFile("/files/javascript/SpreadsheetTranslator.js", scripts);
        includeJavaScriptFile("/files/javascript/spreadsheetSupport.js", scripts);
        includeJavaScriptFile("/files/javascript/WikiFormatter.js", scripts);
        includeJavaScriptFile("/files/javascript/wikiFormatterSupport.js", scripts);
        includeJavaScriptFile("/files/javascript/fitnesse.js", scripts);

        return scripts;
    }

    private static void includeJavaScriptFile(String jsFile, TagGroup scripts) {
        HtmlTag scriptTag = HtmlUtil.makeJavascriptLink(jsFile);
        scripts.add(scriptTag);
    }

    private static HtmlTag makeSaveButton() {
        HtmlTag saveButton = HtmlUtil.makeInputTag("submit", "save", "Save");
        saveButton.addAttribute("tabindex", "2");
        saveButton.addAttribute("accesskey", "s");
        return saveButton;
    }

    private static HtmlTag createTextarea(String content) {
        HtmlTag textarea = new HtmlTag("textarea");
        textarea.addAttribute("class", CONTENT_INPUT_NAME + " no_wrap");
        textarea.addAttribute("wrap", "off");
        textarea.addAttribute("name", CONTENT_INPUT_NAME);
        textarea.addAttribute("id", CONTENT_INPUT_ID);
        textarea.addAttribute("rows", "30");
        textarea.addAttribute("cols", "70");
        textarea.addAttribute("tabindex", "1");
        textarea.add(Utils.escapeHTML(content));
        return textarea;
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
