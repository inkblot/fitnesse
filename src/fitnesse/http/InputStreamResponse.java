// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamResponse extends Response {
    private InputStream input;
    private Integer contentSize = null;

    public InputStreamResponse() {
        super("html");
    }

    public void readyToSend(ResponseSender sender) throws IOException {
        try {
            addStandardHeaders();
            sender.send(makeHttpHeaders().getBytes());
            IOUtils.copy(input, sender.getOutputStream());
        } finally {
            sender.close();
        }
    }

    protected void addSpecificHeaders() {
        if (getContentSize() != null)
            addHeader("Content-Length", getContentSize() + "");
    }

    public Integer getContentSize() {
        return contentSize;
    }

    public void setBody(InputStream input, Integer size) {
        this.input = input;
        contentSize = size;
    }

    public void setBody(File file) throws Exception {
        FileInputStream input = new FileInputStream(file);
        int size = (int) file.length();
        setBody(input, size);
    }
}
