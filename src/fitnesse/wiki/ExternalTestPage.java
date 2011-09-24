package fitnesse.wiki;

import com.google.inject.Injector;
import util.FileSystem;

import java.io.IOException;

public class ExternalTestPage extends CachingPage {
    private static final long serialVersionUID = 1L;
    private FileSystem fileSystem;
    private String path;

    public ExternalTestPage(String path, String name, WikiPage parent, FileSystem fileSystem, Injector injector) {
        super(name, parent, injector);
        this.path = path;
        this.fileSystem = fileSystem;
    }

    @Override
    protected VersionInfo makeVersion() {
        return null;
    }

    @Override
    protected void doCommit(PageData data) {
    }

    @Override
    public boolean hasChildPage(String pageName) {
        return false;
    }

    @Override
    protected WikiPage createChildPage(String name) {
        return null;
    }

    @Override
    protected void loadChildren() {

    }

    @Override
    protected PageData makePageData() throws IOException {
        PageData pageData = new PageData(this);
        String content = fileSystem.getContent(path);
        pageData.setContent("!-" + content + "-!");
        pageData.removeAttribute(PageData.PropertyEDIT);
        pageData.removeAttribute(PageData.PropertyPROPERTIES);
        pageData.removeAttribute(PageData.PropertyVERSIONS);
        pageData.removeAttribute(PageData.PropertyREFACTOR);
        if (content.contains("<table")) {
            pageData.setAttribute(PageType.TEST.toString(), Boolean.toString(true));
        }
        return pageData;
    }

    public PageData getDataVersion(String versionName) {
        return null;
    }
}
