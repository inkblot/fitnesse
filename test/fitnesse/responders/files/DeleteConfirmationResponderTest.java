// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import junit.framework.TestCase;

import static util.RegexAssertions.assertSubString;

public class DeleteConfirmationResponderTest extends TestCase {
    MockRequest request;
    private FitNesseContext context;

    public void setUp() throws Exception {
        request = new MockRequest();
        context = new FitNesseContext("RooT");
    }

    public void testContentOfPage() throws Exception {
        request.setResource("files");
        request.addInput("filename", "MyFile.txt");
        Responder responder = new DeleteConfirmationResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String content = response.getContent();

        assertSubString("deleteFile", content);
        assertSubString("Delete File", content);
        assertSubString("MyFile.txt", content);
    }

}
