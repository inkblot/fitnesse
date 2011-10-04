package fitnesse.junit;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import util.TimeMeasurement;

public class PrintTestListener implements ResultsListener {
    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
        System.out.println("--complete: " + totalTimeMeasurement.elapsedSeconds() + " seconds--");
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
    }

    @Override
    public void errorOccurred() {
    }

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) {
    }

    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    }

    @Override
    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
        System.out.println(new WikiPagePath(test).toString() + " r " + testSummary.right + " w "
                + testSummary.wrong + " " + testSummary.exceptions
                + " " + timeMeasurement.elapsedSeconds() + " seconds");
    }

    @Override
    public void testOutputChunk(String output) {
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner)
            throws Exception {
    }
}
