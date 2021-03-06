package fitnesse.responders.run.formatters;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.responders.run.SuiteExecutionReport.PageHistoryReference;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.Test;
import util.TimeMeasurement;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SuiteExecutionReportFormatterTest extends FitnesseBaseTestCase {

    @Test
    public void testCompleteShouldSetRunTimeForCurrentReference() throws Exception {
        WikiPage page = new WikiPageDummy("name", "content", injector);
        SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(page);

        TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
        when(timeMeasurement.startedAt()).thenReturn(65L);
        when(timeMeasurement.elapsed()).thenReturn(2L);
        formatter.newTestStarted(page, timeMeasurement);

        when(timeMeasurement.elapsed()).thenReturn(99L);
        TestSummary testSummary = new TestSummary(4, 2, 7, 3);
        formatter.testComplete(page, testSummary, timeMeasurement);

        assertThat(formatter.suiteExecutionReport.getPageHistoryReferences().size(), is(1));
        PageHistoryReference reference = formatter.suiteExecutionReport.getPageHistoryReferences().get(0);
        assertThat(reference.getTestSummary(), equalTo(testSummary));
        assertThat(reference.getRunTimeInMillis(), is(99L));
    }

    @Test
    public void allTestingCompleteShouldSetTotalRunTimeOnReport() throws Exception {
        WikiPage page = new WikiPageDummy("name", "content", injector);
        SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(page);

        TimeMeasurement totalTimeMeasurement = new TimeMeasurement().start();
        formatter.announceNumberTestsToRun(0);
        while (totalTimeMeasurement.elapsed() == 0) {
            Thread.sleep(50);
        }

        formatter.allTestingComplete(totalTimeMeasurement.stop());
        assertThat(formatter.suiteExecutionReport.getTotalRunTimeInMillis(),
                is(totalTimeMeasurement.elapsed()));
    }

    @Test
    public void testCompleteShouldSetFailedCount() throws Exception {
        WikiPage page = new WikiPageDummy("name", "content", injector);
        SuiteExecutionReportFormatter formatter = new SuiteExecutionReportFormatter(page);

        TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
        when(timeMeasurement.startedAt()).thenReturn(65L);
        when(timeMeasurement.elapsed()).thenReturn(2L);
        formatter.newTestStarted(page, timeMeasurement);

        when(timeMeasurement.elapsed()).thenReturn(99L);
        TestSummary testSummary = new TestSummary(4, 2, 7, 3);
        formatter.testComplete(page, testSummary, timeMeasurement);

        assertThat(formatter.failCount, is(5));

        formatter.allTestingComplete(timeMeasurement);

        assertThat(BaseFormatter.finalErrorCount, is(5));

    }

}
