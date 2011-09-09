package fitnesse.junit;

import fitnesse.FitnesseBaseTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JUnitHelperTest extends FitnesseBaseTestCase {

    private JUnitHelper helper;

    @Before
    public void setUp() {
        helper = new JUnitHelper("", "", injector);
    }

    @After
    public void tearDown() {
        helper = null;
    }

    @Test
    public void getCommand_formatting() {
        assertEquals("test, no filter", "TestName?test&debug=true&nohistory=true&format=java", helper.getCommand("TestName", "test", null));
        assertEquals("suite, no filter", "SuiteName?suite&debug=true&nohistory=true&format=java", helper.getCommand("SuiteName", "suite", null));
        assertEquals("suite, with filter", "SuiteName?suite&debug=true&nohistory=true&format=java&suiteFilter=xxx", helper.getCommand("SuiteName", "suite", "xxx"));
    }

    @Test
    public void getCommand_formatting_without_debug() {
        helper.setDebugMode(false);
        assertEquals("test, no filter", "TestName?test&nohistory=true&format=java", helper.getCommand("TestName", "test", null));
        assertEquals("suite, no filter", "SuiteName?suite&nohistory=true&format=java", helper.getCommand("SuiteName", "suite", null));
        assertEquals("suite, with filter", "SuiteName?suite&nohistory=true&format=java&suiteFilter=xxx", helper.getCommand("SuiteName", "suite", "xxx"));
    }
}
