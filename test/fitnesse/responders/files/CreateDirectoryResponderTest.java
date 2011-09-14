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
import static org.junit.Assert.assertTrue;

public class CreateDirectoryResponderTest extends FitnesseBaseTestCase {
    @Before
    public void setUp() throws Exception {
        FileUtil.makeDir("testdir");
        FileUtil.makeDir("testdir/files");
    }

    @After
    public void tearDown() throws Exception {
        FileUtil.deleteFileSystemDirectory("testdir");
    }

    @Test
    public void testMakeResponse() throws Exception {
        FitNesseContext context = new FitNesseContext("testdir");
        CreateDirectoryResponder responder = new CreateDirectoryResponder();
        MockRequest request = new MockRequest();
        request.addInput("dirname", "subdir");
        request.setResource("");

        Response response = responder.makeResponse(context, request);

        File file = new File("testdir/subdir");
        assertTrue(file.exists());
        assertTrue(file.isDirectory());

        assertEquals(303, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }
}
