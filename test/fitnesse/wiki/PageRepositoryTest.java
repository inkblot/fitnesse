package fitnesse.wiki;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Module;
import fitnesse.FitnesseBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import util.FileSystem;
import util.MemoryFileSystem;

import java.util.Properties;

import static org.junit.Assert.assertEquals;

public class PageRepositoryTest extends FitnesseBaseTestCase {
    private PageRepository pageRepository;
    private FileSystemPage rootPage;

    private FileSystem fileSystem;
    private WikiPageFactory wikiPageFactory;

    @Inject
    public void inject(FileSystem fileSystem, WikiPageFactory wikiPageFactory) {
        this.fileSystem = fileSystem;
        this.wikiPageFactory = wikiPageFactory;
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
    protected Properties getFitNesseProperties() {
        Properties properties = super.getFitNesseProperties();
        properties.remove(WikiPageFactory.WIKI_PAGE_CLASS);
        return properties;
    }

    @Before
    public void SetUp() throws Exception {
        pageRepository = new PageRepository(fileSystem);
        rootPage = (FileSystemPage) wikiPageFactory.makeRootPage(".", "somepath");
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("ExternalSuite", rootPage);
        assertEquals(ExternalSuitePage.class, page.getClass());
    }

    @Test
    public void DirectoryOfDirectoryOfHtmlFilesIsExternalSuitePage() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/subsuite/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("ExternalSuite", rootPage);
        assertEquals(ExternalSuitePage.class, page.getClass());
    }

    @Test
    public void DirectoryWithoutHtmlFilesIsFileSystemPage() throws Exception {
        fileSystem.makeFile("./somepath/WikiPage/myfile.txt", "stuff");
        fileSystem.makeFile("./somepath/OtherPage/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("WikiPage", rootPage);
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void DirectoryWithContentIsFileSystemPage() throws Exception {
        fileSystem.makeFile("./somepath/WikiPage/content.txt", "stuff");
        fileSystem.makeFile("./somepath/WikiPage/subsuite/myfile.html", "stuff");
        WikiPage page = pageRepository.makeChildPage("WikiPage", rootPage);
        assertEquals(FileSystemPage.class, page.getClass());
    }

    @Test
    public void HtmlFileIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/myfile.html", "stuff");
        ExternalSuitePage page = (ExternalSuitePage) pageRepository.makeChildPage("ExternalSuite", rootPage);
        WikiPage child = pageRepository.findChildren(page).get(0);
        assertEquals(ExternalTestPage.class, child.getClass());
        assertEquals("MyfilE", child.getName());
    }

    @Test
    public void DirectoryOfHtmlFilesIsExternalSuitePageChild() throws Exception {
        fileSystem.makeFile("./somepath/ExternalSuite/subsuite/myfile.html", "stuff");
        ExternalSuitePage page = (ExternalSuitePage) pageRepository.makeChildPage("ExternalSuite", rootPage);
        WikiPage child = pageRepository.findChildren(page).get(0);
        assertEquals(ExternalSuitePage.class, child.getClass());
        assertEquals("SubsuitE", child.getName());
    }
}
