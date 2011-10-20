package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.VelocityFactory;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.templateUtilities.PageTitle;
import org.apache.velocity.VelocityContext;

import java.io.File;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class TestHistoryResponder implements SecureResponder {
    private boolean generateNullResponseForTest;
    private final File testHistoryDirectory;

    @Inject
    public TestHistoryResponder(@Named(FitNesseModule.TEST_RESULTS_PATH) String testResultsPath) {
        this.testHistoryDirectory = new File(testResultsPath);
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();
        if (!generateNullResponseForTest) {
            TestHistory history = new TestHistory();
            String pageName = request.getResource();
            history.readPageHistoryDirectory(testHistoryDirectory, pageName);
            VelocityContext velocityContext = new VelocityContext();
            velocityContext.put("pageTitle", new PageTitle(makePageTitle(pageName)));
            velocityContext.put("testHistory", history);
            String velocityTemplate = "fitnesse/templates/testHistory.vm";
            if (formatIsXML(request)) {
                response.setContentType("text/xml");
                velocityTemplate = "fitnesse/templates/testHistoryXML.vm";
            }
            response.setContent(VelocityFactory.translateTemplate(velocityContext, velocityTemplate));
        }
        return response;
    }

    private String makePageTitle(String pageName) {
        return isEmpty(pageName) ?
                "Test History" :
                "Test History for " + pageName;
    }

    private boolean formatIsXML(Request request) {
        return (request.getInput("format") != null && request.getInput("format").toString().toLowerCase().equals("xml"));
    }

    public void generateNullResponseForTest() {
        generateNullResponseForTest = true;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
