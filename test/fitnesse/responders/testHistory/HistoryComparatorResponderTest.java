package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static util.RegexAssertions.assertHasRegexp;

public class HistoryComparatorResponderTest extends FitnesseBaseTestCase {
    public HistoryComparatorResponder responder;
    public FitNesseContext context;
    public WikiPage root;
    public MockRequest request;
    public HistoryComparator mockedComparator;
    private String FIRST_FILE_PATH;
    private String SECOND_FILE_PATH;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setup() throws Exception {
        context = makeContext();
        root = context.root;
        FIRST_FILE_PATH = getRootPagePath() + "/files/testResults/TestFolder/firstFakeFile"
                .replace('/', File.separatorChar);
        SECOND_FILE_PATH = getRootPagePath() + "/files/testResults/TestFolder/secondFakeFile"
                .replace('/', File.separatorChar);
        request = new MockRequest();
        mockedComparator = mock(HistoryComparator.class);

        responder = new HistoryComparatorResponder(mockedComparator, htmlPageFactory);
        responder.testing = true;
        List<String> resultContent = new ArrayList<String>();
        resultContent.add("pass");
        when(mockedComparator.getResultContent()).thenReturn(resultContent);
        when(mockedComparator.compare(FIRST_FILE_PATH, SECOND_FILE_PATH)).thenReturn(
                true);
        mockedComparator.firstTableResults = new ArrayList<String>();
        mockedComparator.secondTableResults = new ArrayList<String>();
        mockedComparator.firstTableResults
                .add("<table><tr><td>This is the content</td></tr></table>");
        mockedComparator.secondTableResults
                .add("<table><tr><td>This is the content</td></tr></table>");

        request.addInput("TestResult_firstFakeFile", "");
        request.addInput("TestResult_secondFakeFile", "");
        request.setResource("TestFolder");
        FileUtil.createFile(getRootPagePath() + "/files/testResults/TestFolder/firstFakeFile",
                "firstFile");
        FileUtil.createFile(getRootPagePath() + "/files/testResults/TestFolder/secondFakeFile",
                "secondFile");
    }

    @Test
    public void shouldBeAbleToMakeASimpleResponse() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
                request);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGetTwoHistoryFilesFromRequest() throws Exception {
        responder.makeResponse(context, request);
        verify(mockedComparator).compare(FIRST_FILE_PATH, SECOND_FILE_PATH);
    }

    @Test
    public void shouldReturnErrorPageIfCompareFails() throws Exception {
        when(mockedComparator.compare(FIRST_FILE_PATH, SECOND_FILE_PATH)).thenReturn(
                false);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
                request);
        assertEquals(400, response.getStatus());
        assertHasRegexp(
                "These files could not be compared.  They might be suites, or something else might be wrong.",
                response.getContent());
    }

    @Test
    public void shouldReturnErrorPageIfFilesAreInvalid() throws Exception {
        request = new MockRequest();
        request.addInput("TestResult_firstFile", "");
        request.addInput("TestResult_secondFile", "");
        request.setResource("TestFolder");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
                request);
        assertEquals(400, response.getStatus());
        assertHasRegexp("Compare Failed because the files were not found.",
                response.getContent());
    }

    @Test
    public void shouldReturnErrorPageIfThereAreTooFewInputFiles()
            throws Exception {
        request = new MockRequest();
        request.addInput("TestResult_firstFile", "");
        request.setResource("TestFolder");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
                request);
        assertEquals(400, response.getStatus());
        assertHasRegexp(
                "Compare Failed because the wrong number of Input Files were given. Select two please.",
                response.getContent());
    }

    @Test
    public void shouldReturnErrorPageIfThereAreTooManyInputFiles()
            throws Exception {
        request.addInput("TestResult_thirdFakeFile", "");
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
                request);
        assertEquals(400, response.getStatus());
        assertHasRegexp(
                "Compare Failed because the wrong number of Input Files were given. Select two please.",
                response.getContent());
    }

    @Test
    public void shouldReturnAResponseWithResultContent() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context,
                request);
        verify(mockedComparator).getResultContent();
        assertHasRegexp("This is the content", response.getContent());
    }

}
