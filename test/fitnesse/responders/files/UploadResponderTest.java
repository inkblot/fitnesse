// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.UploadedFile;
import fitnesse.wiki.WikiModule;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UploadResponderTest extends FitnesseBaseTestCase {
    private UploadResponder responder;
    private MockRequest request;
    private File testFile;
    private String rootPagePath;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.rootPagePath = rootPagePath;
    }

    @Before
    public void setUp() throws Exception {
        FileUtil.makeDir(rootPagePath);
        FileUtil.makeDir(rootPagePath + "/files");
        testFile = FileUtil.createFile(rootPagePath + "/tempFile.txt", "test content");

        responder = new UploadResponder(rootPagePath);
        request = new MockRequest();
    }

    @Test
    public void testMakeResponse() throws Exception {
        request.addInput("file", new UploadedFile("sourceFilename.txt", "plain/text", testFile));
        request.setResource("files/");

        Response response = responder.makeResponse(request);

        File file = new File(rootPagePath, "files/sourceFilename.txt");
        assertTrue(file.exists());
        assertEquals("test content", FileUtil.getFileContent(file));

        assertEquals(303, response.getStatus());
        assertEquals("/files/", response.getHeader("Location"));
    }

    @Test
    public void testMakeResponseSpaceInFileName() throws Exception {
        request.addInput("file", new UploadedFile("source filename.txt", "plain/text", testFile));
        request.setResource("files/");

        Response response = responder.makeResponse(request);

        File file = new File(rootPagePath, "files/source filename.txt");
        assertTrue(file.exists());
        assertEquals("test content", FileUtil.getFileContent(file));

        assertEquals(303, response.getStatus());
        assertEquals("/files/", response.getHeader("Location"));
    }

    @Test
    public void testMakeResponseSpaceInDirectoryName() throws Exception {
        FileUtil.makeDir(rootPagePath + "/files/Folder With Space");
        request.addInput("file", new UploadedFile("filename.txt", "plain/text", testFile));
        request.setResource("files/Folder%20With%20Space/");

        Response response = responder.makeResponse(request);

        File file = new File(rootPagePath, "files/Folder With Space/filename.txt");
        assertTrue(file.exists());
        assertEquals("test content", FileUtil.getFileContent(file));

        assertEquals(303, response.getStatus());
        assertEquals("/files/Folder%20With%20Space/", response.getHeader("Location"));
    }

    @Test
    public void testMakeRelativeFilename() throws Exception {
        String name1 = "name1.txt";
        String name2 = "name2";
        String name3 = "C:\\folder\\name3.txt";
        String name4 = "/home/user/name4.txt";

        assertEquals("name1.txt", UploadResponder.makeRelativeFilename(name1));
        assertEquals("name2", UploadResponder.makeRelativeFilename(name2));
        assertEquals("name3.txt", UploadResponder.makeRelativeFilename(name3));
        assertEquals("name4.txt", UploadResponder.makeRelativeFilename(name4));
    }

    @Test
    public void testMakeNewFilename() throws Exception {
        assertEquals("file_copy1.txt", UploadResponder.makeNewFilename("file.txt", 1));
        assertEquals("file_copy2.txt", UploadResponder.makeNewFilename("file.txt", 2));
        assertEquals("a.b.c.d_copy2.txt", UploadResponder.makeNewFilename("a.b.c.d.txt", 2));
        assertEquals("somefile_copy1", UploadResponder.makeNewFilename("somefile", 1));
    }

    @Test
    public void testWriteFile() throws Exception {
        File file = new File(rootPagePath, "testFile");
        File inputFile = FileUtil.createFile(rootPagePath + "/testInput", "heres the content");
        UploadedFile uploaded = new UploadedFile(rootPagePath + "/testOutput", "text", inputFile);

        long inputFileLength = inputFile.length();
        String inputFileContent = FileUtil.getFileContent(inputFile);

        responder.writeFile(file, uploaded);

        assertEquals(inputFileLength, file.length());
        assertEquals(inputFileContent, FileUtil.getFileContent(file));
    }
}
