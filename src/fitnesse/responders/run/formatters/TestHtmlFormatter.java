// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run.formatters;

import fitnesse.html.*;
import fitnesse.wikitext.WikiImportProperty;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.*;
import util.TimeMeasurement;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isEmpty;

public abstract class TestHtmlFormatter extends BaseFormatter {
    private HtmlPageFactory pageFactory;
    private TestSummary assertionCounts = new TestSummary();
    private CompositeExecutionLog log = null;
    private HtmlPage htmlPage = null;
    private boolean wasInterrupted = false;
    protected TimeMeasurement latestTestTime;

    private static final String TESTING_INTERRUPTED = "<strong>Testing was interupted and results are incomplete.</strong><br/>";

    public TestHtmlFormatter(final WikiPage page,
                             final HtmlPageFactory pageFactory) {
        super(page);
        this.pageFactory = pageFactory;
    }

    //special constructor for TestRunner.  Used only for formatting.
    //todo this is nasty coupling.
    public TestHtmlFormatter() {
        super(null);
    }

    protected abstract void writeData(String output) throws IOException;

    @Override
    public void writeHead(String pageType) throws IOException {
        htmlPage = buildHtml(pageType);
        htmlPage.main.use(HtmlPage.BreakPoint);
        htmlPage.divide();
        writeData(htmlPage.preDivision + makeSummaryPlaceHolder().html());
    }

    private HtmlTag makeSummaryPlaceHolder() {
        HtmlTag testSummaryDiv = new HtmlTag("div", "Running Tests ...");
        testSummaryDiv.addAttribute("id", "test-summary");

        return testSummaryDiv;
    }

    protected void updateSummaryDiv(String html) throws IOException {
        writeData(HtmlUtil.makeReplaceElementScript("test-summary", html).html());
    }

    protected String testPageSummary() {
        return "";
    }

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws IOException {
        writeData(getPage().getData().getHeaderPageHtml());
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws IOException {
    }

    @Override
    public void testOutputChunk(String output) throws IOException {
        writeData(output);
    }

    @Override
    public void testComplete(WikiPage testPage, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
        super.testComplete(testPage, testSummary, timeMeasurement);
        latestTestTime = timeMeasurement;

        processTestResults(getRelativeName(testPage), testSummary);
    }

    protected String getRelativeName(WikiPage testPage) {
        PageCrawler pageCrawler = getPage().getPageCrawler();
        String relativeName = pageCrawler.getRelativeName(getPage(), testPage);
        if (isEmpty(relativeName)) {
            relativeName = String.format("(%s)", testPage.getName());
        }
        return relativeName;
    }

    public void processTestResults(String relativeName, TestSummary testSummary) throws IOException {
        getAssertionCounts().add(testSummary);
    }

    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws IOException {
        this.log = log;
        addStopLink(stopResponderId);
    }

    private void addStopLink(String stopResponderId) throws IOException {
        String link = "?responder=stoptest&id=" + stopResponderId;

        HtmlTag status = new HtmlTag("div");
        status.addAttribute("id", "stop-test");
        HtmlTag image = new HtmlTag("img");
        image.addAttribute("src", "/files/images/stop.gif");

        status.add(HtmlUtil.makeSilentLink(link, image));
        status.add(HtmlUtil.BR);

        status.add(HtmlUtil.makeSilentLink(link, new RawHtml("Stop Test")));
        writeData(status.html());
    }

    private void removeStopTestLink() throws IOException {
        HtmlTag script = HtmlUtil.makeReplaceElementScript("stop-test", "");
        writeData(script.html());
    }

    protected HtmlPage buildHtml(String pageType) throws IOException {
        PageCrawler pageCrawler = getPage().getPageCrawler();
        WikiPagePath fullPath = pageCrawler.getFullPath(getPage());
        String fullPathName = PathParser.render(fullPath);
        HtmlPage html = pageFactory.newPage();
        html.title.use(pageType + ": " + fullPathName);
        html.header.use(HtmlUtil
                .makeBreadCrumbsWithPageType(fullPathName, pageType));
        html.header.add(String.format("&nbsp;<a style=\"font-size:small;\" href=\"%s?pageHistory\"> [history]</a>", fullPathName));
        PageData data = getPage().getData();
        html.actions.use(HtmlUtil.makeActions(getPage().getActions()));
        WikiImportProperty.handleImportProperties(html, getPage(), data);
        return html;
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        super.allTestingComplete(totalTimeMeasurement);
        removeStopTestLink();
        publishAndAddLog();
        finishWritingOutput();
        close();
    }

    protected void close() {
    }

    protected void finishWritingOutput() throws IOException {
        writeData(testSummary());
        writeData("<br/><div class=\"footer\">\n");
        writeData(getPage().getData().getFooterPageHtml());
        writeData("</div>\n");
        if (htmlPage != null)
            writeData(htmlPage.postDivision);
    }

    protected void publishAndAddLog() throws IOException {
        if (log != null) {
            log.publish();
            writeData(executionStatus(log));
        }
    }

    protected String cssClassFor(TestSummary testSummary) {
        if (testSummary.getWrong() > 0 || wasInterrupted)
            return "fail";
        else if (testSummary.getExceptions() > 0
                || testSummary.getRight() + testSummary.getIgnores() == 0)
            return "error";
        else if (testSummary.getIgnores() > 0 && testSummary.getRight() == 0)
            return "ignore";
        else
            return "pass";
    }

    public String executionStatus(CompositeExecutionLog logs) {
        return logs.executionStatusHtml();
    }

    protected String makeSummaryContent() {
        String summaryContent;
        if (latestTestTime != null) {
            summaryContent = String.format("%s<strong>Assertions:</strong> %s (%.03f seconds)", testPageSummary(), assertionCounts, latestTestTime.elapsedSeconds());
        } else {
            summaryContent = String.format("%s<strong>Assertions:</strong> %s ", testPageSummary(), assertionCounts);
        }
        return summaryContent;
    }

    public String testSummary() {
        String summaryContent = (wasInterrupted) ? TESTING_INTERRUPTED : "";
        summaryContent += makeSummaryContent();
        HtmlTag script = HtmlUtil.makeReplaceElementScript("test-summary", summaryContent);
        script.add("document.getElementById(\"test-summary\").className = \""
                + cssClassFor(assertionCounts) + "\";");
        return script.html();
    }

    @Override
    public int getErrorCount() {
        return getAssertionCounts().getWrong() + getAssertionCounts().getExceptions();
    }

    @Override
    public void addMessageForBlankHtml() throws IOException {
        TagGroup html = new TagGroup();
        HtmlTag h2 = new HtmlTag("h2");
        h2.addAttribute("class", "centered");
        h2.add("Oops!  Did you forget to add to some content to this ?");
        html.add(h2.html());
        html.add(HtmlUtil.HR.html());
        writeData(html.html());
    }

    public TestSummary getAssertionCounts() {
        return assertionCounts;
    }

    public HtmlPage getHtmlPage() throws IOException {
        if (htmlPage != null) {
            return htmlPage;
        }
        return buildHtml("");
    }

    @Override
    public void errorOccurred() {
        wasInterrupted = true;
        latestTestTime = null;
        super.errorOccurred();
    }
}
