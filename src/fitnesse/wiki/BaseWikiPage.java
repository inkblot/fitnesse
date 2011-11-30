// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Injector;
import util.EnvironmentVariableTool;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseWikiPage implements WikiPage {
    private static final long serialVersionUID = 1L;

    protected final String name;
    protected WikiPage parent;
    protected WikiPage parentForVariables;
    protected transient Injector injector;

    protected BaseWikiPage(String name, WikiPage parent, Injector injector) {
        this.name = name;
        this.injector = injector;
        this.parent = this.parentForVariables = parent;
    }

    public String getName() {
        return name;
    }

    public PageCrawler getPageCrawler() {
        return new PageCrawlerImpl();
    }

    public WikiPage getParent() {
        return parent == null ? this : parent;
    }

    public void setParentForVariables(WikiPage parent) {
        parentForVariables = parent;
    }

    public WikiPage getParentForVariables() {
        return parentForVariables == null ? this : parentForVariables;
    }

    protected abstract List<WikiPage> getNormalChildren() throws IOException;

    public List<WikiPage> getChildren() throws IOException {
        List<WikiPage> children = getNormalChildren();
        WikiPageProperties props = getData().getProperties();
        WikiPageProperty symLinksProperty = props.getProperty(SymbolicPage.PROPERTY_NAME);
        if (symLinksProperty != null) {
            for (String linkName : symLinksProperty.keySet()) {
                WikiPage page = createSymbolicPage(symLinksProperty, linkName);
                if (page != null && !children.contains(page))
                    children.add(page);
            }
        }
        return children;
    }

    private WikiPage createSymbolicPage(WikiPageProperty symLinkProperty, String linkName) throws IOException {
        if (symLinkProperty == null)
            return null;
        String linkPath = symLinkProperty.get(linkName);
        if (linkPath == null)
            return null;
        if (linkPath.startsWith("file://"))
            return createExternalSymbolicLink(linkPath, linkName);
        else
            return createInternalSymbolicPage(linkPath, linkName);
    }

    private WikiPage createExternalSymbolicLink(String linkPath, String linkName) throws IOException {
        String fullPagePath = EnvironmentVariableTool.replace(linkPath.substring(7));
        File file = new File(fullPagePath);
        File parentDirectory = file.getParentFile();
        if (parentDirectory.exists()) {
            if (!file.exists())
                FileUtil.makeDir(file.getPath());
            if (file.isDirectory()) {
                WikiPage externalRoot = FileSystemPage.makeRoot(getInjector(), parentDirectory.getPath(), file.getName());
                return new SymbolicPage(linkName, externalRoot, this, getInjector());
            }
        }
        return null;
    }

    protected WikiPage createInternalSymbolicPage(String linkPath, String linkName) throws IOException {
        WikiPagePath path = PathParser.parse(linkPath);
        WikiPage start = (path.isRelativePath()) ? getParent() : this;  //TODO -AcD- a better way?
        WikiPage page = getPageCrawler().getPage(start, path);
        if (page != null)
            page = new SymbolicPage(linkName, page, this, getInjector());
        return page;
    }

    protected abstract WikiPage getNormalChildPage(String name) throws IOException;

    public WikiPage getChildPage(String name) throws IOException {
        WikiPage page = getNormalChildPage(name);
        if (page == null)
            page = createSymbolicPage(getData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME), name);
        return page;
    }

    public WikiPage getHeaderPage() throws IOException {
        return PageCrawlerImpl.getClosestInheritedPage("PageHeader", this);
    }

    public WikiPage getFooterPage() throws IOException {
        return PageCrawlerImpl.getClosestInheritedPage("PageFooter", this);
    }

    public List<WikiPageAction> getActions() throws IOException {
        String localPageName = getLocalPageName();
        String localOrRemotePageName = getPageName();
        PageData pageData = getData();
        List<WikiPageAction> actions = new ArrayList<WikiPageAction>();
        addActionForAttribute("Test", pageData, localPageName, isRemote(), null, null, actions);
        addActionForAttribute("Suite", pageData, localPageName, isRemote(), "", null, actions);
        addActionForAttribute("Edit", pageData, localOrRemotePageName, isRemote(), null, null, actions);
        addActionForAttribute("Properties", pageData, localOrRemotePageName, isRemote(), null, null, actions);
        addActionForAttribute("Refactor", pageData, localOrRemotePageName, isRemote(), null, null, actions);
        addActionForAttribute("Where Used", pageData, localOrRemotePageName, isRemote(), null, "whereUsed", actions);
        addActionForAttribute("Search", pageData, "", isRemote(), null, "searchForm", actions);
        addActionForAttribute("Files", pageData, "/files", isRemote(), null, "", actions);
        addActionForAttribute("Versions", pageData, localOrRemotePageName, isRemote(), null, null, actions);
        addActionForAttribute("Recent Changes", pageData, "/RecentChanges", isRemote(), "", "", actions);
        addAction("User Guide", ".FitNesse.UserGuide", isRemote(), "", "", actions);
        addAction("Test History", "?testHistory", isRemote(), "", "", actions);
        return actions;
    }

    @Override
    public String getPageName() {
        return getLocalPageName();
    }

    @Override
    public String getLocalPageName() {
        return PathParser.render(getPageCrawler().getFullPath(this));
    }

    private void addActionForAttribute(String attribute, PageData pageData, String pageName, boolean newWindowIfRemote,
                                       String shortcutKey, String query, List<WikiPageAction> actions) {
        if (pageData.hasAttribute(attribute.replaceAll("\\s", ""))) {
            addAction(attribute, pageName, newWindowIfRemote, shortcutKey, query, actions);
        }
    }

    private void addAction(String linkName, String pageName, boolean newWindowIfRemote, String shortcutKey, String query, List<WikiPageAction> actions) {
        WikiPageAction link = new WikiPageAction(pageName, linkName);
        link.setNewWindow(newWindowIfRemote);
        if (shortcutKey != null)
            link.setShortcutKey(shortcutKey);
        if (query != null)
            link.setQuery(query);
        actions.add(link);
    }

    public String toString() {
        return this.getClass().getName() + ": " + name;
    }

    public int compareTo(Object o) {
        try {
            return getName().compareTo(((WikiPage) o).getName());
        } catch (Exception e) {
            return 0;
        }
    }

    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof WikiPage))
            return false;
        try {
            PageCrawler crawler = getPageCrawler();
            return crawler.getFullPath(this).equals(crawler.getFullPath(((WikiPage) o)));
        } catch (Exception e) {
            return false;
        }
    }

    public int hashCode() {
        try {
            return getPageCrawler().getFullPath(this).hashCode();
        } catch (Exception e) {
            return 0;
        }
    }

    public String getHelpText() throws IOException {
        String helpText = getData().getAttribute(PageData.PropertyHELP);
        return ((helpText == null) || (helpText.length() == 0)) ? null : helpText;
    }

    public Injector getInjector() {
        return injector;
    }

    @Override
    public boolean isRemote() {
        return false;
    }
}
