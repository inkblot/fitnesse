package fitnesse.updates;

import com.google.inject.Key;
import com.google.inject.name.Names;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContextModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.*;
import static util.RegexAssertions.assertSubString;

public class UpdaterImplementationTest extends FitnesseBaseTestCase {
    private File updateList;
    private File updateDoNotCopyOver;

    protected WikiPage root;
    protected Update update;
    protected UpdaterImplementation updater;
    protected WikiPage pageOne;
    protected WikiPage pageTwo;
    protected FitNesseContext context;
    protected PageCrawler crawler;
    private boolean updateDone = false;

    @Override
    protected Properties getFitNesseProperties() {
        Properties properties = super.getFitNesseProperties();
        properties.remove(WikiPageFactory.WIKI_PAGE_CLASS);
        return properties;
    }

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        assertThat(context.root, instanceOf(FileSystemPage.class));
        String rootPagePath = this.context.getInjector().getInstance(Key.get(String.class, Names.named(FitNesseContextModule.ROOT_PAGE_PATH)));
        root = context.root;

        FileUtil.makeDir(getRootPath());
        crawler = root.getPageCrawler();

        FileUtil.createFile("classes/Resources/files/TestFile", "");
        FileUtil.createFile("classes/Resources/files/BestFile", "");
        FileUtil.createFile("classes/Resources/SpecialFile", "");

        updateList = new File("classes/Resources/updateList");
        updateDoNotCopyOver = new File("classes/Resources/updateDoNotCopyOverList");
        FileUtil.createFile(updateList, "files/TestFile\nfiles/BestFile\n");
        FileUtil.createFile(updateDoNotCopyOver, "SpecialFile");

        updater = new UpdaterImplementation(context, rootPagePath, context.rootPath);
    }

    @Test
    public void shouldBeAbleToGetUpdateFilesAndMakeAlistFromThem() throws Exception {
        ArrayList<String> updateArrayList = new ArrayList<String>();
        updater.tryToParseTheFileIntoTheList(updateList, updateArrayList);
        assertEquals("files/TestFile", updateArrayList.get(0));
        assertEquals("files/BestFile", updateArrayList.get(1));
        updateArrayList = new ArrayList<String>();
        updater.tryToParseTheFileIntoTheList(updateDoNotCopyOver, updateArrayList);
        assertEquals("SpecialFile", updateArrayList.get(0));
    }

    @Test
    public void shouldBeAbleToGetThePathOfJustTheParent() throws Exception {
        String filePath = updater.getCorrectPathForTheDestination("classes/files/moreFiles/TestFile");
        assertSubString(portablePath("classes/files/moreFiles"), filePath);
    }

    private String portablePath(String path) {
        return FileUtil.buildPath(path.split("/"));
    }

    @Test
    public void shouldCreateTheCorrectPathForGivenPath() throws Exception {
        String filePath = updater.getCorrectPathFromJar("FitNesseRoot/files/moreFiles/TestFile");
        assertEquals("Resources/FitNesseRoot/files/moreFiles/TestFile", filePath);
    }

    @Test
    public void shouldCreateSomeFilesInTheRooTDirectory() throws Exception {
        for (Update update : updater.updates) {
            if (update.getClass() == ReplacingFileUpdate.class || update.getClass() == FileUpdate.class)
                update.doUpdate();
        }
        File testFile = new File(context.rootPath, "files/TestFile");
        File bestFile = new File(context.rootPath, "files/BestFile");
        File specialFile = new File(context.rootPath, "SpecialFile");
        assertTrue(testFile.exists());
        assertTrue(bestFile.exists());
        assertTrue(specialFile.exists());
        assertFalse(testFile.isDirectory());
        assertFalse(bestFile.isDirectory());
        assertFalse(specialFile.isDirectory());
    }

    @Test
    public void updatesShouldBeRunIfCurrentVersionNotAlreadyUpdated() throws Exception {
        String version = "TestVersion";
        updater.setFitNesseVersion(version);

        File propertiesFile = new File(getRootPath(), "RooT/properties");
        FileUtil.deleteFile(propertiesFile);
        assertFalse(propertiesFile.exists());

        updater.updates = new Update[]{
                new UpdateSpy()
        };
        updater.update();
        assertTrue(updateDone);
        assertTrue(propertiesFile.exists());

        Properties properties = updater.loadProperties();
        assertTrue(properties.containsKey("Version"));
        assertEquals(version, properties.get("Version"));
        FileUtil.deleteFile(propertiesFile);
    }

    @Test
    public void updatesShouldNotBeRunIfCurrentVersionAlreadyUpdated() throws Exception {
        String version = "TestVersion";
        updater.setFitNesseVersion(version);
        Properties properties = updater.getProperties();
        properties.put("Version", version);
        updater.updates = new Update[]{
                new UpdateSpy()
        };
        updater.update();
        assertFalse(updateDone);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowExceptionInNoUpdateFileExists() throws Exception {
        FileUtil.deleteFile(updateList);
        updater.tryToParseTheFileIntoTheList(updateList, new ArrayList<String>());
    }

    @After
    public void tearDown() {
        FileUtil.deleteFileSystemDirectory("classes/Resources");
    }

    private class UpdateSpy implements Update {
        public String getName() {
            return "test";
        }

        public String getMessage() {
            return "test";
        }

        public boolean shouldBeApplied() {
            return true;
        }

        public void doUpdate() {
            updateDone = true;
        }
    }
}
