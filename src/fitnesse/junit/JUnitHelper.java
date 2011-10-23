package fitnesse.junit;

import fitnesse.responders.run.JavaFormatter;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesseMain.FitNesseMain;
import junit.framework.Assert;

import java.util.Properties;

public class JUnitHelper {

    private static final String PAGE_TYPE_SUITE = "suite";
    private static final String PAGE_TYPE_TEST = "test";
    private final static String COMMON_ARGS = "&nohistory=true&format=java";
    private final static String DEBUG_ARG = "&debug=true";

    private int port = 0;
    private boolean debug = true;
    private final String rootPath;
    private final String outputDir;
    private final ResultsListener resultsListener;
    private final Properties properties;

    public void setPort(int port) {
        this.port = port;
    }

    public JUnitHelper(String rootPath, String outputPath, ResultsListener resultsListener, Properties properties) {
        this.rootPath = rootPath;
        this.outputDir = outputPath;
        this.resultsListener = resultsListener;
        this.properties = properties;
    }

    public TestSummary run(String pageName, String pageType, String suiteFilter, int port) throws Exception {
        JavaFormatter testFormatter = JavaFormatter.getInstance(pageName);
        testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputDir));
        testFormatter.setListener(resultsListener);
        FitNesseMain.Arguments arguments = new FitNesseMain.Arguments();
        arguments.setDaysTillVersionsExpire("0");
        arguments.setPort(String.valueOf(port));
        arguments.setRootPath(rootPath);
        arguments.setCommand(getCommand(pageName, pageType, suiteFilter));
        FitNesseMain.launchFitNesse(arguments, properties);
        return testFormatter.getTotalSummary();
    }

    String getCommand(String pageName, String pageType, String suiteFilter) {
        String commandPrefix = pageName + "?" + pageType;
        if (suiteFilter != null)
            return commandPrefix + getCommandArgs() + "&suiteFilter=" + suiteFilter;
        else
            return commandPrefix + getCommandArgs();
    }

    private String getCommandArgs() {
        if (debug) {
            return DEBUG_ARG + COMMON_ARGS;
        }
        return COMMON_ARGS;
    }

    public void setDebugMode(boolean enabled) {
        debug = enabled;
    }


    public void assertTestPasses(String testName) throws Exception {
        assertPasses(testName, PAGE_TYPE_TEST, null);
    }

    public void assertSuitePasses(String suiteName, String suiteFilter) throws Exception {
        assertPasses(suiteName, PAGE_TYPE_SUITE, suiteFilter);
    }

    public void assertPasses(String pageName, String pageType, String suiteFilter) throws Exception {
        TestSummary summary = run(pageName, pageType, suiteFilter, port);
        Assert.assertEquals("wrong", 0, summary.wrong);
        Assert.assertEquals("exceptions", 0, summary.exceptions);
        Assert.assertTrue("at least one test executed", summary.right > 0);
    }
}
