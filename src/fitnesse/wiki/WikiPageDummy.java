// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import com.google.inject.Injector;
import util.ClockUtil;

import java.util.ArrayList;
import java.util.List;

public class WikiPageDummy implements WikiPage {
    private static final long serialVersionUID = 1L;

    public final String name;
    protected final String location;
    private PageData pageData;
    private WikiPage parent;
    protected WikiPage parentForVariables;
    private final Injector injector;

    public WikiPageDummy(String name, String content, Injector injector) {
        this.name = name;
        this.injector = injector;
        this.pageData = new PageData(this, content);
        this.location = null;
    }

    public WikiPageDummy(String location, Injector injector) {
        this.location = location;
        this.injector = injector;
        this.name = "Default";
        this.pageData = null;
    }

    public WikiPageDummy(Injector injector) {
        this(null, injector);
    }

    public String getName() {
        return name;
    }

    public WikiPage getParent() {
        return parent;
    }

    public void setParentForVariables(WikiPage parent) {
        parentForVariables = parent;
    }

    public WikiPage getParentForVariables() {
        return parentForVariables == null ? this : parentForVariables;
    }

    public void setParent(WikiPage parent) {
        this.parent = this.parentForVariables = parent;
    }

    public PageData getData() {
        return pageData;
    }

    public VersionInfo commit(PageData data) {
        pageData = data;
        return new VersionInfo("mockVersionName", "mockAuthor", ClockUtil.currentDate());
    }

    public List<WikiPage> getChildren() {
        return new ArrayList<WikiPage>();
    }

    public int compareTo(Object o) {
        return 0;
    }

    public PageData getDataVersion(String versionName) {
        return null;
    }

    public void removeChildPage(String name) {
    }

    public PageCrawler getPageCrawler() {
        return new PageCrawlerImpl();
    }

    public WikiPage getHeaderPage() {
        return null;
    }

    public WikiPage getFooterPage() {
        return null;
    }

    public WikiPage addChildPage(String name) {
        return null;
    }

    public boolean hasChildPage(String name) {
        return false;
    }

    public WikiPage getChildPage(String name) {
        return null;
    }

    public boolean hasExtension(String extensionName) {
        return false;
    }

    public Extension getExtension(String extensionName) {
        return null;
    }

    public String getHelpText() {
        return "Dummy help text";
    }

    public List<WikiPageAction> getActions() {
        return null;
    }

    @Override
    public Injector getInjector() {
        return injector;
    }
}
