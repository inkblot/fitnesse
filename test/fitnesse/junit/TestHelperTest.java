package fitnesse.junit;

import com.google.inject.Inject;
import com.google.inject.Injector;
import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestHelperTest extends FitnesseBaseTestCase {

    @Inject
    public Injector injector;

    @Test
    public void getCommand_formatting() {
        TestHelper th = new TestHelper("", "", injector);
        assertEquals("test, no filter", "TestName?test&debug=true&nohistory=true&format=java", th.getCommand("TestName", "test", null));
        assertEquals("suite, no filter", "SuiteName?suite&debug=true&nohistory=true&format=java", th.getCommand("SuiteName", "suite", null));
        assertEquals("suite, with filter", "SuiteName?suite&debug=true&nohistory=true&format=java&suiteFilter=xxx", th.getCommand("SuiteName", "suite", "xxx"));
    }

    @Test
    public void getCommand_formatting_without_debug() {
        TestHelper th = new TestHelper("", "", injector);
        th.setDebugMode(false);
        assertEquals("test, no filter", "TestName?test&nohistory=true&format=java", th.getCommand("TestName", "test", null));
        assertEquals("suite, no filter", "SuiteName?suite&nohistory=true&format=java", th.getCommand("SuiteName", "suite", null));
        assertEquals("suite, with filter", "SuiteName?suite&nohistory=true&format=java&suiteFilter=xxx", th.getCommand("SuiteName", "suite", "xxx"));
    }
}
