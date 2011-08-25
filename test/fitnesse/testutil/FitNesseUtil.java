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
    private static FitNesse instance = null;
    public static final int port = 1999;
    public static FitNesseContext context;
    public static final String URL = "http://localhost:" + port + "/";

    public static void startFitnesse(WikiPage root) throws Exception {
        context = new FitNesseContext(root);
        context.port = port;
        startFitnesseWithContext(context);
    }

    public static void startFitnesseWithContext(FitNesseContext context) {
        instance = new FitNesse(context);
        instance.start();
    }

    public static void stopFitnesse() throws Exception {
        instance.stop();
        FileUtil.deleteFileSystemDirectory("TestDir");
    }

    public static void bindVirtualLinkToPage(WikiPage host, WikiPage proxy) throws Exception {
        VirtualCouplingPage coupling = new VirtualCouplingPage(host, proxy);
        ((VirtualCouplingExtension) host.getExtension(VirtualCouplingExtension.NAME)).setVirtualCoupling(coupling);
    }

    public static void destroyTestContext() {
        FileUtil.deleteFileSystemDirectory("TestDir");
    }
}
