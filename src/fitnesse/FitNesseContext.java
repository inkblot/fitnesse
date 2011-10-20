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
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;

import java.io.File;

@Singleton
public class FitNesseContext {
    public static final String DEFAULT_PATH = ".";
    public static final String DEFAULT_ROOT = "FitNesseRoot";
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_COMMAND_PORT = 9123;
    public static final int DEFAULT_VERSION_DAYS = 14;
    public static final String RECENT_CHANGES_DATE_FORMAT = "kk:mm:ss EEE, MMM dd, yyyy";
    public static final String RFC_COMPLIANT_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

    private final Injector injector;
    private final WikiPage root;
    private final String rootPagePath;
    private final int port;
    private final ResponderFactory responderFactory;
    private final WikiPageFactory wikiPageFactory;
    private final HtmlPageFactory htmlPageFactory;
    private final Provider<Authenticator> authenticatorProvider;

    public RunningTestingTracker runningTestingTracker = new RunningTestingTracker();
    public String testResultsDirectoryName = "testResults";
    public boolean doNotChunk;

    @Inject
    public FitNesseContext(
            @Named(FitNesseModule.ROOT_PAGE_PATH) String rootPagePath,
            @Named(FitNesseModule.PORT) Integer port,
            WikiPageFactory wikiPageFactory,
            ResponderFactory responderFactory,
            HtmlPageFactory htmlPageFactory,
            Provider<Authenticator> authenticatorProvider,
            Injector injector,
            @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.rootPagePath = rootPagePath;
        this.port = port;
        this.wikiPageFactory = wikiPageFactory;
        this.responderFactory = responderFactory;
        this.htmlPageFactory = htmlPageFactory;
        this.authenticatorProvider = authenticatorProvider;
        this.injector = injector;
        this.root = root;
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
