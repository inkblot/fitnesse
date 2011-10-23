// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class FileUpdateTest extends UpdateTestCase {
    protected Update makeUpdate() {
        return new FileUpdate(rootPagePath, "testFile", "files" + File.separator + "images");
    }

    @Test
    public void testSimpleFunctions() throws Exception {
        assertTrue("doesn't want to apply", update.shouldBeApplied());
        assertTrue("wrong message", update.getMessage().equals("."));
        assertEquals("FileUpdate(testFile)", update.getName());
    }

    @Test
    public void testUpdateWithMissingDirectories() throws Exception {
        update.doUpdate();

        File file = new File(rootPagePath + File.separator + "files" + File.separator + "images" + File.separator + "testFile");
        assertTrue(file.exists());

        assertFalse(update.shouldBeApplied());
    }

    @Test(expected = Exception.class)
    public void testFileMissing() throws Exception {
        update = new FileUpdate(rootPagePath, "images/missingFile", "files/images");
        update.doUpdate();
    }
}
  