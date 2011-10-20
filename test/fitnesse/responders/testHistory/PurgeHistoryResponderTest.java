package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.Clock;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

public class PurgeHistoryResponderTest extends FitnesseBaseTestCase {
    private TestHistory history;
    private File testResultsPath;
    private PurgeHistoryResponder responder;
    private MockRequest request;
    private HtmlPageFactory htmlPageFactory;
    private Clock clock;

    @Inject
    public void inject(Clock clock, HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath) {
        this.clock = clock;
        this.htmlPageFactory = htmlPageFactory;
        this.testResultsPath = new File(testResultsPath);
    }

    @Before
    public void setup() throws Exception {
        removeResultsDirectory();
        testResultsPath.mkdirs();
        history = new TestHistory();
        responder = new PurgeHistoryResponder(htmlPageFactory, clock, testResultsPath.getAbsolutePath());
        request = new MockRequest();
        request.setResource("TestPage");
    }

    @After
    public void teardown() {
        removeResultsDirectory();
    }

    private void removeResultsDirectory() {
        if (testResultsPath.exists())
            FileUtil.deleteFileSystemDirectory(testResultsPath);
    }

    private File addTestResult(File pageDirectory, String testResultFileName) throws IOException {
        File testResultFile = new File(pageDirectory, testResultFileName + ".xml");
        testResultFile.createNewFile();
        return testResultFile;
    }

    private File addPageDirectory(String pageName) {
        File pageDirectory = new File(testResultsPath, pageName);
        pageDirectory.mkdir();
        return pageDirectory;
    }

    @Test
    public void shouldBeAbleToSubtractDaysFromDates() throws Exception {
        Date date = makeDate("20090616171615");
        responder.setTodaysDate(date);
        Date resultDate = responder.getDateDaysAgo(10);
        Date tenDaysEarlier = makeDate("20090606171615");
        assertEquals(tenDaysEarlier, resultDate);
    }

    private Date makeDate(String dateString) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat(TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
        return format.parse(dateString);
    }

    @Test
    public void shouldBeAbleToDeleteSomeTestHistory() throws Exception {
        responder.setTodaysDate(makeDate("20090616000000"));
        File pageDirectory = addPageDirectory("SomePage");
        addTestResult(pageDirectory, "20090614000000_1_0_0_0");
        addTestResult(pageDirectory, "20090615000000_1_0_0_0");

        history.readHistoryDirectory(testResultsPath);
        PageHistory pageHistory = history.getPageHistory("SomePage");
        assertEquals(2, pageHistory.size());
        responder.deleteTestHistoryOlderThanDays(1);
        history.readHistoryDirectory(testResultsPath);
        pageHistory = history.getPageHistory("SomePage");
        assertEquals(1, pageHistory.size());
        assertNotNull(pageHistory.get(makeDate("20090615000000")));
        assertNull(pageHistory.get(makeDate("20090614000000")));
    }

    @Test
    public void shouldDeletePageHistoryDirectoryIfEmptiedByPurge() throws Exception {
        responder.setTodaysDate(makeDate("20090616000000"));
        File pageDirectory = addPageDirectory("SomePage");
        addTestResult(pageDirectory, "20090614000000_1_0_0_0");
        responder.deleteTestHistoryOlderThanDays(1);
        String[] files = testResultsPath.list();
        assertEquals(0, files.length);
    }

    @Test
    public void shouldDeleteHistoryFromRequestAndRedirect() throws Exception {
        StubbedPurgeHistoryResponder responder = new StubbedPurgeHistoryResponder(htmlPageFactory, clock, testResultsPath.getAbsolutePath());
        request.addInput("days", "30");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);
        assertEquals(30, responder.daysDeleted);
        assertEquals(303, response.getStatus());
        assertEquals("?testHistory", response.getHeader("Location"));
    }

    @Test
    public void shouldMakeErrorResponseWhenGetsInvalidNumberOfDays() throws Exception {
        request.addInput("days", "-42");
        Response response = responder.makeResponse(request);
        assertEquals(400, response.getStatus());
    }

    @Test
    public void shouldMakeErrorResponseWhenItGetsInvalidTypeForNumberOfDays() throws Exception {
        request.addInput("days", "bob");
        Response response = responder.makeResponse(request);
        assertEquals(400, response.getStatus());

    }

    private static class StubbedPurgeHistoryResponder extends PurgeHistoryResponder {
        public int daysDeleted = -1;

        private StubbedPurgeHistoryResponder(HtmlPageFactory htmlPageFactory, Clock clock, String testResultsPath) {
            super(htmlPageFactory, clock, testResultsPath);
        }

        @Override
        public void deleteTestHistoryOlderThanDays(int days) throws ParseException {
            daysDeleted = days;
        }
    }
}
