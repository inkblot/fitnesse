package fitnesse.junit;


import com.google.inject.Injector;
import fitnesse.responders.run.ResultsListener;
import fitnesse.responders.run.TestSummary;
import junit.framework.Assert;

public class JUnitHelper {

    private final TestHelper helper;
    private int port = 0;

    public void setPort(int port) {
        this.port = port;
    }

    public JUnitHelper(String fitNesseRootPath, String outputPath, Injector injector) {
        this(fitNesseRootPath, outputPath, new PrintTestListener(), injector);
    }

    public JUnitHelper(String fitNesseDir, String outputDir,
                       ResultsListener resultsListener, Injector injector) {
        helper = new TestHelper(fitNesseDir, outputDir, resultsListener, injector);
    }

    public void setDebugMode(boolean enabled) {
        helper.setDebugMode(enabled);
    }

    public void assertTestPasses(String testName) throws Exception {
        assertPasses(testName, TestHelper.PAGE_TYPE_TEST, null);
    }

    public void assertSuitePasses(String suiteName, String suiteFilter) throws Exception {
        assertPasses(suiteName, TestHelper.PAGE_TYPE_SUITE, suiteFilter);
    }

    public void assertPasses(String pageName, String pageType, String suiteFilter) throws Exception {
        TestSummary summary = helper.run(pageName, pageType, suiteFilter, port);
        Assert.assertEquals("wrong", 0, summary.wrong);
        Assert.assertEquals("exceptions", 0, summary.exceptions);
        Assert.assertTrue("at least one test executed", summary.right > 0);
    }
}
