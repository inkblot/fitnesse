// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import util.StringUtil;

public class ErrorResponder implements Responder {
    Exception exception;
    private String message;
    private final HtmlPageFactory htmlPageFactory;

    public ErrorResponder(Exception e, HtmlPageFactory htmlPageFactory) {
        exception = e;
        this.htmlPageFactory = htmlPageFactory;
    }

    public ErrorResponder(String message, HtmlPageFactory htmlPageFactory) {
        this.message = message;
        this.htmlPageFactory = htmlPageFactory;
    }

    public Response makeResponse(Request request) {
        SimpleResponse response = new SimpleResponse(400);
        HtmlPage html = htmlPageFactory.newPage();
        HtmlUtil.addTitles(html, "Error Occurred");
        if (exception != null)
            html.main.add("<pre>" + StringUtil.makeExceptionString(exception) + "</pre>");
        if (message != null)
            html.main.add(makeErrorMessage());
        response.setContent(html.html());

        return response;
    }

    public HtmlTag makeErrorMessage() {
        HtmlTag tag = HtmlUtil.makeDivTag("centered");
        tag.add(message);
        return tag;
    }
}
