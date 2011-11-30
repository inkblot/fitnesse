// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.run.formatters.*;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

import java.io.File;

public class SuiteResponder extends TestResponder {
    private boolean includeHtml;

    @Inject
    public SuiteResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root, @Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath, @Named(FitNesseModule.PORT) Integer port, SocketDealer socketDealer, RunningTestingTracker runningTestingTracker, @Named(FitNesseModule.ENABLE_CHUNKING) boolean chunkingEnabled, Injector injector) {
        super(htmlPageFactory, root, testResultsPath, port, socketDealer, runningTestingTracker, chunkingEnabled, injector);
    }

    @Override
    String getTitle() {
        return "Suite Results";
    }

    @Override
    protected void checkArguments() {
        super.checkArguments();
        includeHtml |= request.hasInput("includehtml");
    }

    @Override
    void addXmlFormatter(File testHistoryDirectory, WikiPage page) {
        CachingSuiteXmlFormatter xmlFormatter = new CachingSuiteXmlFormatter(page, makeResponseWriter(), testHistoryDirectory);
        if (includeHtml)
            xmlFormatter.includeHtml();
        formatters.add(xmlFormatter);
    }

    @Override
    void addHtmlFormatter(WikiPage page) {
        BaseFormatter formatter = new SuiteHtmlFormatter(page, getHtmlPageFactory()) {
            @Override
            protected void writeData(String output) {
                addToResponse(output);
            }
        };
        formatters.add(formatter);
    }

    @Override
    protected void addTestHistoryFormatter(File testHistoryDirectory, WikiPage page) {
        HistoryWriterFactory source = new HistoryWriterFactory(testHistoryDirectory);
        formatters.add(new PageHistoryFormatter(page, source));
        formatters.add(new SuiteHistoryFormatter(page, source));
    }

    @Override
    protected void performExecution(RunningTestingTracker runningTestingTracker, WikiPage root, WikiPage page) throws Exception {
        SuiteFilter filter = new SuiteFilter(getSuiteTagFilter(), getNotSuiteFilter(), getSuiteFirstTest(page));
        SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
        MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), runningTestingTracker, page, formatters, root, port, socketDealer, injector);
        runner.setDebug(isRemoteDebug());
        runner.executeTestPages();
    }

    private String getSuiteTagFilter() {
        return request != null ? (String) request.getInput("suiteFilter") : null;
    }

    private String getNotSuiteFilter() {
        return request != null ? (String) request.getInput("excludeSuiteFilter") : null;
    }


    private String getSuiteFirstTest(WikiPage page) throws Exception {
        String startTest = null;
        if (request != null) {
            startTest = (String) request.getInput("firstTest");
        }

        if (startTest != null) {
            String suiteName = page.getPageCrawler().getFullPath(page).toString();
            if (startTest.indexOf(suiteName) != 0) {
                startTest = suiteName + "." + startTest;
            }
        }

        return startTest;
    }
}
