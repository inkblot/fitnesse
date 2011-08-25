package fitnesse;

import fitnesse.responders.files.SampleFileUtility;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
import org.junit.After;

import java.io.File;

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
    private SampleFileUtility samples;

    protected final FitNesseContext makeContext() {
        return makeContext(InMemoryPage.makeRoot("RooT"));
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

    @After
    public void afterAll() {
        if (context != null) {
            deleteFileSystemDirectory(context.rootPath);
            context = null;
        }
        if (samples != null) {
            samples = null;
        }
    }
}
