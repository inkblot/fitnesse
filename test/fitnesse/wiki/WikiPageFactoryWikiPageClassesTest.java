package fitnesse.wiki;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;

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

    @Inject
    public void inject(WikiPageFactory wikiPageFactory) {
        this.wikiPageFactory = wikiPageFactory;
    }

    @Override
    protected Module getOverrideModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(new TypeLiteral<Class<? extends WikiPage>>(){}).toInstance(wikiPageClass);
            }
        };
    }

    @Test
    public void createRoot() throws Exception {
        assertNotNull(wikiPageFactory.makeRootPage(getRootPath(), "RooT"));
    }
}
