// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import util.ImpossibleException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class MockSocket extends Socket {
    InputStream input;
    OutputStream output;
    private String host;
    private boolean closed;

    public MockSocket(String input) {
        this(new ByteArrayInputStream(input.getBytes()), new ByteArrayOutputStream());
    }

    public MockSocket(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public InputStream getInputStream() {
        return input;
    }

    public OutputStream getOutputStream() {
        return output;
    }

    public void close() {
        closed = true;
        try {
            input.close();
            output.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isClosed() {
        return closed;
    }

    public String getOutput() {
        if (output instanceof ByteArrayOutputStream)
            try {
                return ((ByteArrayOutputStream) output).toString("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new ImpossibleException("UTF-8 is a supported encoding", e);
            }
        else
            return "";
    }

    public void setHost(String host) {
        this.host = host;
    }

    public SocketAddress getRemoteSocketAddress() {
        return new InetSocketAddress(host, 123);
    }
}
