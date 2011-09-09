package fitnesse.junit;


import com.google.inject.Injector;
import fitnesse.responders.run.JavaFormatter;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesseMain.FitNesseMain;
import junit.framework.Assert;

public class JUnitHelper {

    private static final String PAGE_TYPE_SUITE = "suite";
    private static final String PAGE_TYPE_TEST = "test";
    private final static String COMMON_ARGS = "&nohistory=true&format=java";
    private final static String DEBUG_ARG = "&debug=true";

    private int port = 0;
    private boolean debug = true;
    private String fitNesseDir;
    private String outputDir;
    private ResultsListener resultsListener;
    private Injector injector;

    public void setPort(int port) {
        this.port = port;
    }

    public JUnitHelper(String fitNesseRootPath, String outputPath, Injector injector) {
        this(fitNesseRootPath, outputPath, new PrintTestListener(), injector);
    }

    public JUnitHelper(String fitNesseDir, String outputDir,
                       ResultsListener resultsListener, Injector injector) {
        this.fitNesseDir = fitNesseDir;
        this.outputDir = outputDir;
        this.resultsListener = resultsListener;
        this.injector = injector;
    }

    public TestSummary run(String pageName, String pageType, String suiteFilter, int port) throws Exception {
        JavaFormatter testFormatter = JavaFormatter.getInstance(pageName);
        testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputDir, fitNesseDir));
        testFormatter.setListener(resultsListener);
        FitNesseMain.Arguments arguments = new FitNesseMain.Arguments();
        arguments.setDaysTillVersionsExpire("0");
        arguments.setInstallOnly(false);
        arguments.setOmitUpdates(true);
        arguments.setPort(String.valueOf(port));
        arguments.setRootPath(fitNesseDir);
        arguments.setCommand(getCommand(pageName, pageType, suiteFilter));
        FitNesseMain.launchFitNesse(arguments, injector);
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
