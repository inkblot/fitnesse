package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.WikiPageDummy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.TimeMeasurement;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestFormatterTest extends FitnesseBaseTestCase {

    private WikiPageDummy page;
    private TestSummary right;
    private TestSummary wrong;
    private TestSummary exception;
    private XmlFormatter.WriterFactory writerFactory;
    private ChunkedResponse response;
    private WikiPageDummy testPage;
    private FitNesseContext context;

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        response = mock(ChunkedResponse.class);
        testPage = new WikiPageDummy("testPage", "testContent", injector);
        writerFactory = mock(XmlFormatter.WriterFactory.class);

        this.page = new WikiPageDummy("page", "content", injector);
        right = new TestSummary(1, 0, 0, 0);
        wrong = new TestSummary(0, 1, 0, 0);
        exception = new TestSummary(0, 0, 0, 1);
    }

    @After
    public void clearStaticFields() {
        BaseFormatter.finalErrorCount = 0;
    }

    @Test
    public void testTestTextFormatter() throws Exception {
        assertShouldCountTestResults(new TestTextFormatter(response));
    }

    @Test
    public void testXmlFormatter() throws Exception {
        assertShouldCountTestResults(new XmlFormatter(context, page, writerFactory) {
            @Override
            protected void writeResults() {
            }
        });
    }

    @Test
    public void testTestHtmlFormatter() throws Exception {
        assertShouldCountTestResults(new TestHtmlFormatter(context, testPage, mock(HtmlPageFactory.class)) {
            @Override
            protected void writeData(String output) {
            }
        });
    }

    @Test
    public void testPageHistoryFormatter() throws Exception {
        assertShouldCountTestResults(new PageHistoryFormatter(context, testPage, writerFactory) {
            @Override
            protected void writeResults() {
            }
        });
    }

    public void assertShouldCountTestResults(BaseFormatter formatter) throws Exception {
        TimeMeasurement timeMeasurement = mock(TimeMeasurement.class);
        when(timeMeasurement.startedAtDate()).thenReturn(new Date(0));
        when(timeMeasurement.elapsedSeconds()).thenReturn(0d);

        formatter.announceNumberTestsToRun(3);
        formatter.testComplete(page, right, timeMeasurement);
        formatter.testComplete(page, wrong, timeMeasurement);
        formatter.testComplete(page, exception, timeMeasurement);
        formatter.allTestingComplete(new TimeMeasurement().start().stop());

        assertEquals(3, formatter.testCount);
        assertEquals(2, formatter.failCount);
        if (!(formatter instanceof PageHistoryFormatter))
            assertEquals(2, BaseFormatter.finalErrorCount);
    }

}
