// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.http;

import fitnesse.components.Base64;
import util.ImpossibleException;
import util.StreamReader;

import java.io.*;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.*;

public class RequestBuilder {
    private static final byte[] ENDL = "\r\n".getBytes();
    private static final Random RANDOM_GENERATOR = new SecureRandom();

    private String resource;
    private String method = "GET";
    private List<InputStream> bodyParts = new LinkedList<InputStream>();
    private HashMap<String, String> headers = new HashMap<String, String>();
    private HashMap<String, Object> inputs = new HashMap<String, Object>();
    private String host;
    private int port;
    private String boundary;
    private boolean isMultipart = false;
    private int bodyLength = 0;

    public RequestBuilder(String resource) {
        this.resource = resource;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public void addHeader(String key, String value) {
        headers.put(key, value);
    }

    public String getText() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        send(output);
        return output.toString();
    }

    private String buildRequestLine() {
        StringBuilder text = new StringBuilder();
        text.append(method).append(" ").append(resource);
        if (isGet()) {
            String inputString = inputString();
            if (inputString.length() > 0)
                text.append("?").append(inputString);
        }
        text.append(" HTTP/1.1");
        return text.toString();
    }

    private boolean isGet() {
        return method.equals("GET");
    }

    public void send(OutputStream output) throws IOException {
        try {
            output.write(buildRequestLine().getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        output.write(ENDL);
        buildBody();
        sendHeaders(output);
        output.write(ENDL);
        sendBody(output);
    }

    private void sendHeaders(OutputStream output) throws IOException {
        addHostHeader();
        for (String key : headers.keySet()) {
            try {
                output.write((key + ": " + headers.get(key)).getBytes("UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new ImpossibleException("UTF-8 is a supported encoding", e);
            }
            output.write(ENDL);
        }
    }

    private void buildBody() {
        if (!isMultipart) {
            byte[] bytes;
            try {
                bytes = inputString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new ImpossibleException("UTF-8 is a supported encoding", e);
            }
            bodyParts.add(new ByteArrayInputStream(bytes));
            bodyLength += bytes.length;
        } else {
            for (String name : inputs.keySet()) {
                Object value = inputs.get(name);
                StringBuilder partBuffer = new StringBuilder();
                partBuffer.append("--").append(getBoundary()).append("\r\n");
                partBuffer.append("Content-Disposition: form-data; name=\"").append(name).append("\"").append("\r\n");
                if (value instanceof InputStreamPart) {
                    InputStreamPart part = (InputStreamPart) value;
                    partBuffer.append("Content-Type: ").append(part.contentType).append("\r\n");
                    partBuffer.append("\r\n");
                    addBodyPart(partBuffer.toString());
                    bodyParts.add(part.input);
                    bodyLength += part.size;
                    addBodyPart("\r\n");
                } else {
                    partBuffer.append("Content-Type: text/plain").append("\r\n");
                    partBuffer.append("\r\n");
                    partBuffer.append(value);
                    partBuffer.append("\r\n");
                    addBodyPart(partBuffer.toString());
                }
            }
            StringBuilder tail = new StringBuilder();
            tail.append("--").append(getBoundary()).append("--").append("\r\n");
            addBodyPart(tail.toString());
        }
        addHeader("Content-Length", bodyLength + "");
    }

    private void addBodyPart(String input) {
        byte[] bytes;
        try {
            bytes = input.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        bodyParts.add(new ByteArrayInputStream(bytes));
        bodyLength += bytes.length;
    }

    private void sendBody(OutputStream output) throws IOException {
        for (InputStream input : bodyParts) {
            StreamReader reader = new StreamReader(input);
            while (!reader.isEof()) {
                byte[] bytes = reader.readBytes(1000);
                output.write(bytes);
            }
        }
    }

    private void addHostHeader() {
        if (host != null)
            addHeader("Host", host + ":" + port);
        else
            addHeader("Host", "");
    }

    public void addInput(String key, Object value) {
        inputs.put(key, value);
    }

    public String inputString() {
        StringBuilder buffer = new StringBuilder();
        boolean first = true;
        for (String key : inputs.keySet()) {
            String value = (String) inputs.get(key);
            if (!first)
                buffer.append("&");
            try {
            buffer.append(key).append("=").append(URLEncoder.encode(value, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                throw new ImpossibleException("UTF-8 is a supported encoding", e);
            }
            first = false;
        }
        return buffer.toString();
    }

    public void addCredentials(String username, String password) {
        String rawUserpass = username + ":" + password;
        String userpass = Base64.encode(rawUserpass);
        addHeader("Authorization", "Basic " + userpass);
    }

    public void setHostAndPort(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public String getBoundary() {
        if (boundary == null) {
            boundary = "----------" + RANDOM_GENERATOR.nextInt() + "BoUnDaRy";
        }
        return boundary;
    }

    public void addInputAsPart(String name, Object content) {
        multipart();
        addInput(name, content);
    }

    public void addInputAsPart(String name, InputStream input, int size, String contentType) {
        addInputAsPart(name, new InputStreamPart(input, size, contentType));
    }

    private void multipart() {
        if (!isMultipart) {
            isMultipart = true;
            setMethod("POST");
            addHeader("Content-Type", "multipart/form-data; boundary=" + getBoundary());
        }
    }

    private static class InputStreamPart {
        public InputStream input;
        public int size;
        public String contentType;

        public InputStreamPart(InputStream input, int size, String contentType) {
            this.input = input;
            this.size = size;
            this.contentType = contentType;
        }
    }
}
