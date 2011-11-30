package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.WikiModule;
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
    private HistoryComparatorResponder responder;
    private String rootPagePath;
    private String testResultsPath;
    private MockRequest request;
    private HistoryComparator mockedComparator;
    private String FIRST_FILE_PATH;
    private String SECOND_FILE_PATH;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath, @Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath) {
        this.htmlPageFactory = htmlPageFactory;
        this.rootPagePath = rootPagePath;
        this.testResultsPath = testResultsPath;
    }

    @Before
    public void setup() throws Exception {
        FIRST_FILE_PATH = rootPagePath + "/files/testResults/TestFolder/firstFakeFile"
                .replace('/', File.separatorChar);
        SECOND_FILE_PATH = rootPagePath + "/files/testResults/TestFolder/secondFakeFile"
                .replace('/', File.separatorChar);
        request = new MockRequest();
        mockedComparator = mock(HistoryComparator.class);

        responder = new HistoryComparatorResponder(mockedComparator, htmlPageFactory, testResultsPath);
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
        FileUtil.createFile(rootPagePath + "/files/testResults/TestFolder/firstFakeFile",
                "firstFile");
        FileUtil.createFile(rootPagePath + "/files/testResults/TestFolder/secondFakeFile",
                "secondFile");
    }

    @Test
    public void shouldBeAbleToMakeASimpleResponse() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(
                request);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void shouldGetTwoHistoryFilesFromRequest() throws Exception {
        responder.makeResponse(request);
        verify(mockedComparator).compare(FIRST_FILE_PATH, SECOND_FILE_PATH);
    }

    @Test
    public void shouldReturnErrorPageIfCompareFails() throws Exception {
        when(mockedComparator.compare(FIRST_FILE_PATH, SECOND_FILE_PATH)).thenReturn(
                false);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(
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
        SimpleResponse response = (SimpleResponse) responder.makeResponse(
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
        SimpleResponse response = (SimpleResponse) responder.makeResponse(
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
        SimpleResponse response = (SimpleResponse) responder.makeResponse(
                request);
        assertEquals(400, response.getStatus());
        assertHasRegexp(
                "Compare Failed because the wrong number of Input Files were given. Select two please.",
                response.getContent());
    }

    @Test
    public void shouldReturnAResponseWithResultContent() throws Exception {
        SimpleResponse response = (SimpleResponse) responder.makeResponse(
                request);
        verify(mockedComparator).getResultContent();
        assertHasRegexp("This is the content", response.getContent());
    }

}
