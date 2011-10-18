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

    private final Injector injector;
    public final String rootPath;
    public final WikiPage root;
    private final String rootPagePath;
    public final int port;
    private final ResponderFactory responderFactory;
    private final WikiPageFactory wikiPageFactory;
    private final HtmlPageFactory htmlPageFactory;
    public final Provider<Authenticator> authenticatorProvider;

    public SocketDealer socketDealer = new SocketDealer();
    public RunningTestingTracker runningTestingTracker = new RunningTestingTracker();
    public String testResultsDirectoryName = "testResults";
    public boolean doNotChunk;

    @Inject
    public FitNesseContext(
            @Named(FitNesseContextModule.ROOT_PATH) String rootPath,
            @Named(FitNesseContextModule.ROOT_PAGE_PATH) String rootPagePath,
            @Named(FitNesseContextModule.PORT) Integer port,
            WikiPageFactory wikiPageFactory,
            ResponderFactory responderFactory,
            HtmlPageFactory htmlPageFactory,
            Provider<Authenticator> authenticatorProvider,
            Injector injector,
            @Named(FitNesseContextModule.ROOT_PAGE) WikiPage root) throws Exception {
        this.rootPath = rootPath;
        this.rootPagePath = rootPagePath;
        this.port = port;
        this.wikiPageFactory = wikiPageFactory;
        this.responderFactory = responderFactory;
        this.htmlPageFactory = htmlPageFactory;
        this.authenticatorProvider = authenticatorProvider;
        this.injector = injector;
        this.root = root;

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

    public File getTestHistoryDirectory() {
        return new File(String.format("%s/files/%s", rootPagePath, testResultsDirectoryName));
    }

    public ResponderFactory getResponderFactory() {
        return responderFactory;
    }

    public WikiPageFactory getWikiPageFactory() {
        return wikiPageFactory;
    }

    public HtmlPageFactory getHtmlPageFactory() {
        return htmlPageFactory;
    }

    public Injector getInjector() {
        return injector;
    }
}
