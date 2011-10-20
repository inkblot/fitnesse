// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.BasicResponder;

public class StopTestResponder extends BasicResponder {

    String testId = null;
    private final HtmlPageFactory htmlPageFactory;
    private final RunningTestingTracker runningTestingTracker;

    @Inject
    public StopTestResponder(HtmlPageFactory htmlPageFactory, RunningTestingTracker runningTestingTracker) {
        super(htmlPageFactory);
        this.htmlPageFactory = htmlPageFactory;
        this.runningTestingTracker = runningTestingTracker;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();

        if (request.hasInput("id")) {
            testId = request.getInput("id").toString();
        }

        response.setContent(html(runningTestingTracker));

        return response;
    }

    private String html(RunningTestingTracker runningTestingTracker) throws Exception {
        HtmlPage page = htmlPageFactory.newPage();
        HtmlUtil.addTitles(page, "Stopping tests");
        page.main.use(getDetails(runningTestingTracker));
        return page.html();
    }

    public String getDetails(RunningTestingTracker runningTestingTracker) {
        if (testId != null) {
            return "Attempting to stop single test or suite..." + HtmlUtil.BRtag + runningTestingTracker.stopProcess(testId);
        } else {
            return "Attempting to stop all running test processes..." + HtmlUtil.BRtag + runningTestingTracker.stopAllProcesses();
        }
    }
}
