package util;

import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.FilenameFilter;

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

    public static String classPathDir(String dir) {
        File dirFile = new File(dir);
        assertTrue(dirFile.exists());
        return dirFile.getAbsolutePath();
    }

    public static String classPathJarDir(String jarDir) {
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
