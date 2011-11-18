// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util.io;

import org.apache.commons.io.IOUtils;
import util.FileUtil;
import util.ImpossibleException;

import java.io.*;

public class ContentBuffer {
    private File tempFile;
    private OutputStream outputStream;
    private boolean opened;
    private int size = 0;

    public ContentBuffer() throws Exception {
        this(".tmp");
    }

    public ContentBuffer(String ext) throws Exception {
        tempFile = File.createTempFile("FitNesse-", ext);
    }

    private void open() throws FileNotFoundException {
        if (!opened) {
            outputStream = new FileOutputStream(tempFile, true);
            opened = true;
        }
    }

    public ContentBuffer append(String value) throws IOException {
        byte[] bytes;
        try {
            bytes = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        return append(bytes);
    }

    public ContentBuffer append(byte[] bytes) throws IOException {
        open();
        size += bytes.length;
        outputStream.write(bytes);
        return this;
    }

    private void close() {
        if (opened) {
            IOUtils.closeQuietly(outputStream);
            opened = false;
        }
    }

    public String getContent() throws Exception {
        close();
        return FileUtil.getFileContent(tempFile);
    }

    public int getSize() throws Exception {
        close();
        return size;
    }

    public InputStream getInputStream() throws IOException {
        close();
        return new FileInputStream(tempFile) {
            public void close() throws IOException {
                try {
                    super.close();
                } finally {
                    tempFile.delete();
                }
            }
        };
    }

    public InputStream getNonDeleteingInputStream() throws IOException {
        close();
        return new FileInputStream(tempFile);
    }

    public OutputStream getOutputStream() throws Exception {
        return outputStream;
    }

    protected File getFile() {
        return tempFile;
    }

    public void delete() {
        tempFile.delete();
    }

    protected void finalize() throws Throwable {
        delete();
        super.finalize();
    }
}
