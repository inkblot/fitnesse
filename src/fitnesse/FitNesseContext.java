// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.Logger;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.responders.run.SocketDealer;
import fitnesse.wiki.WikiPage;

import java.io.File;

public class FitNesseContext {
    public static final String DEFAULT_PATH = ".";
    public static final String DEFAULT_ROOT = "FitNesseRoot";
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_COMMAND_PORT = 9123;
    public static final int DEFAULT_VERSION_DAYS = 14;

    public FitNesse fitnesse;
    public int port = DEFAULT_PORT;
    public final String rootPath;
    public String rootDirectoryName = DEFAULT_ROOT;
    public String rootPagePath = "";
    public String defaultNewPageContent = "!contents -R2 -g -p -f -h";
    public WikiPage root;
    public ResponderFactory responderFactory = new ResponderFactory(rootPagePath);
    public Logger logger;
    public SocketDealer socketDealer = new SocketDealer();
    public RunningTestingTracker runningTestingTracker = new RunningTestingTracker();
    public Authenticator authenticator = new PromiscuousAuthenticator();
    public HtmlPageFactory htmlPageFactory = new HtmlPageFactory();
    public static String recentChangesDateFormat = "kk:mm:ss EEE, MMM dd, yyyy";
    public static String rfcCompliantDateFormat = "EEE, d MMM yyyy HH:mm:ss Z";
    public static FitNesseContext globalContext;
    public String testResultsDirectoryName = "testResults";
    public boolean doNotChunk;

    public FitNesseContext() {
        this(null);
    }

    public FitNesseContext(WikiPage root) {
        this(root, DEFAULT_PATH);
    }

    public FitNesseContext(WikiPage root, String rootPath) {
        this.root = root;
        this.rootPath = rootPath;
    }


    public String toString() {
        String endl = System.getProperty("line.separator");
        StringBuilder buffer = new StringBuilder();
        buffer.append("\t").append("port:              ").append(port).append(endl);
        buffer.append("\t").append("root page:         ").append(root).append(endl);
        buffer.append("\t").append("logger:            ").append(logger == null ? "none" : logger.toString()).append(endl);
        buffer.append("\t").append("authenticator:     ").append(authenticator).append(endl);
        buffer.append("\t").append("html page factory: ").append(htmlPageFactory).append(endl);

        return buffer.toString();
    }

    public static int getPort() {
        return globalContext != null ? globalContext.port : -1;
    }


    public File getTestHistoryDirectory() {
        return new File(String.format("%s/files/%s", rootPagePath, testResultsDirectoryName));
    }

    public void setRootPagePath() {
        rootPagePath = rootPath + "/" + rootDirectoryName;
    }

}
