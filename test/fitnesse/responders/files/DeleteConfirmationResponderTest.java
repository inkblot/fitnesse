// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Before;
import org.junit.Test;

import static util.RegexAssertions.assertSubString;

public class DeleteConfirmationResponderTest extends FitnesseBaseTestCase {
    MockRequest request;
    private FitNesseContext context;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
        context = makeContext();
    }

    @Test
    public void testContentOfPage() throws Exception {
        request.setResource("files");
        request.addInput("filename", "MyFile.txt");
        Responder responder = new DeleteConfirmationResponder(htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String content = response.getContent();

        assertSubString("deleteFile", content);
        assertSubString("Delete File", content);
        assertSubString("MyFile.txt", content);
    }

}
