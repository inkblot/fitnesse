package fitnesse.responders.testHistory;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.VelocityFactory;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class HistoryComparatorResponder implements Responder {
    public HistoryComparator comparator;
    private SimpleDateFormat dateFormat = new SimpleDateFormat(
            TestHistory.TEST_RESULT_FILE_DATE_PATTERN);
    private VelocityContext velocityContext;
    private String firstFileName = "";
    private String secondFileName = "";
    private String firstFilePath;
    private String secondFilePath;
    public boolean testing = false;

    private final HtmlPageFactory htmlPageFactory;
    private File testHistoryDirectory;

    public HistoryComparatorResponder(HistoryComparator historyComparator, HtmlPageFactory htmlPageFactory, FitNesseContext context) {
        testHistoryDirectory = context.getTestHistoryDirectory();
        comparator = historyComparator;
        this.htmlPageFactory = htmlPageFactory;
    }

    @Inject
    public HistoryComparatorResponder(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
        comparator = new HistoryComparator();
    }

    public Response makeResponse(Request request)
            throws Exception {
        initializeResponseComponents(request);
        if (!getFileNameFromRequest(request))
            return makeErrorResponse(request,
                    "Compare Failed because the wrong number of Input Files were given. "
                            + "Select two please.");
        firstFilePath = composeFileName(request, firstFileName);
        secondFilePath = composeFileName(request, secondFileName);

        if (!filesExist())
            return makeErrorResponse(request,
                    "Compare Failed because the files were not found.");

        return makeResponseFromComparison(request);
    }

    private Response makeResponseFromComparison(Request request) throws Exception {
        if (comparator.compare(firstFilePath, secondFilePath))
            return makeValidResponse();
        else {
            String message = String.format("These files could not be compared."
                    + "  They might be suites, or something else might be wrong.");
            return makeErrorResponse(request, message);
        }
    }

    private boolean filesExist() {
        return ((new File(firstFilePath)).exists())
                || ((new File(secondFilePath)).exists());
    }

    private void initializeResponseComponents(Request request) throws IOException {
        if (comparator == null)
            comparator = new HistoryComparator();
        velocityContext = new VelocityContext();
        velocityContext.put("pageTitle", makePageTitle(request.getResource()));
    }

    private String composeFileName(Request request, String fileName) {
        return testHistoryDirectory.getPath() + File.separator
                + request.getResource() + File.separator + fileName;
    }

    private boolean getFileNameFromRequest(Request request) {
        firstFileName = "";
        secondFileName = "";
        Map<String, Object> inputs = request.getMap();
        Set<String> keys = inputs.keySet();
        return setFileNames(keys);
    }

    private boolean setFileNames(Set<String> keys) {
        for (String key : keys) {
            if (key.contains("TestResult_"))
                if (setFileNames(key))
                    return false;
        }
        return !(isEmpty(firstFileName) || isEmpty(secondFileName));
    }

    private boolean setFileNames(String key) {
        if (isEmpty(firstFileName))
            firstFileName = key.substring(key.indexOf("_") + 1);
        else if (isEmpty(secondFileName))
            secondFileName = key.substring(key.indexOf("_") + 1);
        else
            return true;
        return false;
    }

    private Response makeValidResponse() throws Exception {
        int count = 0;
        if (!testing) {
            velocityContext.put("firstFileName", dateFormat.parse(firstFileName));
            velocityContext.put("secondFileName", dateFormat.parse(secondFileName));
            velocityContext.put("completeMatch", comparator.allTablesMatch());
            velocityContext.put("comparator", comparator);
        }
        velocityContext.put("resultContent", comparator.getResultContent());
        velocityContext.put("firstTables", comparator.firstTableResults);
        velocityContext.put("secondTables", comparator.secondTableResults);
        velocityContext.put("count", count);
        String velocityTemplate = "fitnesse/templates/compareHistory.vm";
        Template template = VelocityFactory.getVelocityEngine().getTemplate(
                velocityTemplate);
        return makeResponseFromTemplate(template);

    }

    private Response makeResponseFromTemplate(Template template) throws Exception {
        StringWriter writer = new StringWriter();
        SimpleResponse response = new SimpleResponse();
        template.merge(velocityContext, writer);
        response.setContent(writer.toString());
        return response;
    }

    private PageTitle makePageTitle(String resource) {
        return new PageTitle("Test History", PathParser.parse(resource));

    }

    private Response makeErrorResponse(Request request, String message) throws Exception {
        return new ErrorResponder(message, htmlPageFactory).makeResponse(request);
    }
}
