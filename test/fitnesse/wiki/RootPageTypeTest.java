package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContextModule;
import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.List;
import java.util.Properties;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/17/11
 * Time: 10:41 AM
 */
@RunWith(Parameterized.class)
public class RootPageTypeTest extends FitnesseBaseTestCase {

    private final Class<? extends WikiPage> wikiPageClass;
    private WikiPage root;

    @Parameterized.Parameters
    public static List parameters() {
        return asList(
                new Object[]{InMemoryPage.class},
                new Object[]{FileSystemPage.class});
    }

    public RootPageTypeTest(Class<? extends WikiPage> wikiPageClass) {
        this.wikiPageClass = wikiPageClass;
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, wikiPageClass.getName());
        return properties;
    }

    @Inject
    public void inject(@Named(FitNesseContextModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Test
    public void testRootType() throws Exception {
        assertThat(root, instanceOf(wikiPageClass));
    }
}
