// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeleteFileResponderTest extends FitnesseBaseTestCase {
    public MockRequest request;
    private FitNesseContext context;

    @Before
    public void setUp() {
        FileUtil.makeDir("testdir");
        request = new MockRequest();
        context = new FitNesseContext("testdir");
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.deleteFileSystemDirectory("testdir");
    }

    @Test
    public void testDelete() throws Exception {
        File file = new File("testdir/testfile");
        assertTrue(file.createNewFile());
        DeleteFileResponder responder = new DeleteFileResponder();
        request.addInput("filename", "testfile");
        request.setResource("");
        Response response = responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertEquals(303, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }

    @Test
    public void testDeleteDirectory() throws Exception {
        File dir = new File("testdir/dir");
        assertTrue(dir.mkdir());
        File file = new File(dir, "testChildFile");
        assertTrue(file.createNewFile());
        DeleteFileResponder responder = new DeleteFileResponder();
        request.addInput("filename", "dir");
        request.setResource("");
        responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertFalse(dir.exists());

    }
}
