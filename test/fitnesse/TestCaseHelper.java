package fitnesse;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/17/11
 * Time: 8:11 PM
 */
public class TestCaseHelper {

    public static String getRootPath(String wikiName) {
        File rootPath = new File(System.getProperty("java.io.tmpdir"), wikiName);
        assertTrue(rootPath.exists() || rootPath.mkdirs());
        return rootPath.getAbsolutePath();
    }

}
