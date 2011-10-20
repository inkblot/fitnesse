// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Before;
import org.junit.Test;

import static util.RegexAssertions.assertSubString;

public class DeleteConfirmationResponderTest extends FitnesseBaseTestCase {
    MockRequest request;
    private String rootPagePath;
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.htmlPageFactory = htmlPageFactory;
        this.rootPagePath = rootPagePath;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
    }

    @Test
    public void testContentOfPage() throws Exception {
        request.setResource("files");
        request.addInput("filename", "MyFile.txt");
        Responder responder = new DeleteConfirmationResponder(htmlPageFactory, rootPagePath);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);
        String content = response.getContent();

        assertSubString("deleteFile", content);
        assertSubString("Delete File", content);
        assertSubString("MyFile.txt", content);
    }

}
