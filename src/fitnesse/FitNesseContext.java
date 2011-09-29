// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.run.RunningTestingTracker;
import fitnesse.responders.run.SocketDealer;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class FitNesseContext {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseContext.class);

    public static final String DEFAULT_PATH = ".";
    public static final String DEFAULT_ROOT = "FitNesseRoot";
    public static final int DEFAULT_PORT = 80;
    public static final int DEFAULT_COMMAND_PORT = 9123;
    public static final int DEFAULT_VERSION_DAYS = 14;
    public static final String RECENT_CHANGES_DATE_FORMAT = "kk:mm:ss EEE, MMM dd, yyyy";
    public static final String RFC_COMPLIANT_DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss Z";

    public static FitNesseContext globalContext;

    private final Injector injector;
    public final String rootPath;
    public final WikiPage root;
    public final String rootPagePath;
    public final ResponderFactory responderFactory;

    public FitNesse fitnesse;
    public int port = DEFAULT_PORT;
    public String defaultNewPageContent = "!contents -R2 -g -p -f -h";
    public SocketDealer socketDealer = new SocketDealer();
    public RunningTestingTracker runningTestingTracker = new RunningTestingTracker();
    public Authenticator authenticator = new PromiscuousAuthenticator();
    public HtmlPageFactory htmlPageFactory = new HtmlPageFactory();
    public String testResultsDirectoryName = "testResults";
    public boolean doNotChunk;

    public FitNesseContext(Injector injector, String rootPath, String rootPageName) throws Exception {
        this.injector = injector.createChildInjector(new ContextModule(this, rootPath, rootPageName));
        this.rootPath = rootPath;
        WikiPageFactory wikiPageFactory = getInjector().getInstance(WikiPageFactory.class);
        this.root = wikiPageFactory.makeRootPage(rootPath, rootPageName);
        String absolutePath = new File(this.rootPath).getAbsolutePath();
        if (!absolutePath.equals(this.rootPath)) {
            logger.warn("rootPath is not absolute: rootPath=" + this.rootPath + " absolutePath=" + absolutePath, new RuntimeException());
        }
        rootPagePath = rootPath + File.separator + root.getName();
        responderFactory = new ResponderFactory(this.injector, rootPagePath);
    }


    public String toString() {
        String endl = System.getProperty("line.separator");
        StringBuilder buffer = new StringBuilder();
        buffer.append("\t").append("port:              ").append(port).append(endl);
        buffer.append("\t").append("root page:         ").append(root).append(endl);
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

    public Injector getInjector() {
        return injector;
    }

    public static class ContextModule extends AbstractModule {
        private final FitNesseContext context;
        private final String rootPath;
        private final String rootPageName;

        public ContextModule(FitNesseContext context, String rootPath, String rootPageName) {
            this.context = context;
            this.rootPath = rootPath;
            this.rootPageName = rootPageName;
        }

        @Override
        protected void configure() {
            bind(FitNesseContext.class).toInstance(context);
            bind(String.class).annotatedWith(Names.named("fitnesse.rootPath")).toInstance(rootPath);
            bind(String.class).annotatedWith(Names.named("fitnesse.rootPageName")).toInstance(rootPageName);
        }
    }
}
