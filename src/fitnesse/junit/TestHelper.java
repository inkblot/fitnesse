package fitnesse.junit;

import com.google.inject.Injector;
import fitnesse.responders.run.JavaFormatter;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import fitnesseMain.FitNesseMain;

public class TestHelper {

    private final String fitNesseRootPath;
    private final String outputPath;
    private final ResultsListener resultListener;

    private boolean debug = true;

    public static final String PAGE_TYPE_SUITE = "suite";
    public static final String PAGE_TYPE_TEST = "test";
    private Injector injector;

    public TestHelper(String fitNesseRootPath, String outputPath, Injector injector) {
        this(fitNesseRootPath, outputPath, new PrintTestListener(), injector);
    }

    public TestHelper(String fitNesseRootPath, String outputPath, ResultsListener resultListener, Injector injector) {
        this.fitNesseRootPath = fitNesseRootPath;
        this.outputPath = outputPath;
        this.resultListener = resultListener;
        this.injector = injector;
    }

    public TestSummary run(String pageName, String pageType) throws Exception {
        return run(pageName, pageType, null);
    }

    public TestSummary run(String pageName, String pageType, String suiteFilter, int port) throws Exception {
        JavaFormatter testFormatter = JavaFormatter.getInstance(pageName);
        testFormatter.setResultsRepository(new JavaFormatter.FolderResultsRepository(outputPath, fitNesseRootPath));
        testFormatter.setListener(resultListener);
        FitNesseMain.Arguments arguments = new FitNesseMain.Arguments();
        arguments.setDaysTillVersionsExpire("0");
        arguments.setInstallOnly(false);
        arguments.setOmitUpdates(true);
        arguments.setPort(String.valueOf(port));
        arguments.setRootPath(fitNesseRootPath);
        arguments.setCommand(getCommand(pageName, pageType, suiteFilter));
        FitNesseMain.launchFitNesse(arguments, injector);
        return testFormatter.getTotalSummary();
    }

    public TestSummary run(String pageName, String pageType, String suiteFilter) throws Exception {
        return run(pageName, pageType, suiteFilter, 0);
    }

    String getCommand(String pageName, String pageType, String suiteFilter) {
        String commandPrefix = pageName + "?" + pageType;
        if (suiteFilter != null)
            return commandPrefix + getCommandArgs() + "&suiteFilter=" + suiteFilter;
        else
            return commandPrefix + getCommandArgs();
    }

    private final static String COMMON_ARGS = "&nohistory=true&format=java";
    private final static String DEBUG_ARG = "&debug=true";

    private String getCommandArgs() {
        if (debug) {
            return DEBUG_ARG + COMMON_ARGS;
        }
        return COMMON_ARGS;
    }

    public void setDebugMode(boolean enabled) {
        debug = enabled;
    }


}
