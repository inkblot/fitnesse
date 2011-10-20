package fitnesse.responders.run.formatters;

import fitnesse.VelocityFactory;
import fitnesse.responders.run.SuiteExecutionReport;
import fitnesse.responders.run.TestExecutionReport;
import fitnesse.responders.testHistory.PageHistory;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.VelocityContext;
import util.TimeMeasurement;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class CachingSuiteXmlFormatter extends SuiteExecutionReportFormatter {
    private TestHistory testHistory = new TestHistory();
    private VelocityContext velocityContext;
    private Writer writer;
    private boolean includeHtml = false;
    private final File testHistoryDirectory;

    public CachingSuiteXmlFormatter(WikiPage page, Writer writer, File testHistoryDirectory) {
        super(page);
        velocityContext = new VelocityContext();
        this.writer = writer;
        this.testHistoryDirectory = testHistoryDirectory;
    }

    void setTestHistoryForTests(TestHistory testHistory) {
        this.testHistory = testHistory;
    }

    void setVelocityForTests(VelocityContext velocityContext, Writer writer) {
        this.velocityContext = velocityContext;
        this.writer = writer;
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        super.allTestingComplete(totalTimeMeasurement);
        writeOutSuiteXML();
    }

    protected void writeOutSuiteXML() throws IOException {
        testHistory.readHistoryDirectory(testHistoryDirectory);
        velocityContext.put("formatter", this);
        VelocityFactory.mergeTemplate(writer, velocityContext, "fitnesse/templates/suiteXML.vm");
        closeQuietly(writer);
    }

    public TestExecutionReport getTestExecutionReport(SuiteExecutionReport.PageHistoryReference reference) throws IOException {
        PageHistory pageHistory = testHistory.getPageHistory(reference.getPageName());
        Date date;
        date = new Date(reference.getTime());
        PageHistory.TestResultRecord record = pageHistory.get(date);
        return makeTestExecutionReport().read(record.getFile());
    }

    TestExecutionReport makeTestExecutionReport() {
        return new TestExecutionReport();
    }

    public void includeHtml() {
        includeHtml = true;
    }

    public boolean shouldIncludeHtml() {
        return includeHtml;
    }

    public long getTotalRunTimeInMillis() {
        // for velocity macro only -- would be nicer to rewrite the macro
        // so that it reads from the report directly as per SuiteHistoryFormatter
        return suiteExecutionReport.getTotalRunTimeInMillis();
    }
}
