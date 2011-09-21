// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import com.google.inject.Injector;
import fitnesse.wiki.CachingPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import util.ClockUtil;

public class SimpleCachingPage extends CachingPage {
    private static final long serialVersionUID = 1L;

    private PageData data;

    public SimpleCachingPage(String name, WikiPage parent, Injector injector) throws Exception {
        super(name, parent, injector);
    }

    public boolean hasChildPage(String pageName) throws Exception {
        return hasCachedSubpage(pageName);
    }

    protected WikiPage createChildPage(String name) throws Exception {
        return new SimpleCachingPage(name, this, getInjector());
    }

    protected void loadChildren() throws Exception {
    }

    protected PageData makePageData() throws Exception {
        if (data == null)
            return new PageData(this, "some content");
        else
            return new PageData(data);
    }

    protected VersionInfo makeVersion() throws Exception {
        return new VersionInfo("abc", "Jon", ClockUtil.currentDate());
    }

    protected void doCommit(PageData data) throws Exception {
        this.data = data;
    }

    public PageData getDataVersion(String versionName) throws Exception {
        return new PageData(this, "content from version " + versionName);
    }
}
