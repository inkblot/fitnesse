package fitnesse.responders.run.formatters;

import fitnesse.FitNesseContext;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.responders.run.TestSummary;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.TimeMeasurement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


@RunWith(Parameterized.class)
public class TestFormatterTest {
    private BaseFormatter formatter;

    public TestFormatterTest(BaseFormatter formatter) {
        this.formatter = formatter;
    }

    @Parameterized.Parameters
    public static Collection formatters() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT");
        FitNesseContext context = new FitNesseContext(root);
        ChunkedResponse response = mock(ChunkedResponse.class);
        WikiPageDummy page = new WikiPageDummy("testPage", "testContent");
        XmlFormatter.WriterFactory writerFactory = mock(XmlFormatter.WriterFactory.class);

        TestTextFormatter testTextFormatter = new TestTextFormatter(response);
        XmlFormatter xmlFormatter = new XmlFormatter(context, page, writerFactory) {
            @Override
            protected void writeResults() throws Exception {
            }
        };
        TestHtmlFormatter testHtmlFormatter = new TestHtmlFormatter(context, page, mock(HtmlPageFactory.class)) {
            @Override
            protected void writeData(String output) throws Exception {
            }
        };
        PageHistoryFormatter pageHistoryFormatter = new PageHistoryFormatter(context, page, writerFactory) {
            @Override
            protected void writeResults() throws Exception {
            }
        };
        return Arrays.asList(new Object[][]{
                {testTextFormatter},
                {xmlFormatter},
                {testHtmlFormatter},
                {pageHistoryFormatter},
        });
    }

    private WikiPageDummy page;
    private TestSummary right;
    private TestSummary wrong;
    private TestSummary exception;

    @Before
    public void setUp() throws Exception {
        page = new WikiPageDummy("page", "content");
        right = new TestSummary(1, 0, 0, 0);
        wrong = new TestSummary(0, 1, 0, 0);
        exception = new TestSummary(0, 0, 0, 1);
    }

    @After
    public void clearStaticFields() {
        BaseFormatter.finalErrorCount = 0;
    }

    @Test
    public void testComplete_shouldCountTestResults() throws Exception {
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
