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
import fitnesse.wiki.WikiPage;

@Singleton
public class FitNesseContext {

    public final Injector injector;
    private final WikiPage root;
    private final int port;
    private final HtmlPageFactory htmlPageFactory;
    private final Provider<Authenticator> authenticatorProvider;

    public boolean doNotChunk;

    @Inject
    public FitNesseContext(
            @Named(FitNesseModule.PORT) Integer port,
            HtmlPageFactory htmlPageFactory,
            Provider<Authenticator> authenticatorProvider,
            Injector injector,
            @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.port = port;
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

}
