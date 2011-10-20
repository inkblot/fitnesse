// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;

public class VersionResponder implements SecureResponder {
    private String version;
    private String resource;
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public VersionResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        resource = request.getResource();
        version = (String) request.getInput("version");
        if (version == null)
            return new ErrorResponder("No version specified.", htmlPageFactory).makeResponse(context, request);

        PageCrawler pageCrawler = root.getPageCrawler();
        WikiPagePath path = PathParser.parse(resource);
        WikiPage page = pageCrawler.getPage(root, path);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);
        PageData pageData = page.getDataVersion(version);

        String fullPathName = PathParser.render(pageCrawler.getFullPath(page));
        HtmlPage html = makeHtml(fullPathName, pageData);

        SimpleResponse response = new SimpleResponse();
        response.setContent(html.html());

        return response;
    }

    private HtmlPage makeHtml(String name, PageData pageData) throws Exception {
        HtmlPage html = htmlPageFactory.newPage();
        html.title.use("Version " + version + ": " + name);
        html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Version " + version));
        html.actions.use(makeRollbackLink(name));
        html.main.use(HtmlUtil.makeNormalWikiPageContent(pageData));
        return html;
    }

    private HtmlTag makeRollbackLink(String name) {
        WikiPageAction action = new WikiPageAction(name, "Rollback");
        action.setQuery("responder=rollback&version=" + version);
        action.setShortcutKey("");
        return HtmlUtil.makeAction(action);
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
