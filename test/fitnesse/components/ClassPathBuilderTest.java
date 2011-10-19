// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNeseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class ClassPathBuilderTest extends FitnesseBaseTestCase {
    private WikiPage root;
    private ClassPathBuilder builder;
    String pathSeparator = System.getProperty("path.separator");
    private PageCrawler crawler;
    private WikiPagePath somePagePath;
    private static final String TEST_DIR = "testDir";

    @Inject
    public void inject(@Named(FitNeseModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        builder = new ClassPathBuilder();
        somePagePath = PathParser.parse("SomePage");
    }

    @Test
    public void testGetClasspath() throws Exception {
        crawler.addPage(root, PathParser.parse("TestPage"),
                "!path fitnesse.jar\n" +
                        "!path my.jar");
        String expected = "fitnesse.jar" + pathSeparator + "my.jar";
        assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
    }

    @Test
    public void testPathSeparatorVariable() throws Exception {
        WikiPage page = crawler.addPage(root, PathParser.parse("TestPage"),
                "!path fitnesse.jar\n" +
                        "!path my.jar");
        PageData data = page.getData();
        data.addVariable("PATH_SEPARATOR", "|");
        page.commit(data);

        String expected = "fitnesse.jar" + "|" + "my.jar";
        assertEquals(expected, builder.getClasspath(root.getChildPage("TestPage")));
    }

    @Test
    public void testGetPaths_OneLevel() throws Exception {
        String pageContent = "This is some content\n" +
                "!path aPath\n" +
                "end of conent\n";
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage page = crawler.addPage(root, PathParser.parse("ClassPath"), pageContent);
        String path = builder.getClasspath(page);
        assertEquals("aPath", path);
    }

    @Test
    public void testGetClassPathMultiLevel() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        crawler.addPage(root, PathParser.parse("ProjectOne"),
                "!path path2\n" +
                        "!path path 3");
        crawler.addPage(root, PathParser.parse("ProjectOne.TesT"), "!path path1");

        String cp = builder.getClasspath(crawler.getPage(root, PathParser.parse("ProjectOne.TesT")));
        assertSubString("path1", cp);
        assertSubString("path2", cp);
        assertSubString("\"path 3\"", cp);
    }

    @Test
    public void testLinearClassPath() throws Exception {
        WikiPage root = InMemoryPage.makeRoot("RooT", injector);
        WikiPage superPage = crawler.addPage(root, PathParser.parse("SuperPage"), "!path superPagePath");
        WikiPage subPage = crawler.addPage(superPage, PathParser.parse("SubPage"), "!path subPagePath");
        String cp = builder.getClasspath(subPage);
        assertEquals("subPagePath" + pathSeparator + "superPagePath", cp);

    }

    @Test
    public void testGetClassPathFromPageThatDoesntExist() throws Exception {
        String classPath = makeClassPathFromSimpleStructure("somePath");

        assertEquals("somePath", classPath);
    }

    private String makeClassPathFromSimpleStructure(String path) throws Exception {
        PageData data = root.getData();
        data.setContent("!path " + path);
        root.commit(data);
        crawler = root.getPageCrawler();
        crawler.setDeadEndStrategy(new MockingPageCrawler());
        WikiPage page = crawler.getPage(root, somePagePath);
        return builder.getClasspath(page);
    }

    @Test
    public void testThatPathsWithSpacesGetQuoted() throws Exception {
        crawler.addPage(root, somePagePath, "!path Some File.jar");
        crawler = root.getPageCrawler();
        crawler.setDeadEndStrategy(new MockingPageCrawler());
        WikiPage page = crawler.getPage(root, somePagePath);

        assertEquals("\"Some File.jar\"", builder.getClasspath(page));

        crawler.addPage(root, somePagePath, "!path somefile.jar\n!path Some Dir/someFile.jar");
        assertEquals("somefile.jar" + pathSeparator + "\"Some Dir/someFile.jar\"", builder.getClasspath(page));
    }

    @Test
    public void testWildCardExpansion() throws Exception {
        try {
            makeSampleFilesX();

            String classPath = makeClassPathFromSimpleStructure("testDir/*.jar");
            assertHasRegexp("one\\.jar", classPath);
            assertHasRegexp("two\\.jar", classPath);

            classPath = makeClassPathFromSimpleStructure("testDir/*.dll");
            assertHasRegexp("one\\.dll", classPath);
            assertHasRegexp("two\\.dll", classPath);

            classPath = makeClassPathFromSimpleStructure("testDir/one*");
            assertHasRegexp("one\\.dll", classPath);
            assertHasRegexp("one\\.jar", classPath);
            assertHasRegexp("oneA", classPath);

            classPath = makeClassPathFromSimpleStructure("testDir/**.jar");
            assertHasRegexp("one\\.jar", classPath);
            assertHasRegexp("two\\.jar", classPath);
            assertHasRegexp("subdir(?:\\\\|/)sub1\\.jar", classPath);
            assertHasRegexp("subdir(?:\\\\|/)sub2\\.jar", classPath);
        } finally {
            deleteSampleFiles();
        }
    }

    public static void makeSampleFilesX() {
        FileUtil.makeDir(TEST_DIR);
        FileUtil.createFile(TEST_DIR + "/one.jar", "");
        FileUtil.createFile(TEST_DIR + "/two.jar", "");
        FileUtil.createFile(TEST_DIR + "/one.dll", "");
        FileUtil.createFile(TEST_DIR + "/two.dll", "");
        FileUtil.createFile(TEST_DIR + "/oneA", "");
        FileUtil.createFile(TEST_DIR + "/twoA", "");
        FileUtil.createDir(TEST_DIR + "/subdir");
        FileUtil.createFile(TEST_DIR + "/subdir/sub1.jar", "");
        FileUtil.createFile(TEST_DIR + "/subdir/sub2.jar", "");
        FileUtil.createFile(TEST_DIR + "/subdir/sub1.dll", "");
        FileUtil.createFile(TEST_DIR + "/subdir/sub2.dll", "");
    }

    public static void deleteSampleFiles() {
        FileUtil.deleteFileSystemDirectory(TEST_DIR);
    }
}
