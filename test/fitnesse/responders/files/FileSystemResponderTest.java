// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.http.*;
import fitnesse.responders.ResponderFactory;
import fitnesse.wiki.WikiModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.StringUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static util.RegexAssertions.*;

public class FileSystemResponderTest extends FitnesseBaseTestCase {
    MockRequest request;
    // Example: "Tue, 02 Apr 2003 22:18:49 GMT"
    private final String HTTP_DATE_REGEXP = "[SMTWF][a-z]{2}\\,\\s[0-9]{2}\\s[JFMASOND][a-z]{2}\\s[0-9]{4}\\s[0-9]{2}\\:[0-9]{2}\\:[0-9]{2}\\sGMT";

    private Response response;
    private FileResponder responder;
    private Locale saveLocale;
    private String rootPagePath;
    private SampleFileUtility samples;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath, SampleFileUtility samples) {
        this.rootPagePath = rootPagePath;
        this.samples = samples;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
        samples.makeSampleFiles();
        response = null;
        saveLocale = Locale.getDefault();
    }

    @After
    public void tearDown() throws Exception {
        if (response != null) response.readyToSend(new MockResponseSender());
        Locale.setDefault(saveLocale);
    }

    @Test
    public void testFileContent() throws Exception {
        request.setResource("files/testFile1");
        responder = (FileResponder) ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        response = responder.makeResponse(request);
        assertEquals(InputStreamResponse.class, response.getClass());
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        assertSubString("file1 content", sender.sentData());
    }

    @Test
    public void testSpacesInFileName() throws Exception {
        String restoredPath = StringUtil.decodeURLText("files/test%20File%20With%20Spaces%20In%20Name");
        assertEquals("files/test File With Spaces In Name", restoredPath);
    }

    @Test
    public void testLastModifiedHeader() throws Exception {
        Locale.setDefault(Locale.US);
        request.setResource("files/testFile1");
        responder = (FileResponder) ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        response = responder.makeResponse(request);
        String lastModifiedHeader = response.getHeader("Last-Modified");
        assertMatches(HTTP_DATE_REGEXP, lastModifiedHeader);
    }

    @Test
    public void test304IfNotModified() throws Exception {
        Locale.setDefault(Locale.US);
        Calendar now = new GregorianCalendar();
        now.add(Calendar.DATE, -1);
        String yesterday = SimpleResponse.makeStandardHttpDateFormat().format(now.getTime());
        now.add(Calendar.DATE, 2);
        String tomorrow = SimpleResponse.makeStandardHttpDateFormat().format(now.getTime());

        request.setResource("files/testFile1");
        request.addHeader("If-Modified-Since", yesterday);
        responder = (FileResponder) ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        response = responder.makeResponse(request);
        assertEquals(200, response.getStatus());

        request.setResource("files/testFile1");
        request.addHeader("If-Modified-Since", tomorrow);
        responder = (FileResponder) ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        SimpleResponse notModifiedResponse = (SimpleResponse) responder.makeResponse(request);
        assertEquals(304, notModifiedResponse.getStatus());
        assertEquals("", notModifiedResponse.getContent());
        assertMatches(HTTP_DATE_REGEXP, notModifiedResponse.getHeader("Date"));
        assertNotNull(notModifiedResponse.getHeader("Cache-Control"));
    }

    @Test
    public void testRecoverFromUnparseableDateInIfNotModifiedHeader() throws Exception {
        request.setResource("files/testFile1");
        request.addHeader("If-Modified-Since", "Unparseable Date");
        responder = (FileResponder) ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        response = responder.makeResponse(request);
        assertEquals(200, response.getStatus());
    }

    @Test
    public void testNotFoundFile() throws Exception {
        request.setResource("files/something/that/aint/there");
        Responder notFoundResponder = ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        SimpleResponse response = (SimpleResponse) notFoundResponder.makeResponse(request);
        assertEquals(404, response.getStatus());
        assertHasRegexp("files/something/that/aint/there", response.getContent());
    }

    @Test
    public void testCssMimeType() throws Exception {
        samples.addFile("/files/fitnesse.css", "body{color: red;}");
        request.setResource("files/fitnesse.css");
        responder = (FileResponder) ResponderFactory.makeFileResponder(injector, request.getResource(), rootPagePath);
        response = responder.makeResponse(request);
        assertEquals("text/css", response.getContentType());
    }
}
