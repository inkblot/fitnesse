// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.fixtures;

import fit.Fixture;
import util.FileUtil;

import java.io.File;

import static fitnesse.fixtures.FitnesseFixtureContext.*;
import static fitnesse.fixtures.FitnesseFixtureContext.testResultsPath;

public class TearDown extends Fixture {
    public TearDown() throws Exception {
        fitnesse.stop();
        root = null;
        File historyDirectory = new File(testResultsPath);
        if (historyDirectory.exists())
            FileUtil.deleteFileSystemDirectory(historyDirectory);
    }
}
