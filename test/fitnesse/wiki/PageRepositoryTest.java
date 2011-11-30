package fitnesse.wiki;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import org.junit.Before;
import org.junit.Test;
import util.FileSystem;
import util.MemoryFileSystem;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class PageRepositoryTest extends WikiBaseTestCase {
    private PageRepository pageRepository;
    private FileSystemPage rootPage;

    private FileSystem fileSystem;

    @Inject
    public void inject(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    @Override
    protected Module getOverrideModule() {
        return new AbstractModule() {
            @Override
            protected void configure() {
                bind(FileSystem.class).to(MemoryFileSystem.class);
            }
        };
    }

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.remove(WikiModule.WIKI_PAGE_CLASS);
        return properties;
    }

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage rootPage) {
        this.rootPage = (FileSystemPage) rootPage;
    }

    @Before
    public void SetUp() throws Exception {
        pageRepository = new PageRepository(fileSystem);
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
        fileSystem.makeFile(getRootPath() + "/RooT/ExternalSuite/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("ExternalSuite", rootPage);
        assertEquals(ExternalSuitePage.class, page.getClass());
    }

    @Test
    public void DirectoryOfDirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
        fileSystem.makeFile(getRootPath() + "/RooT/ExternalSuite/subsuite/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("ExternalSuite", rootPage);
        assertEquals(ExternalSuitePage.class, page.getClass());
    }

    @Test
    public void DirectoryWithoutHtmlFilesIsFileSystemPage() throws Exception {
        fileSystem.makeFile(getRootPath() + "/RooT/WikiPage/myfile.txt", "stuff");
        fileSystem.makeFile(getRootPath() + "/RooT/OtherPage/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("WikiPage", rootPage);
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void DirectoryWithContentIsFileSystemPage() throws Exception {
        fileSystem.makeFile(getRootPath() + "/RooT/WikiPage/content.txt", "stuff");
        fileSystem.makeFile(getRootPath() + "/RooT/WikiPage/subsuite/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("WikiPage", rootPage);
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void HtmlFileIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile(getRootPath() + "/RooT/ExternalSuite/myfile.html", "stuff");
        ExternalSuitePage page = (ExternalSuitePage) pageRepository.makeChildPage("ExternalSuite", rootPage);
        WikiPage child = pageRepository.findChildren(page).get(0);
        assertEquals(ExternalTestPage.class, child.getClass());
        assertEquals("MyfilE", child.getName());
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile(getRootPath() + "/RooT/ExternalSuite/subsuite/myfile.html", "stuff");
        ExternalSuitePage page = (ExternalSuitePage) pageRepository.makeChildPage("ExternalSuite", rootPage);
        WikiPage child = pageRepository.findChildren(page).get(0);
        assertEquals(ExternalSuitePage.class, child.getClass());
        assertEquals("SubsuitE", child.getName());
    }
}
