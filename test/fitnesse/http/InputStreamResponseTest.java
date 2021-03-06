// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import junit.framework.TestCase;
import util.FileUtil;

import java.io.*;

import static util.RegexAssertions.assertSubString;

public class InputStreamResponseTest extends TestCase implements ResponseSender {
    private InputStreamResponse response;
    private boolean closed = false;
    private ByteArrayOutputStream output;
    private File testFile = new File("testFile.test");

    public void setUp() throws Exception {
        response = new InputStreamResponse();
        output = new ByteArrayOutputStream();
    }

    public void tearDown() throws Exception {
        FileUtil.deleteFile(testFile);
    }

    public void testSimpleUsage() throws Exception {
        ByteArrayInputStream input = new ByteArrayInputStream("content".getBytes());
        response.setBody(input, 7);
        response.readyToSend(this);
        assertTrue(closed);

        ResponseParser result = new ResponseParser(new ByteArrayInputStream(output.toByteArray()));
        assertEquals(200, result.getStatus());
        assertEquals(7 + "", result.getHeader("Content-Length"));
        assertEquals("content", result.getBody());
    }

    public void testWithFile() throws Exception {
        FileUtil.createFile(testFile, "content");
        response.setBody(testFile);
        response.readyToSend(this);
        assertTrue(closed);

        ResponseParser result = new ResponseParser(new ByteArrayInputStream(output.toByteArray()));
        assertEquals(200, result.getStatus());
        assertEquals(7 + "", result.getHeader("Content-Length"));
        assertEquals("content", result.getBody());
    }

    public void testWithLargeFile() throws Exception {
        writeLinesToFile(1000);

        response.setBody(testFile);
        response.readyToSend(this);
        String responseString = output.toString();
        assertSubString("Content-Length: 100000", responseString);
        assertTrue(output.toByteArray().length > 100000);
    }

    public void testWithLargerFile() throws Exception {
        writeLinesToFile(100000);

        response.setBody(testFile);
        response.readyToSend(this);
        String responseString = output.toString();
        assertSubString("Content-Length: 10000000", responseString);
        assertTrue(output.toByteArray().length > 10000000);
    }

    // Don't run unless you have some time to kill.
    public void _testWithReallyBigFile() throws Exception {
        writeLinesToFile(10000000);

        response.setBody(testFile);
        response.readyToSend(this);
        String responseString = output.toString();
        assertSubString("Content-Length: 1000000000", responseString);
        assertTrue(output.toByteArray().length > 1000000000);
    }

    private void writeLinesToFile(int lines) throws IOException {
        String sampleLine = "This is a sample line of a large file that's created for the large file tests. It's 100 bytes long.\n";
        byte[] bytes = sampleLine.getBytes();
        FileOutputStream testFileOutput = new FileOutputStream(testFile);
        for (int i = 0; i < lines; i++)
            testFileOutput.write(bytes);
        testFileOutput.close();
    }

    public void send(byte[] bytes) {
        try {
            output.write(bytes);
        } catch (IOException e) {
            e.printStackTrace(System.err);
            fail("No IOException should occur here");
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
        return output;
    }
}
