// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.testutil;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.wiki.VirtualCouplingExtension;
import fitnesse.wiki.VirtualCouplingPage;
import fitnesse.wiki.WikiPage;
import util.FileUtil;

public class FitNesseUtil {
    public static final int DEFAULT_PORT = 1999;
    public static final String URL = "http://localhost:" + FitNesseUtil.DEFAULT_PORT + "/";

    private FitNesse instance = null;

    public void startFitnesse(FitNesseContext context) {
        instance = context.injector.getInstance(FitNesse.class);
        instance.start();
    }

    public void stopFitnesse() throws Exception {
        instance.stop();
        destroyTestContext();
    }

    public static void bindVirtualLinkToPage(WikiPage host, WikiPage proxy) throws Exception {
        VirtualCouplingPage coupling = new VirtualCouplingPage(host, proxy);
        ((VirtualCouplingExtension) host.getExtension(VirtualCouplingExtension.NAME)).setVirtualCoupling(coupling);
    }

    public static void destroyTestContext() {
        FileUtil.deleteFileSystemDirectory("TestDir");
    }
}
