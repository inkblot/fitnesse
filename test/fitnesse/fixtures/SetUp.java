// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provider;
import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.SaveRecorder;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPageFactory;
import util.Clock;
import util.FileUtil;
import util.SystemClock;

import java.io.File;
import java.util.Properties;

import static fitnesse.fixtures.FitnesseFixtureContext.*;

public class SetUp extends Fixture {
    public SetUp() throws Exception {
        Properties properties = new Properties();
        properties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        FitnesseFixtureContext.clock = new SystemClock();
        FitnesseFixtureContext.authenticator = new PromiscuousAuthenticator();
        Injector injector = Guice.createInjector(
                new FitNesseModule(properties, null),
                new AbstractModule() {
                    @Override
                    protected void configure() {
                        bind(Clock.class).toProvider(new Provider<Clock>() {
                            @Override
                            public Clock get() {
                                return FitnesseFixtureContext.clock;
                            }
                        });
                        bind(Authenticator.class).toProvider(new Provider<Authenticator>() {
                            @Override
                            public Authenticator get() {
                                return FitnesseFixtureContext.authenticator;
                            }
                        });
                    }
                });

        //TODO - Inject the test listeners
        WikiImportTestEventListener.register();

        context = FitNesseContext.makeContext(injector, baseDir, "RooT", 9123);
        root = context.root;
        fitnesse = new FitNesse(context, false);
        File historyDirectory = context.getTestHistoryDirectory();
        if (historyDirectory.exists())
            FileUtil.deleteFileSystemDirectory(historyDirectory);
        historyDirectory.mkdirs();
        SaveRecorder.clear();
        fitnesse.start();
    }
}
