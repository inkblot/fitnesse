package fitnesse.wiki;

import fitnesse.FitnesseBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
public class WikiPageFactoryWikiPageClassesTest extends FitnesseBaseTestCase {

    private final Class<? extends WikiPage> wikiPageClass;
    private WikiPageFactory wikiPageFactory;

    @Parameterized.Parameters
    public static List parameters() {
        return asList(
                new Object[]{InMemoryPage.class},
                new Object[]{FileSystemPage.class});
    }

    public WikiPageFactoryWikiPageClassesTest(Class<? extends WikiPage> wikiPageClass) {
        this.wikiPageClass = wikiPageClass;
    }

    @Override
    protected Properties getFitNesseProperties() {
        Properties properties = super.getFitNesseProperties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, wikiPageClass.getName());
        return properties;
    }

    @Before
    public void setUp() throws Exception {
        wikiPageFactory = makeContext(wikiPageClass).getWikiPageFactory();
    }

    @Test
    public void createRoot() throws Exception {
        assertNotNull(wikiPageFactory.makeRootPage(getRootPath(), "RooT"));
    }
}
