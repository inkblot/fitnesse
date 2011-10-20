// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.versions;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.components.RecentChanges;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RollbackResponder implements SecureResponder {
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public RollbackResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();

        String resource = request.getResource();
        String version = (String) request.getInput("version");
        if (version == null)
            return new ErrorResponder("missing version", htmlPageFactory).makeResponse(request);

        WikiPagePath path = PathParser.parse(resource);
        WikiPage page = root.getPageCrawler().getPage(root, path);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(request);
        PageData data = page.getDataVersion(version);

        page.commit(data);

        RecentChanges.updateRecentChanges(data);
        response.redirect(resource);

        return response;
    }

    public SecureOperation getSecureOperation() {
        return new SecureWriteOperation();
    }
}
