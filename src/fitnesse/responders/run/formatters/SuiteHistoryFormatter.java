package fitnesse.responders.run.formatters;

import fitnesse.VelocityFactory;
import fitnesse.wiki.WikiPage;
import org.apache.velocity.VelocityContext;
import util.TimeMeasurement;

import java.io.IOException;
import java.io.Writer;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class SuiteHistoryFormatter extends SuiteExecutionReportFormatter {
    private Writer writer;
    private XmlFormatter.WriterFactory writerFactory;
    private long suiteTime = 0;

    public SuiteHistoryFormatter(final WikiPage page, Writer writer) {
        super(page);
        this.writer = writer;
    }

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) {
        if (suiteTime == 0)
            suiteTime = timeMeasurement.startedAt();
        super.newTestStarted(test, timeMeasurement);
    }

    public SuiteHistoryFormatter(WikiPage page, XmlFormatter.WriterFactory source) {
        super(page);
        writerFactory = source;
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        super.allTestingComplete(totalTimeMeasurement);
        if (writerFactory != null)
            writer = writerFactory.getWriter(page, getPageCounts(), suiteTime);
        VelocityContext velocityContext = new VelocityContext();
        velocityContext.put("suiteExecutionReport", suiteExecutionReport);
        VelocityFactory.mergeTemplate(writer, velocityContext, "fitnesse/templates/suiteHistoryXML.vm");
        closeQuietly(writer);
    }
}
