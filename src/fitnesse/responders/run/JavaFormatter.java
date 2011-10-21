package fitnesse.responders.run;

import fitnesse.responders.run.formatters.BaseFormatter;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.apache.commons.io.IOUtils;
import util.TimeMeasurement;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JavaFormatter extends BaseFormatter {

    private String mainPageName;
    private boolean isSuite = true;
    public static final String SUMMARY_FOOTER = "</table>";
    public static final String SUMMARY_HEADER = "<table><tr><td>Name</td><td>Right</td><td>Wrong</td><td>Exceptions</td></tr>";

    public interface ResultsRepository {
        void open(String string) throws IOException;

        void close() throws IOException;

        void write(String content) throws IOException;
    }

    public static class FolderResultsRepository implements ResultsRepository {
        private String outputPath;
        private Writer currentWriter;

        public FolderResultsRepository(String outputPath) throws IOException {
            this.outputPath = outputPath;
            initFolder();
        }

        public void close() throws IOException {
            if (currentWriter != null) {
                currentWriter.write("</body></html>");
                currentWriter.close();
            }
        }

        public void open(String testName) throws IOException {
            currentWriter = new FileWriter(new File(outputPath, testName + ".html"));

            currentWriter.write("<head><title>");
            currentWriter.write(testName);
            currentWriter
                    .write("</title><meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>"
                            + "<link rel='stylesheet' type='text/css' href='fitnesse.css' media='screen'/>"
                            + "<link rel='stylesheet' type='text/css' href='fitnesse_print.css' media='print'/>"
                            + "<script src='fitnesse.js' type='text/javascript'></script>" + "</head><body><h2>");
            currentWriter.write(testName);
            currentWriter.write("</h2>");

        }

        public void write(String content) throws IOException {
            currentWriter.write(content.replace("src=\"/files/images/", "src=\"images/"));
        }

        private void addFile(String file, String relativeFilePath) throws IOException {
            File dst = new File(outputPath, relativeFilePath);
            dst.getParentFile().mkdirs();
            InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
            OutputStream out = new FileOutputStream(dst);
            IOUtils.copy(in, out);
            in.close();
            out.close();
        }

        private void initFolder() throws IOException {
            addFile("Resources/FitNesseRoot/files/css/fitnesse_base.css", "fitnesse.css");
            addFile("Resources/FitNesseRoot/files/javascript/fitnesse.js", "fitnesse.js");
            addFile("Resources/FitNesseRoot/files/images/collapsableOpen.gif", "images/collapsableOpen.gif");
            addFile("Resources/FitNesseRoot/files/images/collapsableClosed.gif", "images/collapsableClosed.gif");
        }
    }

    private TestSummary totalSummary = new TestSummary();

    @Override
    public void writeHead(String pageType) {

    }

    public String getFullPath(final WikiPage wikiPage) {
        return new WikiPagePath(wikiPage).toString();
    }

    private List<String> visitedTestPages = new ArrayList<String>();
    private Map<String, TestSummary> testSummaries = new HashMap<String, TestSummary>();

    @Override
    public void newTestStarted(WikiPage test, TimeMeasurement timeMeasurement) throws IOException {
        resultsRepository.open(getFullPath(test));
        if (listener != null)
            listener.newTestStarted(test, timeMeasurement);
    }

    @Override
    public void setExecutionLogAndTrackingId(String stopResponderId, CompositeExecutionLog log) {
    }

    public void testComplete(WikiPage test, TestSummary testSummary, TimeMeasurement timeMeasurement) throws IOException {
        String fullPath = getFullPath(test);
        visitedTestPages.add(fullPath);
        totalSummary.add(testSummary);
        testSummaries.put(fullPath, new TestSummary(testSummary));
        resultsRepository.close();
        isSuite = isSuite && (!mainPageName.equals(fullPath));
        if (listener != null)
            listener.testComplete(test, testSummary, timeMeasurement);
    }

    TestSummary getTestSummary(String testPath) {
        return testSummaries.get(testPath);
    }

    @Override
    public void testOutputChunk(String output) throws IOException {
        resultsRepository.write(output);
    }

    @Override
    public void testSystemStarted(TestSystem testSystem, String testSystemName, String testRunner) {
    }

    private ResultsRepository resultsRepository;

    public TestSummary getTotalSummary() {
        return totalSummary;
    }

    public void setTotalSummary(TestSummary testSummary) {
        totalSummary = testSummary;
    }

    public void setResultsRepository(ResultsRepository mockResultsRepository) {
        this.resultsRepository = mockResultsRepository;

    }

    // package-private to prevent instantiation apart from getInstance and tests
    JavaFormatter(String suiteName) {
        this.mainPageName = suiteName;
    }

    private static Map<String, JavaFormatter> allocatedInstances = new HashMap<String, JavaFormatter>();
    private ResultsListener listener;

    public synchronized static JavaFormatter getInstance(String testName) {
        JavaFormatter existing = allocatedInstances.get(testName);
        if (existing != null)
            return existing;
        existing = new JavaFormatter(testName);
        allocatedInstances.put(testName, existing);
        return existing;
    }

    @Override
    public void allTestingComplete(TimeMeasurement totalTimeMeasurement) throws IOException {
        super.allTestingComplete(totalTimeMeasurement);
        if (isSuite)
            writeSummary(mainPageName);
        if (listener != null)
            listener.allTestingComplete(totalTimeMeasurement);
    }

    public void writeSummary(String suiteName) throws IOException {
        resultsRepository.open(suiteName);
        resultsRepository.write(SUMMARY_HEADER);
        for (String s : visitedTestPages) {
            resultsRepository.write(summaryRow(s, testSummaries.get(s)));
        }
        resultsRepository.write(SUMMARY_FOOTER);
        resultsRepository.close();
    }

    public String summaryRow(String testName, TestSummary testSummary) {
        StringBuilder sb = new StringBuilder();
        sb.append("<tr class=\"").append(getCssClass(testSummary)).append("\"><td>").append(
                "<a href=\"").append(testName).append(".html\">").append(testName).append("</a>").append(
                "</td><td>").append(testSummary.right).append("</td><td>").append(testSummary.wrong)
                .append("</td><td>").append(testSummary.exceptions).append("</td></tr>");
        return sb.toString();
    }

    private String getCssClass(TestSummary ts) {
        if (ts.exceptions > 0)
            return "error";
        if (ts.wrong > 0)
            return "fail";
        if (ts.right > 0)
            return "pass";
        return "plain";
    }

    public void setListener(ResultsListener listener) {
        this.listener = listener;
    }

    public List<String> getTestsExecuted() {
        return visitedTestPages;
    }

    public static void dropInstance(String testName) {
        allocatedInstances.remove(testName);
    }

}
