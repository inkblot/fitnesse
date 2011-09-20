// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;

import static org.junit.Assert.*;

public class RenameFileResponderTest extends FitnesseBaseTestCase {
    private MockRequest request;
    private FitNesseContext context;

    @Before
    public void setUp() {
        request = new MockRequest();
        context = makeContext();
        FileUtil.makeDir(context.rootPagePath);
    }

    @Test
    public void testMakeResponse() throws Exception {
        File file = new File(context.rootPagePath, "testfile");
        assertTrue(file.createNewFile());
        RenameFileResponder responder = new RenameFileResponder();
        request.addInput("filename", "testfile");
        request.addInput("newName", "newName");
        request.setResource("");
        Response response = responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertTrue(new File(context.rootPagePath, "newName").exists());
        assertEquals(303, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }

    @Test
    public void testRenameWithTrailingSpace() throws Exception {
        File file = new File(context.rootPagePath, "testfile");
        assertTrue(file.createNewFile());
        RenameFileResponder responder = new RenameFileResponder();
        request.addInput("filename", "testfile");
        request.addInput("newName", "new Name With Space ");
        request.setResource("");
        responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertTrue(new File(context.rootPagePath, "new Name With Space").exists());
    }

}
