// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.FitNesse;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

public class ShutdownResponder implements SecureResponder {

    private final HtmlPageFactory htmlPageFactory;
    private final FitNesse fitNesse;

    @Inject
    public ShutdownResponder(HtmlPageFactory htmlPageFactory, FitNesse fitNesse) {
        this.htmlPageFactory = htmlPageFactory;
        this.fitNesse = fitNesse;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();

        HtmlPage html = htmlPageFactory.newPage();
        html.title.use("Shutdown");
        html.header.use(HtmlUtil.makeSpanTag("page_title", "Shutdown"));

        HtmlTag content = HtmlUtil.makeDivTag("centered");
        content.add(new HtmlTag("h3", "FitNesse is shutting down..."));

        html.main.use(content);
        response.setContent(html.html());


        Thread shutdownThread = new Thread() {
            public void run() {
                try {
                    fitNesse.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        shutdownThread.start();

        return response;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
