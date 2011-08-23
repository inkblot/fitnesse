package fitnesse;

import fitnesse.wiki.WikiPage;

import java.io.File;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/22/11
 * Time: 10:24 PM
 */
public class FitnesseBaseTestCase {

    protected final FitNesseContext makeContext() {
        return makeContext(null);
    }

    protected FitNesseContext makeContext(WikiPage root) {
        return new FitNesseContext(root, new File(System.getProperty("java.io.tmpdir"), getClass().getSimpleName()).getAbsolutePath());
    }
}
