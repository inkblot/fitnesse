// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Injector;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

public class SymbolicPage extends BaseWikiPage {
    private static final long serialVersionUID = 1L;

    public static final String PROPERTY_NAME = "SymbolicLinks";

    private WikiPage realPage;

    public SymbolicPage(String name, WikiPage realPage, WikiPage parent, Injector injector) {
        super(name, parent, injector);
        this.realPage = realPage;
    }

    public WikiPage getRealPage() {
        return realPage;
    }

    public WikiPage addChildPage(String name) throws IOException  {
        return realPage.addChildPage(name);
    }

    public boolean hasChildPage(String name) throws IOException {
        return realPage.hasChildPage(name);
    }

    protected WikiPage getNormalChildPage(String name) throws IOException {
        WikiPage childPage = realPage.getChildPage(name);
        if (childPage != null && !(childPage instanceof SymbolicPage))
            childPage = new SymbolicPage(name, childPage, this, getInjector());
        return childPage;
    }

    @Override
    protected WikiPage createInternalSymbolicPage(String linkPath, String linkName) throws IOException {
        WikiPagePath path = PathParser.parse(linkPath);
        WikiPage start = (path.isRelativePath()) ? getRealPage().getParent() : getRealPage();
        WikiPage page = getPageCrawler().getPage(start, path);
        if (page != null)
            page = new SymbolicPage(linkName, page, this, getInjector());
        return page;
    }

    public void removeChildPage(String name) {
        realPage.removeChildPage(name);
    }

    public List<WikiPage> getNormalChildren() throws IOException {
        List<WikiPage> children = realPage.getChildren();
        List<WikiPage> symChildren = new LinkedList<WikiPage>();
        //...Intentionally exclude symbolic links on symbolic pages
        //   to prevent infinite cyclic symbolic references.
        //TODO: -AcD- we need a better cyclic infinite recursion algorithm here.
        for (WikiPage child : children) {
            symChildren.add(new SymbolicPage(child.getName(), child, this, getInjector()));
        }
        return symChildren;
    }

    public PageData getData() throws IOException {
        PageData data = realPage.getData();
        data.setWikiPage(this);
        return data;
    }

    public PageData getDataVersion(String versionName) throws IOException {
        PageData data = realPage.getDataVersion(versionName);
        data.setWikiPage(this);
        return data;
    }

    public VersionInfo commit(PageData data) throws IOException {
        return realPage.commit(data);
    }

}
