package fitnesse.responders.run.formatters;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;

public abstract class BaseFormatter implements ResultsListener {

    protected WikiPage page = null;
    public static int finalErrorCount = 0;
    protected int testCount = 0;
    protected int failCount = 0;

    public abstract void writeHead(String pageType) throws IOException;

    protected BaseFormatter() {
    }

    protected BaseFormatter(final WikiPage page) {
        this.page = page;
    }

    protected WikiPage getPage() {
        return page;
    }

    @Override
    public void errorOccurred() {
        try {
            allTestingComplete(new TimeMeasurement().start().stop());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        finalErrorCount = failCount;
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
    }

    @Override
    public void testComplete(WikiPage test, TestSummary summary, TimeMeasurement timeMeasurement) throws IOException {
        testCount++;
        if (summary.wrong > 0) {
            failCount++;
        }
        if (summary.exceptions > 0) {
            failCount++;
        }
    }

    public void addMessageForBlankHtml() throws IOException {
    }

    public int getErrorCount() {
        return 0;
    }

}

class NullFormatter extends BaseFormatter {
    NullFormatter() {
        super(null);
    }

    protected WikiPage getPage() {
        return null;
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
    }

    @Override
    public void errorOccurred() {
    }

    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    }

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws IOException {
    }

    @Override
    public void testOutputChunk(String output) {
    }

    @Override
    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
    }

    @Override
    public void writeHead(String pageType) {
    }
}
