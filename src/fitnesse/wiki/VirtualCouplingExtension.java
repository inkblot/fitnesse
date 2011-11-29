// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import util.StringUtil;

import java.io.IOException;

public class VirtualCouplingExtension implements Extension {
    private static final long serialVersionUID = 1L;

    public static final String NAME = "VirtualCoupling";

    private WikiPage hostPage;
    protected VirtualCouplingPage virtualCoupling;

    public String getName() {
        return NAME;
    }

    public VirtualCouplingExtension(WikiPage page) {
        hostPage = page;
        resetVirtualCoupling();
    }

    public void setVirtualCoupling(VirtualCouplingPage coupling) {
        virtualCoupling = coupling;
    }

    public void resetVirtualCoupling() {
        virtualCoupling = new NullVirtualCouplingPage(hostPage);
    }

    public WikiPage getVirtualCoupling() throws IOException {
        detectAndLoadVirtualChildren();
        return virtualCoupling;
    }

    private void detectAndLoadVirtualChildren() throws IOException {
        PageData data = hostPage.getData();
        if (data.hasAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE))
            loadVirtualChildren(data.getAttribute(WikiPageProperties.VIRTUAL_WIKI_ATTRIBUTE));
    }

    private void loadVirtualChildren(String url) throws IOException {
        try {
            ProxyPage proxy = ProxyPage.retrievePage(url, hostPage.getInjector());
            virtualCoupling = new VirtualCouplingPage(hostPage, proxy);
        } catch (Exception e) {
            WikiPage page = hostPage.getChildPage("VirtualWikiNetworkError");
            if (page == null)
                page = hostPage.addChildPage("VirtualWikiNetworkError");
            PageData data = page.getData();
            data.setContent("{{{" + StringUtil.makeExceptionString(e) + "}}}");
            page.commit(data);
        }
    }
}
