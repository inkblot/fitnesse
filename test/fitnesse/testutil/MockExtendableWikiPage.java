// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import com.google.inject.Injector;
import fitnesse.wiki.*;

import java.util.List;

public class MockExtendableWikiPage extends ExtendableWikiPage {
    private static final long serialVersionUID = 1L;

    public MockExtendableWikiPage(Extension e, Injector injector) {
        super("blah", null, injector);
        addExtention(e);
    }

    public WikiPage getParent() {
        return null;
    }

    public WikiPage addChildPage(String name) throws Exception {
        return null;
    }

    public boolean hasChildPage(String name) throws Exception {
        return false;
    }

    public WikiPage getNormalChildPage(String name) throws Exception {
        return null;
    }

    public void removeChildPage(String name) throws Exception {
    }

    public List<WikiPage> getNormalChildren() throws Exception {
        return null;
    }

    public String getName() {
        return null;
    }

    public PageData getData() throws Exception {
        return null;
    }

    public PageData getDataVersion(String versionName) throws Exception {
        return null;
    }

    public VersionInfo commit(PageData data) throws Exception {
        return null;
    }

    public int compareTo(Object o) {
        return 0;
    }
}
