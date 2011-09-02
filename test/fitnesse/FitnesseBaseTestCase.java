package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Module;
import com.google.inject.util.Modules;
import fitnesse.responders.files.SampleFileUtility;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static util.FileUtil.deleteFileSystemDirectory;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/22/11
 * Time: 10:24 PM
 */
public class FitnesseBaseTestCase {

    private FitNesseContext context;
    protected SampleFileUtility samples;
    private static final AbstractModule NOOP_MODULE =
            new AbstractModule() {
                @Override
                protected void configure() {
                }
            };

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

    public void inject() {
        inject(getTestModule());
    }

    public void inject(Module... testModules) {
        Guice.createInjector(
                Modules.override(new FitNesseModule())
                        .with(testModules))
                .injectMembers(this);
    }

    protected Module getTestModule() {
        return NOOP_MODULE;
    }

    @Before
    public void beforeAll() {
        inject();
    }

    @After
    public void afterAll() {
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

        File classes = new File("classes");
        assertTrue(classes.exists());
        cp.append(classes.getAbsolutePath());

        File testResources = new File("../test-resources");
        assertTrue(testResources.exists());
        cp.append(File.pathSeparator).append(testResources.getAbsolutePath());

        File resources = new File("../resources");
        assertTrue(resources.exists());
        cp.append(File.pathSeparator).append(resources.getAbsolutePath());

        final File libRuntime = new File("../lib/runtime");
        assertTrue(libRuntime.exists());
        String[] jarList = libRuntime.list(
                new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return dir.equals(libRuntime) && name.endsWith(".jar");
                    }
                });
        for (String jar : jarList) {
            File runtimeJar = new File(libRuntime, jar);
            assertTrue(runtimeJar.exists());
            cp.append(File.pathSeparator).append(runtimeJar.getAbsolutePath());
        }

        return cp.toString();
    }
}
