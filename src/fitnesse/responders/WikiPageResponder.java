// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlUtil;
import fitnesse.html.SetupTeardownAndLibraryIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.editing.EditResponder;
import fitnesse.wiki.*;
import org.apache.velocity.VelocityContext;
import util.Clock;

import java.io.IOException;
import java.util.Properties;

public class WikiPageResponder implements SecureResponder {

    private final Properties properties;
    private final HtmlPageFactory htmlPageFactory;
    private final Clock clock;
    private final WikiPage root;

    @Inject
    public WikiPageResponder(@Named(FitNesseModule.PROPERTIES_FILE) Properties properties, HtmlPageFactory htmlPageFactory, Clock clock, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.properties = properties;
        this.htmlPageFactory = htmlPageFactory;
        this.clock = clock;
        this.root = root;
    }

    public Response makeResponse(Request request) throws Exception {
        WikiPagePath path = PathParser.parse(request.getResource());
        PageCrawler crawler = root.getPageCrawler();
        WikiPage page = crawler.getPage(root, path);

        if (page == null) return notFoundResponse(request, root);

        PageData pageData = page.getData();
        return makePageResponse(pageData);
    }

    private Response notFoundResponse(Request request, WikiPage root) throws Exception {
        if (doNotCreateNonExistentPage(request))
            return new NotFoundResponder(htmlPageFactory).makeResponse(request);
        return EditResponder.makeResponseForNonExistentPage(request, htmlPageFactory, root, getDefaultPageContent(), clock);
    }

    private String getDefaultPageContent() {
        return properties.getProperty(EditResponder.DEFAULT_PAGE_CONTENT_PROPERTY, EditResponder.DEFAULT_PAGE_CONTENT);
    }

    private boolean doNotCreateNonExistentPage(Request request) {
        String doNotCreate = (String) request.getInput("dontCreatePage");
        return doNotCreate != null && (doNotCreate.length() == 0 || Boolean.parseBoolean(doNotCreate));
    }

    private SimpleResponse makePageResponse(PageData pageData) throws IOException {
        String html = makeHtml(pageData);

        SimpleResponse response = new SimpleResponse();
        response.setMaxAge(0);
        response.setContent(html);
        return response;
    }

    public String makeHtml(PageData pageData) throws IOException {
        WikiPage page = pageData.getWikiPage();
        HtmlPage html = htmlPageFactory.newPage();
        WikiPagePath fullPath = page.getPageCrawler().getFullPath(page);
        String fullPathName = PathParser.render(fullPath);
        html.title.use(fullPathName);
        html.header.use(HtmlUtil.makeBreadCrumbsWithCurrentPageNotLinked(fullPathName));
        html.header.add("<a style=\"font-size:small;\" onclick=\"popup('addChildPopup')\"> [add child]</a>");
        html.actions.use(HtmlUtil.makeActions(page.getActions()));
        SetupTeardownAndLibraryIncluder.includeInto(pageData);
        html.main.use(generateHtml(pageData));
        VelocityContext velocityContext = new VelocityContext();

        velocityContext.put("page_name", page.getName());
        velocityContext.put("full_path", fullPathName);
        html.main.add(VelocityFactory.translateTemplate(velocityContext, "fitnesse/templates/addChildPagePopup.vm"));
        return html.html();
    }

    /* hook for subclasses */
    protected String generateHtml(PageData pageData) throws IOException {
        return HtmlUtil.makePageHtmlWithHeaderAndFooter(pageData);
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
