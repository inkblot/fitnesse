// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.junit.Test;
import util.FileUtil;

import java.io.File;
import java.io.FileOutputStream;

import static org.junit.Assert.*;

public class ReplacingFileUpdateTest extends UpdateTestCase {

    public final String destDirName = "subDir";
    public final String destPath = getRootPath() + "/" + "RooT" + "/" + destDirName + "/testFile";
    public final File destFile = new File(destPath);

    protected Update makeUpdate() {
        return new ReplacingFileUpdate(rootPagePath, "testFile", destDirName);
    }

    @Test
    public void testNoDestination() throws Exception {
        assertTrue(update.shouldBeApplied());
        update.doUpdate();
        assertTrue(destFile.exists());
    }

    @Test
    public void testFileMatch() throws Exception {
        update.doUpdate();
        assertFalse(update.shouldBeApplied());
    }

    @Test
    public void testFileDiffer() throws Exception {
        update.doUpdate();

        FileOutputStream output = new FileOutputStream(testFile);
        output.write("hello".getBytes());
        output.close();

        assertTrue(update.shouldBeApplied());
        update.doUpdate();

        assertEquals("hello", FileUtil.getFileContent(destFile));
    }
}
