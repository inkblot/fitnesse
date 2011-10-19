package fitnesse.junit;

import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class JUnitHelperTest {

    private JUnitHelper helper;

    @Before
    public void setUp() {
        helper = new JUnitHelper("", "", new PrintTestListener(), getProperties());
    }

    @After
    public void tearDown() {
        helper = null;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
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
