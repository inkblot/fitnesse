package fitnesse.wiki;

import org.junit.Test;
import util.FileSystem;
import util.MemoryFileSystem;

import static org.junit.Assert.assertEquals;

public class ExternalSuitePageTest extends WikiBaseTestCase {
    @Test
    public void ContentIsTableOfContents() throws Exception {
        assertEquals("!contents", new ExternalSuitePage("somewhere", "MyTest", null, null, injector).getData().getContent());
    }

    @Test
    public void ChildrenAreLoaded() throws Exception {
        FileSystem fileSystem = new MemoryFileSystem();
        fileSystem.makeFile("somewhere/MyTest/myfile.html", "stuff");
        assertEquals(1, new ExternalSuitePage("somewhere/MyTest", "MyTest", null, fileSystem, injector).getChildren().size());
    }
}
