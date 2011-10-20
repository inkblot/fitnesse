// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import util.FileUtil;

import java.io.File;

@Singleton
public class SampleFileUtility {
    public final String rootPagePath;
    public final File testDir;
    public final File testFile1;

    @Inject
    public SampleFileUtility(@Named(FitNesseModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.rootPagePath = rootPagePath;
        testDir = new File(new File(rootPagePath, "files"), "testDir");
        testFile1 = new File(rootPagePath, "files/testFile1");
    }

    public void makeSampleFiles() {
        File dir = new File(rootPagePath);
        dir.mkdir();
        File filesDir = new File(dir, "files");
        filesDir.mkdir();
        testDir.mkdir();

        FileUtil.createFile(testFile1, "file1 content");
        FileUtil.createFile(rootPagePath + "/files/testDir/testFile2", "file2 content");
        FileUtil.createFile(rootPagePath + "/files/testDir/testFile3", "file3 content");
        FileUtil.createFile(rootPagePath + "/files/file4 with spaces.txt", "file4 content");
    }

    public void addFile(String name, String content) {
        FileUtil.createFile(rootPagePath + name, content);
    }
}
