// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.authentication.InsecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public abstract class BasicResponder implements SecureResponder {
    private final HtmlPageFactory htmlPageFactory;

    public BasicResponder(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    protected Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception {
        return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);
    }

    protected Response responseWith(String content) throws Exception {
        SimpleResponse response = new SimpleResponse();
        response.setContentType(getContentType());
        response.setContent(content);
        return response;
    }

    protected String getContentType() {
        return Response.DEFAULT_CONTENT_TYPE;
    }

    public SecureOperation getSecureOperation() {
        return new InsecureOperation();
    }
}
