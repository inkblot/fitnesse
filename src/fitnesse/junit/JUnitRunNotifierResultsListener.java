package fitnesse.junit;

import fitnesse.responders.run.CompositeExecutionLog;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesse.responders.run.TestSystem;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;
import util.TimeMeasurement;

public class JUnitRunNotifierResultsListener implements ResultsListener {
    private final Class<?> mainClass;
    private final RunNotifier notifier;

    public JUnitRunNotifierResultsListener(RunNotifier notifier, Class<?> mainClass) {
        this.notifier = notifier;
        this.mainClass = mainClass;
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
    }

    @Override
    public void announceNumberTestsToRun(int testsToRun) {
    }

    @Override
    public void errorOccurred() {
    }

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) {
        notifier.fireTestStarted(descriptionFor(test));
    }

    private Description descriptionFor(WikiPage test) {
        return Description.createTestDescription(mainClass, new WikiPagePath(test).toString());
    }

    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    }

    @Override
    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) {
        if (testSummary.wrong == 0 && testSummary.exceptions == 0) {
            notifier.fireTestFinished(descriptionFor(test));
        } else {
            notifier.fireTestFailure(new Failure(descriptionFor(test), new AssertionError("wrong: "
                    + testSummary.wrong + " exceptions: " + testSummary.exceptions)));
        }
    }

    @Override
    public void testOutputChunk(String output) {
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    }
}
