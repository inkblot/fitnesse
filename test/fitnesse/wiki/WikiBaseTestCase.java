package fitnesse.wiki;

import com.google.inject.AbstractModule;
import com.google.inject.Key;
import com.google.inject.Module;
import com.google.inject.name.Names;
import fitnesse.wikitext.parser.Context;
import fitnesse.wikitext.parser.MapVariableSource;
import fitnesse.wikitext.parser.VariableSource;
import org.junit.After;
import org.movealong.junit.BaseInjectedTestCase;
import util.FileUtil;
import util.UtilModule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/29/11
 * Time: 9:28 PM
 */
public class WikiBaseTestCase extends BaseInjectedTestCase {
    @Override
    protected Module[] getBaseModules() {
        return new Module[] {
                new WikiModule(getRootPath(), getRootPageName(), getProperties()),
                new UtilModule(),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        Map<String, String> contextVariables = new HashMap<String, String>();
                        contextVariables.put("FITNESSE_ROOTPATH", getRootPath());
                        bind(VariableSource.class).annotatedWith(Context.class).toInstance(new MapVariableSource(contextVariables));
                    }
                }
        };
    }

    protected Properties getProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiModule.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
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

    @After
    public final void afterContextualTest() {
        FileUtil.deleteFileSystemDirectory(injector.getInstance(Key.get(String.class, Names.named(WikiModule.ROOT_PATH))));
    }
}
