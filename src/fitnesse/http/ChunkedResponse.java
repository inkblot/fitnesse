// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import java.nio.ByteBuffer;

public class ChunkedResponse extends Response {
    private ResponseSender sender;
    private int bytesSent = 0;
    private boolean isReadyToSend = false;
    private boolean dontChunk = false;

    public ChunkedResponse(String format) {
        super(format);
        if (isTextFormat())
            dontChunk = true;
    }

    public void readyToSend(ResponseSender sender) {
        this.sender = sender;
        addStandardHeaders();
        sender.send(makeHttpHeaders().getBytes());
        setReadyToSend(true);
        synchronized (this) {
            notifyAll();
            Thread.yield();
        }
    }

    public synchronized boolean isReadyToSend() {
        return isReadyToSend;
    }

    protected void addSpecificHeaders() {
        if (!dontChunk)
            addHeader("Transfer-Encoding", "chunked");
    }

    public static String asHex(int value) {
        return Integer.toHexString(value);
    }

    public void add(String text) {
        if (text != null)
            add(getEncodedBytes(text));
    }

    public void add(byte[] bytes) {
        if (bytes == null || bytes.length == 0)
            return;
        if (dontChunk) {
            sender.send(bytes);
        } else {
            String sizeLine = asHex(bytes.length) + CRLF;
            ByteBuffer chunk = ByteBuffer.allocate(sizeLine.length() + bytes.length + 2);
            chunk.put(sizeLine.getBytes()).put(bytes).put(CRLF.getBytes());
            sender.send(chunk.array());
        }
        bytesSent += bytes.length;
    }

    public void addTrailingHeader(String key, String value) {
        String header = key + ": " + value + CRLF;
        sender.send(header.getBytes());
    }

    public void closeChunks() {
        sender.send(("0" + CRLF).getBytes());
    }

    public void closeTrailer() {
        sender.send(CRLF.getBytes());
    }

    public void close() {
        sender.close();
    }

    public void closeAll() {
        closeChunks();
        closeTrailer();
        close();
    }

    public Integer getContentSize() {
        return bytesSent;
    }

    private synchronized void setReadyToSend(boolean isReadyToSend) {
        this.isReadyToSend = isReadyToSend;
    }

    public void turnOffChunking() {
        dontChunk = true;
    }

    public boolean isChunkingTurnedOff() {
        return dontChunk;
    }
}
