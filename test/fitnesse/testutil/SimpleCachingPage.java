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

    public SimpleCachingPage(String name, WikiPage parent, Injector injector) {
        super(name, parent, injector);
    }

    public boolean hasChildPage(String pageName) {
        return hasCachedSubpage(pageName);
    }

    protected WikiPage createChildPage(String name) {
        return new SimpleCachingPage(name, this, getInjector());
    }

    protected void loadChildren() {
    }

    protected PageData makePageData() {
        if (data == null)
            return new PageData(this, "some content");
        else
            return new PageData(data);
    }

    protected VersionInfo makeVersion() {
        return new VersionInfo("abc", "Jon", ClockUtil.currentDate());
    }

    protected void doCommit(PageData data) {
        this.data = data;
    }

    public PageData getDataVersion(String versionName) {
        return new PageData(this, "content from version " + versionName);
    }
}
