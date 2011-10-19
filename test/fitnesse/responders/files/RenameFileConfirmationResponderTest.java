// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.SingleContextBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import org.junit.Before;
import org.junit.Test;

import static util.RegexAssertions.assertSubString;

public class RenameFileConfirmationResponderTest extends SingleContextBaseTestCase {
    MockRequest request;
    private FitNesseContext context;
    private String content;
    private HtmlPageFactory htmlPageFactory;
    private SampleFileUtility samples;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, FitNesseContext context, SampleFileUtility samples) {
        this.htmlPageFactory = htmlPageFactory;
        this.context = context;
        this.samples = samples;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
        samples.makeSampleFiles();
    }

    @Test
    public void testContentOfPage() throws Exception {
        getContentForSimpleRename();

        assertSubString("renameFile", content);
        assertSubString("Rename File", content);
        assertSubString("Rename <b>MyFile.txt</b>", content);
    }

    @Test
    public void testExistingFilenameIsInTextField() throws Exception {
        getContentForSimpleRename();

        assertSubString("<input type=\"text\" name=\"newName\" value=\"MyFile.txt\"/>", content);
    }

    private void getContentForSimpleRename() throws Exception {
        request.setResource("files");
        request.addInput("filename", "MyFile.txt");
        Responder responder = new RenameFileConfirmationResponder(htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        content = response.getContent();
    }

    @Test
    public void testFitnesseLook() throws Exception {
        Responder responder = new RenameFileConfirmationResponder(htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
        String content = response.getContent();
        assertSubString("<link rel=\"stylesheet\" type=\"text/css\" href=\"/files/css/fitnesse.css\" media=\"screen\"/>", content);
    }

}
