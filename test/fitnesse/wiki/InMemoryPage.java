// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Injector;
import util.Clock;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPage extends CommitingPage {
    private static final long serialVersionUID = 1L;

    protected static final String currentVersionName = "current_version";

    protected Map<String, PageData> versions = new ConcurrentHashMap<String, PageData>();
    protected Map<String, WikiPage> children = new ConcurrentHashMap<String, WikiPage>();

    private transient final Clock clock;

    public static WikiPage makeRoot(Injector injector, String rootPath, String rootPageName) {
        return new InMemoryPage(rootPageName, injector);
    }

    public InMemoryPage(String rootPageName, Injector injector) {
        this(rootPageName, null, injector);
    }

    protected InMemoryPage(String name, WikiPage parent, Injector injector) {
        super(name, parent, injector);
        clock = injector.getInstance(Clock.class);
        addExtention(new VirtualCouplingExtension(this));
        versions.put(currentVersionName, new PageData(this, ""));
    }

    public WikiPage addChildPage(String name) throws Exception {
        WikiPage page = createChildPage(name);
        children.put(name, page);
        return page;
    }

    public static WikiPage makeRoot(String name, Injector injector) {
        return new InMemoryPage(name, null, injector);
    }

    protected WikiPage createChildPage(String name) throws Exception {
        BaseWikiPage newPage = new InMemoryPage(name, this, getInjector());
        children.put(newPage.getName(), newPage);
        return newPage;
    }

    public void removeChildPage(String name) throws Exception {
        children.remove(name);
    }

    public boolean hasChildPage(String pageName) {
        return children.containsKey(pageName);
    }

    protected VersionInfo makeVersion() throws Exception {
        PageData current = getDataVersion(currentVersionName);

        String name = String.valueOf(VersionInfo.nextId());
        VersionInfo version = makeVersionInfo(current, name);
        versions.put(version.getName(), current);
        return version;
    }

    protected WikiPage getNormalChildPage(String name) throws Exception {
        return children.get(name);
    }

    public List<WikiPage> getNormalChildren() {
        return new LinkedList<WikiPage>(children.values());
    }

    public PageData getData() throws Exception {
        return new PageData(getDataVersion(currentVersionName));
    }

    public void doCommit(PageData newData) throws Exception {
        newData.setWikiPage(this);
        newData.getProperties().setLastModificationTime(clock.currentClockDate());
        versions.put(currentVersionName, newData);
    }

    public PageData getDataVersion(String versionName) throws Exception {
        PageData version = versions.get(versionName);
        if (version == null)
            throw new NoSuchVersionException("There is no version '" + versionName + "'");

        Set<String> names = new HashSet<String>(versions.keySet());
        names.remove(currentVersionName);
        List<VersionInfo> pageVersions = new LinkedList<VersionInfo>();
        for (String name : names) {
            PageData data = versions.get(name);
            pageVersions.add(makeVersionInfo(data, name));
        }
        version.addVersions(pageVersions);
        return new PageData(version);
    }

    protected VersionInfo makeVersionInfo(PageData current, String name) throws Exception {
        String author = current.getAttribute(PageData.LAST_MODIFYING_USER);
        if (author == null)
            author = "";
        Date date = current.getProperties().getLastModificationTime();
        return new VersionInfo(name, author, date);
    }
}
