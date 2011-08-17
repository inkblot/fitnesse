package fitnesse.junit;

import fitnesse.responders.run.JavaFormatter;
import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

public class JUnitHelperExampleTest {
    JUnitHelper helper;
    private String[] expectedTestsWithSuiteFilter = new String[]{
            "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.ErikPragtBug",
            "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim"
    };

    @Before
    public void prepare() {
        helper = new JUnitHelper("..",
                new File(System.getProperty("java.io.tmpdir"), "fitnesse").getAbsolutePath());
        JavaFormatter.dropInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
    }

    @Test
    public void assertTestPasses_RunsATestThroughFitNesseAndWeCanInspectTheResultUsingJavaFormatter() throws Exception {
        String testName = "FitNesse.SuiteAcceptanceTests.SuiteSlimTests.SystemUnderTestTest";
        helper.assertTestPasses(testName);
        JavaFormatter formatter = JavaFormatter.getInstance(testName);
        Assert.assertEquals(testName, formatter.getTestsExecuted().get(0));
        Assert.assertEquals(1, formatter.getTestsExecuted().size());
    }

    @Test
    public void assertSuitePasses_appliesSuiteFilterIfDefined() throws Exception {
        helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", "testSuite");

        JavaFormatter formatter = JavaFormatter.getInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
        Assert.assertEquals(new HashSet<String>(Arrays.asList(expectedTestsWithSuiteFilter)),
                new HashSet<String>(formatter.getTestsExecuted()));

    }

    @Test
    public void helperWillFailTestsIfNoTestsAreExecuted() throws Exception {
        try {
            helper.assertSuitePasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests", "nonExistingFilter");

        } catch (AssertionError ae) {
            Assert.assertEquals("at least one test executed", ae.getMessage());
        }

        JavaFormatter formatter = JavaFormatter.getInstance("FitNesse.SuiteAcceptanceTests.SuiteSlimTests");
        Assert.assertEquals(new HashSet<String>(),
                new HashSet<String>(formatter.getTestsExecuted()));

    }

    @Test
    public void dummy() {

    }
}
