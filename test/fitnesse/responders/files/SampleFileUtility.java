// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import java.io.File;

import util.FileUtil;

public class SampleFileUtility {
    public final String base;
    public final File testDir;
    public final File testFile1;

    public SampleFileUtility() {
        this("testdir");
    }

    public SampleFileUtility(String base) {
        this.base = base;
        testDir = new File(new File(base, "files"), "testDir");
        testFile1 = new File(base, "files/testFile1");
    }

    public void makeSampleFiles() {
        File dir = new File(base);
        dir.mkdir();
        File filesDir = new File(dir, "files");
        filesDir.mkdir();
        testDir.mkdir();

        FileUtil.createFile(testFile1, "file1 content");
        FileUtil.createFile(base + "/files/testDir/testFile2", "file2 content");
        FileUtil.createFile(base + "/files/testDir/testFile3", "file3 content");
        FileUtil.createFile(base + "/files/file4 with spaces.txt", "file4 content");
    }

    public void deleteSampleFiles() {
        FileUtil.deleteFileSystemDirectory(base);
    }

    public void addFile(String name, String content) {
        FileUtil.createFile(base + name, content);
    }
}
