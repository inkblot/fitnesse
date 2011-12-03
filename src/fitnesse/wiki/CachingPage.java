// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.

package fitnesse.wiki;

import com.google.inject.Injector;
import util.TimeMeasurement;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CachingPage extends CommitingPage {
    private static final long serialVersionUID = 1L;

    public static int cacheTime = 3000;

    protected Map<String, WikiPage> children = new HashMap<String, WikiPage>();
    private transient SoftReference<PageData> cachedData;
    private transient TimeMeasurement cachedTime;

    public CachingPage(String name, WikiPage parent, Injector injector) {
        super(name, parent, injector);
    }

    public abstract boolean hasChildPage(String pageName) throws IOException;

    protected abstract WikiPage createChildPage(String name) throws IOException;

    protected abstract void loadChildren() throws IOException;

    protected abstract PageData makePageData() throws IOException;

    public WikiPage addChildPage(String name) throws IOException {
        WikiPage page = createChildPage(name);
        children.put(name, page);
        return page;
    }

    @Override
    public List<WikiPage> getNormalChildren() throws IOException {
        loadChildren();
        return getCachedChildren();
    }

    public List<WikiPage> getCachedChildren() {
        return new ArrayList<WikiPage>(children.values());
    }

    public void removeChildPage(String name) {
        if (hasCachedSubpage(name))
            children.remove(name);
    }

    @Override
    public WikiPage getNormalChildPage(String name) throws IOException {
        if (hasCachedSubpage(name) || hasChildPage(name))
            return children.get(name);
        else
            return null;
    }

    protected boolean hasCachedSubpage(String name) {
        return children.containsKey(name);
    }

    public PageData getData() throws IOException {
        if (cachedDataExpired()) {
            PageData data = makePageData();
            setCachedData(data);
        }
        return new PageData(getCachedData());
    }

    private boolean cachedDataExpired() {
        return getCachedData() == null || cachedTime.elapsed() >= cacheTime;
    }

    public void dumpExpiredCachedData() {
        if (cachedDataExpired())
            clearCache();
    }

    @Override
    public VersionInfo commit(PageData data) throws IOException {
        VersionInfo versionInfo = super.commit(data);
        setCachedData(makePageData());
        return versionInfo;
    }

    private void setCachedData(PageData data) {
        if (cachedData != null)
            cachedData.clear();
        cachedData = new SoftReference<PageData>(data);
        cachedTime = new TimeMeasurement().start();
    }

    public PageData getCachedData() {
        if (cachedData != null)
            return cachedData.get();
        else
            return null;
    }

    public void clearCache() {
        cachedData.clear();
        cachedData = null;
    }
}
