// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.authentication.Authenticator;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.responders.run.SocketDealer;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Clock;

import java.io.File;

@Singleton
public class FitNesseContext {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseContext.class);

    public static final String DEFAULT_PATH = ".";
    public static final String DEFAULT_ROOT = "FitNesseRoot";
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_COMMAND_PORT = 9123;
    public static final int DEFAULT_VERSION_DAYS = 14;
    public static final String RECENT_CHANGES_DATE_FORMAT = "kk:mm:ss EEE, MMM dd, yyyy";
    public static final String RFC_COMPLIANT_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";
    public static final String ROOT_PATH = "fitnesse.rootPath";
    public static final String ROOT_PAGE_NAME = "fitnesse.rootPageName";
    public static final String ROOT_PAGE_PATH = "fitnesse.rootPagePath";
    public static final String PROPERTIES_FILE = "plugins.properties";

    public static FitNesseContext globalContext;

    public final String rootPath;
    public final WikiPage root;
    public final String rootPagePath;
    private final ResponderFactory responderFactory;
    private final Clock clock;
    private final WikiPageFactory wikiPageFactory;
    private final HtmlPageFactory htmlPageFactory;
    public final Provider<Authenticator> authenticatorProvider;

    public FitNesse fitnesse;
    public int port = DEFAULT_PORT;
    public SocketDealer socketDealer = new SocketDealer();
    public RunningTestingTracker runningTestingTracker = new RunningTestingTracker();
    public String testResultsDirectoryName = "testResults";
    public boolean doNotChunk;

    public static FitNesseContext makeContext(Injector injector, String rootPath, String rootPageName) throws Exception {
        Injector contextInjector = injector.createChildInjector(new FitNesseContextModule(rootPath, rootPageName));
        return contextInjector.getInstance(FitNesseContext.class);
    }

    @Inject
    public FitNesseContext(
            @Named(ROOT_PATH) String rootPath,
            @Named(ROOT_PAGE_PATH) String rootPagePath,
            WikiPageFactory wikiPageFactory,
            ResponderFactory responderFactory,
            Clock clock,
            HtmlPageFactory htmlPageFactory,
            Provider<Authenticator> authenticatorProvider) throws Exception {
        this.rootPath = rootPath;
        this.rootPagePath = rootPagePath;
        this.wikiPageFactory = wikiPageFactory;
        this.responderFactory = responderFactory;
        this.clock = clock;
        this.htmlPageFactory = htmlPageFactory;
        this.authenticatorProvider = authenticatorProvider;

        this.root = wikiPageFactory.makeRootPage();
        String absolutePath = new File(this.rootPath).getAbsolutePath();
        if (!absolutePath.equals(this.rootPath)) {
            logger.warn("rootPath is not absolute: rootPath=" + this.rootPath + " absolutePath=" + absolutePath, new RuntimeException());
        }
    }


    public String toString() {
        String endl = System.getProperty("line.separator");
        StringBuilder buffer = new StringBuilder();
        buffer.append("\t").append("port:              ").append(port).append(endl);
        buffer.append("\t").append("root page:         ").append(root).append(endl);
        buffer.append("\t").append("authenticator:     ").append(authenticatorProvider.get()).append(endl);
        buffer.append("\t").append("html page factory: ").append(htmlPageFactory).append(endl);

        return buffer.toString();
    }

    public static int getPort() {
        return globalContext != null ? globalContext.port : -1;
    }

    public File getTestHistoryDirectory() {
        return new File(String.format("%s/files/%s", rootPagePath, testResultsDirectoryName));
    }

    public ResponderFactory getResponderFactory() {
        return responderFactory;
    }

    public WikiPageFactory getWikiPageFactory() {
        return wikiPageFactory;
    }

    public Clock getClock() {
        return clock;
    }

    public HtmlPageFactory getHtmlPageFactory() {
        return htmlPageFactory;
    }
}
