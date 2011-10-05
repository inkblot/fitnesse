// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.run.formatters.*;
import fitnesse.wiki.WikiPage;

public class SuiteResponder extends TestResponder {
    private boolean includeHtml;

    @Inject
    public SuiteResponder(HtmlPageFactory htmlPageFactory) {
        super(htmlPageFactory);
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
    void addXmlFormatter(FitNesseContext context, WikiPage page) {
        CachingSuiteXmlFormatter xmlFormatter = new CachingSuiteXmlFormatter(context, page, makeResponseWriter());
        if (includeHtml)
            xmlFormatter.includeHtml();
        formatters.add(xmlFormatter);
    }

    @Override
    void addHtmlFormatter(FitNesseContext context, WikiPage page) {
        BaseFormatter formatter = new SuiteHtmlFormatter(context, page, getHtmlPageFactory()) {
            @Override
            protected void writeData(String output) {
                addToResponse(output);
            }
        };
        formatters.add(formatter);
    }

    @Override
    protected void addTestHistoryFormatter(FitNesseContext context, WikiPage page) {
        HistoryWriterFactory source = new HistoryWriterFactory();
        formatters.add(new PageHistoryFormatter(context, page, source));
        formatters.add(new SuiteHistoryFormatter(context, page, source));
    }

    @Override
    protected void performExecution(FitNesseContext context, WikiPage root, WikiPage page) throws Exception {
        SuiteFilter filter = new SuiteFilter(getSuiteTagFilter(), getNotSuiteFilter(), getSuiteFirstTest(page));
        SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
        MultipleTestsRunner runner = new MultipleTestsRunner(suiteTestFinder.getAllPagesToRunForThisSuite(), context, page, formatters);
        runner.setDebug(isRemoteDebug());
        runner.setFastTest(isFastTest());
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
