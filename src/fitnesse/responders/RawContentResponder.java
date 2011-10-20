// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

public class RawContentResponder implements SecureResponder {
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public RawContentResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        String resource = request.getResource();
        WikiPagePath path = PathParser.parse(resource);
        WikiPage page = root.getPageCrawler().getPage(root, path);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);
        PageData pageData = page.getData();

        SimpleResponse response = new SimpleResponse();
        response.setMaxAge(0);
        response.setContent(pageData.getContent());

        return response;
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
