// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContextModule;
import fitnesse.SingleContextBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;

import static org.junit.Assert.*;

public class RenameFileResponderTest extends SingleContextBaseTestCase {
    private MockRequest request;
    private FitNesseContext context;
    private String rootPagePath;

    @Inject
    public void inject(FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.context = context;
        this.rootPagePath = rootPagePath;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
        FileUtil.makeDir(rootPagePath);
    }

    @Test
    public void testMakeResponse() throws Exception {
        File file = new File(rootPagePath, "testfile");
        assertTrue(file.createNewFile());
        RenameFileResponder responder = new RenameFileResponder(rootPagePath);
        request.addInput("filename", "testfile");
        request.addInput("newName", "newName");
        request.setResource("");
        Response response = responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertTrue(new File(rootPagePath, "newName").exists());
        assertEquals(303, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }

    @Test
    public void testRenameWithTrailingSpace() throws Exception {
        File file = new File(rootPagePath, "testfile");
        assertTrue(file.createNewFile());
        RenameFileResponder responder = new RenameFileResponder(rootPagePath);
        request.addInput("filename", "testfile");
        request.addInput("newName", "new Name With Space ");
        request.setResource("");
        responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertTrue(new File(rootPagePath, "new Name With Space").exists());
    }

}
