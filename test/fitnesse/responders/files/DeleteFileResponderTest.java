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

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DeleteFileResponderTest extends SingleContextBaseTestCase {
    public MockRequest request;
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
        assertTrue(new File(getRootPath(), "RooT").mkdir());
    }

    @Test
    public void testDelete() throws Exception {
        File file = new File(new File(getRootPath(), "RooT"), "testfile");
        assertTrue(file.createNewFile());
        DeleteFileResponder responder = new DeleteFileResponder(rootPagePath);
        request.addInput("filename", "testfile");
        request.setResource("");
        Response response = responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertEquals(303, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }

    @Test
    public void testDeleteDirectory() throws Exception {
        File dir = new File(new File(getRootPath(), "RooT"), "dir");
        assertTrue(dir.mkdir());
        File file = new File(dir, "testChildFile");
        assertTrue(file.createNewFile());
        DeleteFileResponder responder = new DeleteFileResponder(rootPagePath);
        request.addInput("filename", "dir");
        request.setResource("");
        responder.makeResponse(context, request);
        assertFalse(file.exists());
        assertFalse(dir.exists());

    }
}
