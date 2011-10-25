package util;

import java.io.File;
import java.io.FilenameFilter;
import java.util.regex.Pattern;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/24/11
 * Time: 7:23 PM
 */
public class ClassPathHelper {
    public static String classPath() {
        StringBuilder cp = new StringBuilder();

        cp.append(classPathDir("test/classes"));
        cp.append(classPathDir("../test-resources"));
        cp.append(classPathDir("classes"));
        cp.append(classPathDir("../resources"));
        cp.append(classPathJarDir("../lib/runtime"));
        cp.append(classPathJarDir("../lib/compile"));
        cp.append(classPathJarDir("../lib/test"));

        return cp.toString().replaceAll(Pattern.quote(File.pathSeparator) + "$", "");
    }

    public static String classPathDir(String dir) {
        File dirFile = new File(dir);
        if (dirFile.exists())
            return dirFile.getAbsolutePath() + File.pathSeparator;
        return "";
    }

    public static StringBuilder classPathJarDir(String jarDir) {
        final File jarDirFile = new File(jarDir);
        StringBuilder dirClassPath = new StringBuilder();
        if (jarDirFile.exists()) {
            File[] jarList = jarDirFile.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return dir.equals(jarDirFile) && name.endsWith(".jar");
                }
            });
            for (File aJarList : jarList) {
                assertTrue(aJarList.exists());
                dirClassPath.append(aJarList.getAbsolutePath()).append(File.pathSeparator);
            }
        }
        return dirClassPath;
    }
}
