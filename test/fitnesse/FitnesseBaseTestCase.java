package fitnesse;

import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.WikiPage;
import org.junit.After;

import java.io.File;

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

    protected final FitNesseContext makeContext() {
        return makeContext(null);
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
        assertTrue(new File(context.rootPagePath).mkdir());
        UpdaterImplementation updater = new UpdaterImplementation(context);
        updater.update();
    }

    @After
    public void afterAll() {
        if (context != null) {
            deleteFileSystemDirectory(context.rootPath);
            context = null;
        }
    }
}
