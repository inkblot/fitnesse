package fitnesse;

import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPageFactory;
import org.junit.After;
import util.FileUtil;

import java.io.File;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/16/11
 * Time: 10:14 PM
 */
public abstract class FitnesseBaseTestCase extends BaseInjectedTestCase {

    @Override
    protected Module[] getBaseModules() {
        return new Module[]{
                new FitNesseModule(getProperties(), getUserPass(), getRootPath(), getRootPageName(), getPort(), true)
        };
    }

    @After
    public void afterContextualText() {
        FileUtil.deleteFileSystemDirectory(injector.getInstance(Key.get(String.class, Names.named(FitNesseModule.ROOT_PATH))));
    }

    protected String getUserPass() {
        return null;
    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
    }

    protected final String getRootPath() {
        File rootPath = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName());
        assertTrue(rootPath.exists() || rootPath.mkdirs());
        return rootPath.getAbsolutePath();
    }

    protected String getRootPageName() {
        return "RooT";
    }

    protected int getPort() {
        return 1999;
    }

}
