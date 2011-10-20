// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.Responder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.util.regex.Pattern;

// TODO: Some of this code may now be obsolete, because this responder is no longer used for some
// scenarios (we skip directly to an EditResponder...).
public class NotFoundResponder implements Responder {
    private String resource;
    private final HtmlPageFactory htmlPageFactory;

    @Inject
    public NotFoundResponder(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse(404);
        resource = request.getResource();

        response.setContent(makeHtml());
        return response;
    }

    private String makeHtml() throws Exception {
        HtmlPage page = htmlPageFactory.newPage();
        HtmlUtil.addTitles(page, "Not Found:" + resource);
        page.main.use(makeRightColumn(resource));
        return page.html();
    }

    private String makeRightColumn(String name) throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("The requested resource: <i>").append(name).append("</i> was not found.");
        if (Pattern.matches(WikiWordWidget.REGEXP, name)) {
            makeCreateThisPageWithButton(name, buffer);
        }
        return buffer.toString();
    }

    private void makeCreateThisPageWithButton(String name, StringBuffer buffer)
            throws Exception {
        HtmlTag createPageForm = HtmlUtil.makeFormTag("POST", name + "?edit", "createPageForm");
        HtmlTag submitButton = HtmlUtil.makeInputTag("submit", "createPageSubmit", "Create This Page");
        submitButton.addAttribute("accesskey", "c");
        createPageForm.add(submitButton);
        buffer.append(HtmlUtil.BR);
        buffer.append(HtmlUtil.BR);
        buffer.append(createPageForm.html());
    }

}
