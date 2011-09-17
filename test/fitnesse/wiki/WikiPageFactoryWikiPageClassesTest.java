package fitnesse.wiki;

import com.google.inject.Guice;
import fitnesse.ComponentFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import util.Clock;
import util.ClockUtil;
import util.DiskFileSystem;
import util.UtilModule;

import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/17/11
 * Time: 10:41 AM
 */
@RunWith(Parameterized.class)
public class WikiPageFactoryWikiPageClassesTest {

    private final Class<? extends WikiPage> wikiPageClass;

    @Parameterized.Parameters
    public static List parameters() {
        return asList(
                new Object[]{InMemoryPage.class},
                new Object[]{FileSystemPage.class});
    }

    public WikiPageFactoryWikiPageClassesTest(Class<? extends WikiPage> wikiPageClass) {
        this.wikiPageClass = wikiPageClass;
    }

    @BeforeClass
    public static void setUpClass() {
        Guice.createInjector(new UtilModule());
    }

    @AfterClass
    public static void tearDownClass() {
        ClockUtil.inject((Clock) null);
    }

    @Test
    public void createRoot() throws Exception {
        Properties properties = new Properties();
        WikiPageFactory wikiPageFactory = new WikiPageFactory(new DiskFileSystem(), wikiPageClass, properties);
        ComponentFactory componentFactory = new ComponentFactory(properties);
        assertNotNull(wikiPageFactory.makeRootPage(".", "RooT", componentFactory));
    }
}
