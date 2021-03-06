package fitnesse.responders.run.formatters;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CompositeFormatter extends BaseFormatter {
    List<BaseFormatter> formatters = new ArrayList<BaseFormatter>();

    public void add(BaseFormatter formatter) {
        formatters.add(formatter);
    }

    @Override
    protected WikiPage getPage() {
        throw new RuntimeException("Should not get here.");
    }

    @Override
    public void errorOccurred() {
        for (BaseFormatter formatter : formatters)
            formatter.errorOccurred();
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
        for (BaseFormatter formatter : formatters)
            formatter.announceNumberTestsToRun(testsToRun);
    }

    @Override
    public void addMessageForBlankHtml() throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.addMessageForBlankHtml();
    }

    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.setExecutionLogAndTrackingId(stopResponderId, log);
    }

    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.testSystemStarted(testSystem, testSystemName, testRunner);
    }

    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.newTestStarted(test, timeMeasurement);
    }

    public void testOutputChunk(String output) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.testOutputChunk(output);
    }

    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.testComplete(test, testSummary, timeMeasurement);
    }

    public void writeHead(String pageType) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.writeHead(pageType);
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        for (BaseFormatter formatter : formatters)
            formatter.allTestingComplete(totalTimeMeasurement);
    }

    public int getErrorCount() {
        int exitCode = 0;
        for (BaseFormatter formatter : formatters)
            exitCode = Math.max(exitCode, formatter.getErrorCount());
        return exitCode;
    }

}
