// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import com.google.inject.Injector;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;


public interface WikiPage extends Serializable, Comparable<Object> {
    public WikiPage getParent();

    public WikiPage getParentForVariables() throws IOException;

    public void setParentForVariables(WikiPage parent);

    public WikiPage addChildPage(String name) throws IOException;

    public boolean hasChildPage(String name) throws IOException;

    public WikiPage getChildPage(String name) throws IOException;

    public void removeChildPage(String name);

    public List<WikiPage> getChildren() throws IOException;

    public String getName();

    public PageData getData() throws IOException;

    public PageData getDataVersion(String versionName) throws IOException;

    public VersionInfo commit(PageData data) throws IOException;

    public PageCrawler getPageCrawler();

    public WikiPage getHeaderPage() throws IOException;

    public WikiPage getFooterPage() throws IOException;

    public String getHelpText() throws IOException;

    public List<WikiPageAction> getActions() throws IOException;

    Injector getInjector();

    boolean isRemote();

    String getPageName();

    String getLocalPageName();
}



