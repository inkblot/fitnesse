// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CreateDirectoryResponderTest extends FitnesseBaseTestCase {
    private FitNesseContext context;
    private String rootPagePath;

    @Inject
    public void inject(FitNesseContext context, @Named(FitNesseModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.context = context;
        this.rootPagePath = rootPagePath;
    }

    @Before
    public void setUp() throws Exception {
        assertTrue(new File(getRootPath(), "RooT").mkdir());
        assertTrue(new File(new File(getRootPath(), "RooT"), "files").mkdir());
    }

    @Test
    public void testMakeResponse() throws Exception {
        CreateDirectoryResponder responder = new CreateDirectoryResponder(rootPagePath);
        MockRequest request = new MockRequest();
        request.addInput("dirname", "subdir");
        request.setResource("");

        Response response = responder.makeResponse(context, request);

        File file = new File(new File(getRootPath(), "RooT"), "subdir");
        assertTrue(file.exists());
        assertTrue(file.isDirectory());

        assertEquals(303, response.getStatus());
        assertEquals("/", response.getHeader("Location"));
    }
}
