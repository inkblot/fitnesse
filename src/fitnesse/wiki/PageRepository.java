package fitnesse.wiki;

import fitnesse.wikitext.WikiWordUtil;
import util.FileSystem;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PageRepository {
    private final FileSystem fileSystem;

    public PageRepository(FileSystem fileSystem) {
        this.fileSystem = fileSystem;
    }

    public WikiPage makeChildPage(String name, FileSystemPage parent) throws IOException {
        String path = parent.getFileSystemPath() + "/" + name;
        if (hasContentChild(path)) {
            return new FileSystemPage(name, parent, fileSystem, parent.getClock(), parent.getInjector());
        } else if (hasHtmlChild(path)) {
            return new ExternalSuitePage(path, name, parent, fileSystem, parent.getInjector());
        } else {
            return new FileSystemPage(name, parent, fileSystem, parent.getClock(), parent.getInjector());
        }
    }

    private Boolean hasContentChild(String path) {
        for (String child : fileSystem.list(path)) {
            if (child.equals("content.txt")) return true;
        }
        return false;
    }

    private Boolean hasHtmlChild(String path) {
        if (path.endsWith(".html")) return true;
        for (String child : fileSystem.list(path)) {
            if (hasHtmlChild(path + "/" + child)) return true;
        }
        return false;
    }

    public List<WikiPage> findChildren(ExternalSuitePage parent) {
        List<WikiPage> children = new ArrayList<WikiPage>();
        for (String child : fileSystem.list(parent.getFileSystemPath())) {
            String childPath = parent.getFileSystemPath() + "/" + child;
            if (child.endsWith(".html")) {
                children.add(new ExternalTestPage(childPath,
                        WikiWordUtil.makeWikiWord(child.replace(".html", "")), parent, fileSystem, parent.getInjector()));
            } else if (hasHtmlChild(childPath)) {
                children.add(new ExternalSuitePage(childPath,
                        WikiWordUtil.makeWikiWord(child), parent, fileSystem, parent.getInjector()));
            }
        }
        return children;
    }
}
