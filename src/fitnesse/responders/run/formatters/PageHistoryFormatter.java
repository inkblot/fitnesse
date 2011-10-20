package fitnesse.responders.run.formatters;

import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPage;
import util.TimeMeasurement;

import java.io.IOException;

public class PageHistoryFormatter extends XmlFormatter {
    private WikiPage historyPage;

    public PageHistoryFormatter(final WikiPage page, WriterFactory writerFactory) {
        super(page, writerFactory);
    }

    @Override
    public void newTestStarted(WikiPage testedPage, TimeMeasurement timeMeasurement) throws IOException {
        testResponse = new TestExecutionReport();
        writeHead(testedPage);
        historyPage = testedPage;
        super.newTestStarted(testedPage, timeMeasurement);
    }

    @Override
    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
        super.testComplete(test, testSummary, timeMeasurement);
        writeResults();
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) {
        setTotalRunTimeOnReport(totalTimeMeasurement);
    }

    @Override
    protected WikiPage getPageForHistory() {
        return historyPage;
    }
}
