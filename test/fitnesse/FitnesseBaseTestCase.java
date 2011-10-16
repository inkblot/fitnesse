package fitnesse;

import com.google.inject.*;
import com.google.inject.name.Names;
import fitnesse.responders.files.SampleFileUtility;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;
import util.Clock;
import util.ClockUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static util.FileUtil.deleteFileSystemDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/22/11
 * Time: 10:24 PM
 */
public class FitnesseBaseTestCase extends BaseInjectedTestCase {

    public static final int DEFAULT_PORT = 1999;

    private FitNesseContext context;
    protected SampleFileUtility samples;

    protected final FitNesseContext makeContext() throws Exception {
        return makeContext(DEFAULT_PORT);
    }

    protected final FitNesseContext makeContext(int port) throws Exception {
        return makeContext(InMemoryPage.class, port);
    }

    protected final FitNesseContext makeContext(Class<? extends WikiPage> wikiPageClass) throws Exception {
        return makeContext(wikiPageClass, DEFAULT_PORT);
    }

    protected final FitNesseContext makeContext(Class<? extends WikiPage> wikiPageClass, int port) throws Exception {
        assertNull(context);
        context = FitNesseContext.makeContext(injector, getRootPath(), "RooT", port, true);
        assertThat(context.root, instanceOf(wikiPageClass));
        return context;
    }

    protected final String getRootPath() {
        File rootPath = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName());
        assertTrue(rootPath.exists() || rootPath.mkdirs());
        return rootPath.getAbsolutePath();
    }

    protected final String getRootPagePath() {
        assertNotNull("A context must have already been made", context);
        return context.getInjector().getInstance(Key.get(String.class, Names.named(FitNesseContextModule.ROOT_PAGE_PATH)));
    }

    protected final void makeSampleFiles() {
        assertNotNull("A context must have already been made", context);
        samples = new SampleFileUtility(getRootPagePath());
        samples.makeSampleFiles();
    }

    protected final Module[] getBaseModules() {
        return new Module[]{new FitNesseModule(getFitNesseProperties(), getUserpass())};
    }

    protected String getUserpass() {
        return null;
    }

    protected Properties getFitNesseProperties() {
        Properties properties = new Properties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        return properties;
    }

    @Before
    public final void beforeAllFitNesseTests() {
        inject();
    }

    @After
    public final void afterAllFitNesseTests() {
        deleteFileSystemDirectory(getRootPath());
        context = null;
        samples = null;
        inject(NOOP_MODULE);
        ClockUtil.inject((Clock) null);
    }

    protected String classPath() {
        StringBuilder cp = new StringBuilder();

        cp.append(classPathDir("test/classes"));
        cp.append(File.pathSeparator);
        cp.append(classPathDir("../test-resources"));
        cp.append(File.pathSeparator);
        cp.append(classPathDir("classes"));
        cp.append(File.pathSeparator);
        cp.append(classPathDir("../resources"));
        cp.append(File.pathSeparator);
        cp.append(classPathJarDir("../lib/runtime"));
//        cp.append(File.pathSeparator);
//        cp.append(classPathJarDir("../lib/compile"));
        cp.append(File.pathSeparator);
        cp.append(classPathJarDir("../lib/test"));

        return cp.toString();
    }

    private String classPathDir(String dir) {
        File dirFile = new File(dir);
        assertTrue(dirFile.exists());
        return dirFile.getAbsolutePath();
    }

    private String classPathJarDir(String jarDir) {
        final File jarDirFile = new File(jarDir);
        assertTrue(jarDirFile.exists());
        File[] jarList = jarDirFile.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return dir.equals(jarDirFile) && name.endsWith(".jar");
            }
        });
        String[] jarPaths = new String[jarList.length];
        for (int index = 0; index < jarPaths.length; index++) {
            assertTrue(jarList[index].exists());
            jarPaths[index] = jarList[index].getAbsolutePath();
        }
        return StringUtils.join(jarPaths, File.pathSeparator);
    }
}
