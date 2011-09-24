// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Injector;
import fitnesse.http.ResponseParser;
import util.ClockUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.List;

public class ProxyPage extends CachingPage implements Serializable {
    private static final long serialVersionUID = 1L;

    public static int retrievalCount = 0;

    private String host;
    private int hostPort;
    private WikiPagePath realPath;
    public ResponseParser parser;
    private long lastLoadChildrenTime = 0;

    public ProxyPage(WikiPage original, Injector injector) throws IOException {
        super(original.getName(), null, injector);
        realPath = original.getPageCrawler().getFullPath(original);

        List<WikiPage> children = original.getChildren();
        for (WikiPage page : children) {
            ProxyPage child = new ProxyPage(page, injector);
            child.parent = this;
            this.children.put(child.getName(), child);
        }
    }

    protected ProxyPage(String name, WikiPage parent, Injector injector) {
        super(name, parent, injector);
    }

    public ProxyPage(String name, WikiPage parent, String host, int port, WikiPagePath path, Injector injector) {
        super(name, parent, injector);
        this.host = host;
        hostPort = port;
        realPath = path;
    }

    public static ProxyPage retrievePage(String urlString) throws IOException {
        retrievalCount++;
        URL url = new URL(urlString + "?responder=proxy&type=bones");
        ProxyPage page = (ProxyPage) getObjectFromUrl(url);
        page.setTransientValues(url.getHost(), ClockUtil.currentTimeInMillis());
        int port = url.getPort();
        page.setHostPort((port == -1) ? 80 : port);
        page.lastLoadChildrenTime = ClockUtil.currentTimeInMillis();
        return page;
    }

    protected WikiPage createChildPage(String name) throws IOException {
        WikiPagePath childPath = realPath.copy().addNameToEnd(name);
        return new ProxyPage(name, this, host, getHostPort(), childPath, getInjector());
    }

    protected void loadChildren() throws IOException {
        if (cacheTime <= (ClockUtil.currentTimeInMillis() - lastLoadChildrenTime)) {
            ProxyPage page = retrievePage(getThisPageUrl());
            children.clear();
            for (WikiPage wikiPage : page.children.values()) {
                ProxyPage child = (ProxyPage) wikiPage;
                child.parent = this;
                children.put(child.getName(), child);
            }
            lastLoadChildrenTime = ClockUtil.currentTimeInMillis();
        }
    }

    public String getThisPageUrl() {
        StringBuilder url = new StringBuilder("http://");
        url.append(host);
        url.append(":").append(getHostPort());
        url.append("/").append(PathParser.render(realPath));
        return url.toString();
    }

    public boolean hasChildPage(String pageName) throws IOException {
        if (children.containsKey(pageName))
            return true;
        else {
            loadChildren();
            return children.containsKey(pageName);
        }
    }

    public void setTransientValues(String host, long lastLoadTime) {
        this.host = host;
        lastLoadChildrenTime = lastLoadTime;
        for (WikiPage wikiPage : children.values()) {
            ProxyPage page = (ProxyPage) wikiPage;
            page.setTransientValues(host, lastLoadTime);
        }
    }

    public String getHost() {
        return host;
    }

    public void setHostPort(int port) {
        hostPort = port;
        for (WikiPage wikiPage : children.values()) {
            ProxyPage page = (ProxyPage) wikiPage;
            page.setHostPort(port);
        }
    }

    public int getHostPort() {
        return hostPort;
    }

    public PageData getMeat() throws IOException {
        return getMeat(null);
    }

    public PageData getMeat(String versionName) throws IOException {
        StringBuilder urlString = new StringBuilder(getThisPageUrl());
        urlString.append("?responder=proxy&type=meat");
        if (versionName != null)
            urlString.append("&version=").append(versionName);
        URL url = new URL(urlString.toString());
        PageData data = (PageData) getObjectFromUrl(url);
        if (data != null)
            data.setWikiPage(this);
        return data;
    }

    private static Object getObjectFromUrl(URL url) throws IOException {
        Object obj;
        InputStream is = null;
        ObjectInputStream ois = null;
        try {
            is = url.openStream();
            ois = new ObjectInputStream(is);
            obj = ois.readObject();
            return obj;
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            if (is != null)
                is.close();
            if (ois != null)
                ois.close();
        }
    }

    protected PageData makePageData() throws IOException {
        return getMeat();
    }

    public PageData getDataVersion(String versionName) throws IOException {
        PageData data = getMeat(versionName);
        if (data == null)
            throw new NoSuchVersionException("There is no version '" + versionName + "'");
        return data;
    }

    //TODO-MdM these are not needed
    // We expect this to go away when we do the checkout model
    protected VersionInfo makeVersion() {
        return null;
    }

    protected void doCommit(PageData data) {
    }
}
