// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import com.google.inject.*;
import com.google.inject.name.Names;
import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNeseModule;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.components.SaveRecorder;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.InMemoryPage;
import fitnesse.wiki.WikiPage;
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
                new FitNeseModule(properties, null, baseDir, "RooT", 9123, true),
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

        context = injector.getInstance(FitNesseContext.class);
        root = injector.getInstance(Key.get(WikiPage.class, Names.named(FitNeseModule.ROOT_PAGE)));
        fitnesse = injector.getInstance(FitNesse.class);
        File historyDirectory = context.getTestHistoryDirectory();
        if (historyDirectory.exists())
            FileUtil.deleteFileSystemDirectory(historyDirectory);
        historyDirectory.mkdirs();
        SaveRecorder.clear();
        fitnesse.start();
    }
}
