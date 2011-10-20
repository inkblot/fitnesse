package fitnesse.responders.run.formatters;

import fitnesse.FitNesseVersion;
import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;
import java.util.List;

public class SuiteExecutionReportFormatter extends BaseFormatter {
    private SuiteExecutionReport.PageHistoryReference referenceToCurrentTest;
    protected SuiteExecutionReport suiteExecutionReport;

    public SuiteExecutionReportFormatter(final WikiPage page) {
        super(page);
        suiteExecutionReport = new SuiteExecutionReport();
        suiteExecutionReport.version = new FitNesseVersion().toString();
        suiteExecutionReport.rootPath = this.page.getName();
    }

    @Override
    public void writeHead(String pageType) {
    }

    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    }

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) {
        String pageName = PathParser.render(test.getPageCrawler().getFullPath(test));
        referenceToCurrentTest = new SuiteExecutionReport.PageHistoryReference(pageName, timeMeasurement.startedAt(), timeMeasurement.elapsed());
    }

    @Override
    public void testOutputChunk(String output) {
    }

    public String getRootPageName() {
        return suiteExecutionReport.getRootPath();
    }

    public String getFitNesseVersion() {
        return new FitNesseVersion().toString();
    }

    @Override
    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
        referenceToCurrentTest.setTestSummary(testSummary);
        referenceToCurrentTest.setRunTimeInMillis(timeMeasurement.elapsed());
        suiteExecutionReport.addPageHistoryReference(referenceToCurrentTest);
        suiteExecutionReport.tallyPageCounts(testSummary);
        failCount += testSummary.wrong;
        failCount += testSummary.exceptions;
    }

    public List<SuiteExecutionReport.PageHistoryReference> getPageHistoryReferences() {
        return suiteExecutionReport.getPageHistoryReferences();
    }

    @Override
    public int getErrorCount() {
        return getPageCounts().wrong + getPageCounts().exceptions;
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        super.allTestingComplete(totalTimeMeasurement);
        suiteExecutionReport.setTotalRunTimeInMillis(totalTimeMeasurement);
    }

    public TestSummary getPageCounts() {
        return suiteExecutionReport.getFinalCounts();
    }

}
