// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.http.HttpException;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.responders.ErrorResponder;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.ClockUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class FitNesseExpediter implements ResponseSender {
    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    private Socket socket;
    private InputStream input;
    private OutputStream output;
    private Request request;
    private Response response;
    private FitNesseContext context;
    protected long requestParsingTimeLimit;
    private long requestProgress;
    private long requestParsingDeadline;
    private volatile boolean hasError;

    public FitNesseExpediter(Socket s, FitNesseContext context) throws IOException {
        this.context = context;
        socket = s;
        input = s.getInputStream();
        output = s.getOutputStream();
        requestParsingTimeLimit = 10000;
    }

    public void start() throws Exception {
        try {
            Request request = makeRequest();
            makeResponse(request);
            sendResponse();
        } catch (SocketException se) {
            // can be thrown by makeResponse or sendResponse.
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void setRequestParsingTimeLimit(long t) {
        requestParsingTimeLimit = t;
    }

    public void send(byte[] bytes) {
        try {
            output.write(bytes);
            output.flush();
        } catch (IOException e) {
            // output stream closed prematurely, probably by user action
        }
    }

    public void close() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.error("Could not close socket", e);
        }
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return socket.getInputStream();
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        return socket.getOutputStream();
    }

    public Request makeRequest() throws Exception {
        request = new Request(input);
        return request;
    }

    public void sendResponse() throws Exception {
        response.readyToSend(this);
    }

    private Response makeResponse(Request request) throws Exception {
        try {
            Thread parseThread = createParsingThread(request);
            parseThread.start();

            waitForRequest(request);
            if (!hasError)
                response = createGoodResponse(request);
        } catch (SocketException se) {
            throw (se);
        } catch (Exception e) {
            response = new ErrorResponder(e).makeResponse(context, request);
        }
        return response;
    }

    public Response createGoodResponse(Request request) throws Exception {
        Response response;
        if (StringUtils.isEmpty(request.getResource()) && StringUtils.isEmpty(request.getQueryString()))
            request.setResource("FrontPage");
        Responder responder = context.responderFactory.makeResponder(request);
        responder = context.authenticator.authenticate(context, request, responder);
        response = responder.makeResponse(context, request);
        response.addHeader("Server", "FitNesse-" + FitNesse.VERSION);
        response.addHeader("Connection", "close");
        return response;
    }

    private void waitForRequest(Request request) throws InterruptedException {
        long now = ClockUtil.currentTimeInMillis();
        requestParsingDeadline = now + requestParsingTimeLimit;
        requestProgress = 0;
        while (!hasError && !request.hasBeenParsed()) {
            Thread.sleep(10);
            if (timeIsUp(now) && parsingIsUnproductive(request))
                reportError(408, "The client request has been unproductive for too long.  It has timed out and will no longer be processed");
        }
    }

    private boolean parsingIsUnproductive(Request request) {
        long updatedRequestProgress = request.numberOfBytesParsed();
        if (updatedRequestProgress > requestProgress) {
            requestProgress = updatedRequestProgress;
            return false;
        } else
            return true;
    }

    private boolean timeIsUp(long now) {
        now = ClockUtil.currentTimeInMillis();
        if (now > requestParsingDeadline) {
            requestParsingDeadline = now + requestParsingTimeLimit;
            return true;
        } else
            return false;
    }

    private Thread createParsingThread(final Request request) {
        return
                new Thread() {
                    public synchronized void run() {
                        try {
                            request.parse();
                        } catch (HttpException e) {
                            reportError(400, e.getMessage());
                        } catch (Exception e) {
                            reportError(e);
                        }
                    }
                };
    }

    private void reportError(int status, String message) {
        try {
            response = new ErrorResponder(message).makeResponse(context, request);
            response.setStatus(status);
            hasError = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportError(Exception e) {
        try {
            response = new ErrorResponder(e).makeResponse(context, request);
            hasError = true;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
