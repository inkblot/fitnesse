// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class FileUpdateTest extends UpdateTestCase {
    public final File testFile = new File("classes/testFile");

    protected Update makeUpdate() throws Exception {
        return new FileUpdate(getRootPagePath(), "testFile", "files" + File.separator + "images");
    }

    @Before
    public void setUp() throws Exception {
        testFile.createNewFile();
    }

    @After
    public void tearDown() throws Exception {
        testFile.delete();
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

        File file = new File(getRootPagePath() + File.separator + "files" + File.separator + "images" + File.separator + "testFile");
        assertTrue(file.exists());

        assertFalse(update.shouldBeApplied());
    }

    @Test(expected = Exception.class)
    public void testFileMissing() throws Exception {
        update = new FileUpdate(getRootPagePath(), "images/missingFile", "files/images");
        update.doUpdate();
    }
}
  