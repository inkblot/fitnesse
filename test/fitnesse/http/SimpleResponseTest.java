// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import junit.framework.TestCase;
import util.ImpossibleException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import static util.RegexAssertions.assertHasRegexp;
import static util.RegexAssertions.assertSubString;

public class SimpleResponseTest extends TestCase implements ResponseSender {
    private String text;
    private boolean closed = false;

    public void send(byte[] bytes) {
        try {
            text = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
    }

    public void close() {
        closed = true;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return null;
    }

    public void setUp() throws Exception {
    }

    public void tearDown() throws Exception {
    }

    public void testSimpleResponse() throws Exception {
        SimpleResponse response = new SimpleResponse();
        response.setContent("some content");
        response.readyToSend(this);
        assertTrue(text.startsWith("HTTP/1.1 200 OK\r\n"));
        assertHasRegexp("Content-Length: 12", text);
        assertHasRegexp("Content-Type: text/html", text);
        assertTrue(text.endsWith("some content"));
        assertTrue(closed);
    }

    public void testPageNotFound() throws Exception {
        SimpleResponse response = new SimpleResponse(404);
        response.readyToSend(this);
        assertHasRegexp("404 Not Found", text);
    }

    public void testRedirect() throws Exception {
        SimpleResponse response = new SimpleResponse();
        response.redirect("some url");
        response.readyToSend(this);
        assertEquals(303, response.getStatus());
        assertHasRegexp("Location: some url\r\n", text);
    }

    public void testUnicodeCharacters() throws Exception {
        SimpleResponse response = new SimpleResponse();
        response.setContent("\uba80\uba81\uba82\uba83");
        response.readyToSend(this);

        assertSubString("\uba80\uba81\uba82\uba83", text);
    }
}



