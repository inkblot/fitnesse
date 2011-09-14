package fitnesse;

import com.google.inject.*;
import fitnesse.responders.files.SampleFileUtility;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Properties;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static util.FileUtil.deleteFileSystemDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/22/11
 * Time: 10:24 PM
 */
public class FitnesseBaseTestCase extends BaseInjectedTestCase {

    private FitNesseContext context;
    protected SampleFileUtility samples;

    protected final FitNesseContext makeContext() {
        return makeContext("RooT");
    }

    protected final FitNesseContext makeContext(String rootName) {
        return makeContext(InMemoryPage.makeRoot(rootName));
    }

    protected final FitNesseContext makeContext(WikiPage root) {
        if (context == null) {
            File rootPath = new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName());
            assertTrue(rootPath.mkdirs());
            context = new FitNesseContext(root, rootPath.getAbsolutePath());
        }
        return context;
    }

    protected final void installUpdates() throws Exception {
        assertNotNull(context);
        assertTrue(new File(context.rootPagePath).mkdir());
        UpdaterImplementation updater = new UpdaterImplementation(context);
        updater.update();
    }

    protected final void makeSampleFiles() {
        assertNotNull(context);
        samples = new SampleFileUtility(context.rootPagePath);
        samples.makeSampleFiles();
    }

    protected final Module[] getBaseModules() {
        return new Module[]{new FitNesseModule(getFitNesseProperties(), getUserpass())};
    }

    protected String getUserpass() {
        return null;
    }

    protected Properties getFitNesseProperties() {
        return new Properties();
    }

    @Before
    public final void beforeAllFitNesseTests() {
        inject();
    }

    @After
    public final void afterAllFitNesseTests() {
        if (context != null) {
            deleteFileSystemDirectory(context.rootPath);
            context = null;
        }
        if (samples != null) {
            samples = null;
        }
        inject(NOOP_MODULE);
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
