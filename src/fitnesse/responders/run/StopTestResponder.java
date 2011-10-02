// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.BasicResponder;

public class StopTestResponder extends BasicResponder {

    String testId = null;

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();

        if (request.hasInput("id")) {
            testId = request.getInput("id").toString();
        }

        response.setContent(html(context));

        return response;
    }

    private String html(FitNesseContext context) throws Exception {
        HtmlPage page = context.getHtmlPageFactory().newPage();
        HtmlUtil.addTitles(page, "Stopping tests");
        page.main.use(getDetails(context));
        return page.html();
    }

    public String getDetails(FitNesseContext context) {
        if (testId != null) {
            return "Attempting to stop single test or suite..." + HtmlUtil.BRtag + context.runningTestingTracker.stopProcess(testId);
        } else {
            return "Attempting to stop all running test processes..." + HtmlUtil.BRtag + context.runningTestingTracker.stopAllProcesses();
        }
    }
}
