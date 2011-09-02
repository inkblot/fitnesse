// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import com.google.inject.Guice;
import fit.Fixture;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.components.SaveRecorder;
import fitnesse.responders.WikiImportTestEventListener;
import fitnesse.wiki.InMemoryPage;
import util.FileUtil;

import java.io.File;

import static fitnesse.fixtures.FitnesseFixtureContext.*;

public class SetUp extends Fixture {
    public SetUp() throws Exception {
        Guice.createInjector(new FitNesseModule());

        //TODO - Inject the test listeners
        WikiImportTestEventListener.register();

        root = InMemoryPage.makeRoot("RooT");
        context = new FitNesseContext(root, baseDir);
        context.port = 9123;
        fitnesse = new FitNesse(context, false);
        File historyDirectory = context.getTestHistoryDirectory();
        if (historyDirectory.exists())
            FileUtil.deleteFileSystemDirectory(historyDirectory);
        historyDirectory.mkdirs();
        SaveRecorder.clear();
        fitnesse.start();
    }
}
