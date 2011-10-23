// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;

import java.io.*;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class FileUtil {

    public static File createFile(String path, String content) {
        String names[] = path.split("/");
        if (names.length == 1)
            return createFile(new File(path), content);
        else {
            File parent = null;
            for (int i = 0; i < names.length - 1; i++) {
                parent = parent == null ? new File(names[i]) : new File(parent, names[i]);
                if (!parent.exists())
                    parent.mkdir();
            }
            File fileToCreate = new File(parent, names[names.length - 1]);
            return createFile(fileToCreate, content);
        }
    }

    public static File createFile(File file, String content) {
        FileOutputStream fileOutput = null;
        try {
            fileOutput = new FileOutputStream(file);
            fileOutput.write(content.getBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            if (fileOutput != null)
                try {
                    fileOutput.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
        return file;
    }

    public static boolean makeDir(String path) {
        return new File(path).mkdir();
    }

    public static void deleteFileSystemDirectory(String dirPath) {
        deleteFileSystemDirectory(new File(dirPath));
    }

    public static void deleteFileSystemDirectory(File current) {
        File[] files = current.listFiles();

        for (int i = 0; files != null && i < files.length; i++) {
            File file = files[i];
            if (file.isDirectory())
                deleteFileSystemDirectory(file);
            else
                deleteFile(file);
        }
        deleteFile(current);
    }

    public static void deleteFile(String filename) {
        deleteFile(new File(filename));
    }

    public static void deleteFile(File file) {
        if (!file.exists())
            return;
        for (int i = 0; i < 10; i++) {
            if (file.delete()) {
                waitUntilFileDeleted(file);
                return;
            }
            waitFor(10);
        }
        throw new RuntimeException("Could not delete '" + file.getAbsoluteFile() + "'");
    }

    private static void waitUntilFileDeleted(File file) {
        int i = 10;
        while (file.exists()) {
            if (--i <= 0) {
                System.out.println("Breaking out of delete wait");
                break;
            }
            waitFor(500);
        }
    }

    private static void waitFor(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            // stop waiting
        }
    }

    public static String getFileContent(String path) throws IOException {
        File input = new File(path);
        return getFileContent(input);
    }

    public static String getFileContent(File input) throws IOException {
        return new String(getFileBytes(input));
    }

    public static byte[] getFileBytes(File input) throws IOException {
        long size = input.length();
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(input);
            return new StreamReader(stream).readBytes((int) size);
        } finally {
            if (stream != null)
                stream.close();
        }
    }

    public static LinkedList<String> getFileLines(File file) throws IOException {
        LinkedList<String> lines = new LinkedList<String>();
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        while ((line = reader.readLine()) != null)
            lines.add(line);

        reader.close();
        return lines;
    }

    public static void writeLinesToFile(File file, List<?> lines) throws FileNotFoundException {
        PrintStream output = new PrintStream(new FileOutputStream(file));
        for (Object line1 : lines) {
            String line = (String) line1;
            output.println(line);
        }
        output.close();
    }

    public static void copyBytes(InputStream input, OutputStream output) throws IOException {
        StreamReader reader = new StreamReader(input);
        while (!reader.isEof())
            output.write(reader.readBytes(1000));
    }

    public static File createDir(String path) {
        makeDir(path);
        return new File(path);
    }

    public static File[] getDirectoryListing(File dir) {
        SortedSet<File> dirSet = new TreeSet<File>();
        SortedSet<File> fileSet = new TreeSet<File>();
        File[] files = dir.listFiles();
        if (files == null)
            return new File[0];
        for (File file : files) {
            if (file.isDirectory())
                dirSet.add(file);
            else
                fileSet.add(file);
        }
        List<File> fileList = new LinkedList<File>();
        fileList.addAll(dirSet);
        fileList.addAll(fileSet);
        return fileList.toArray(new File[fileList.size()]);
    }

    public static String buildPath(String[] parts) {
        return StringUtils.join(Arrays.asList(parts), System.getProperty("file.separator"));
    }

    public static List<String> breakFilenameIntoParts(String fileName) {
        return new ArrayList<String>(Arrays.asList(fileName.split("/")));
    }

    public static void addItemsToClasspath(String classpathItems) throws Exception {
        final String separator = System.getProperty("path.separator");
        String currentClassPath = System.getProperty("java.class.path");
        System.setProperty("java.class.path", currentClassPath + separator + classpathItems);
        String[] items = classpathItems.split(separator);
        for (String item : items) {
            addFileToClassPath(item);
        }
    }

    private static void addFileToClassPath(String fileName) throws Exception {
        addUrlToClasspath(new File(fileName).toURI().toURL());
    }

    public static void addUrlToClasspath(URL u) throws Exception {
        URLClassLoader sysloader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class<URLClassLoader> sysclass = URLClassLoader.class;
        Method method = sysclass.getDeclaredMethod("addURL", new Class[]{URL.class});
        method.setAccessible(true);
        method.invoke(sysloader, u);
    }

    public static Properties loadProperties(File file) {
        Properties properties = new Properties();
        FileInputStream propertiesIn = null;
        try {
            propertiesIn = new FileInputStream(file);
            properties.load(propertiesIn);
        } catch (IOException e) {
            // ignore?  the Properties object will be empty
        } finally {
            IOUtils.closeQuietly(propertiesIn);
        }
        return properties;
    }
}
