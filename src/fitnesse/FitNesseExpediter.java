// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.http.HttpException;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.responders.ErrorResponder;
import util.ClockUtil;
import util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class FitNesseExpediter implements ResponseSender {
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

    public FitNesseExpediter(Socket s,
                             FitNesseContext context) throws Exception {
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

    public void send(byte[] bytes) throws Exception {
        try {
            output.write(bytes);
            output.flush();
        } catch (IOException stopButtonPressed_probably) {
            // TODO: When does this exception occur and what is the appropriate corrective action?
        }
    }

    public void close() throws Exception {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Socket getSocket() throws Exception {
        return socket;
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
        if (StringUtil.isBlank(request.getResource()) && StringUtil.isBlank(request.getQueryString()))
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
                reportError(408, "The client request has been unproductive for too long.  It has timed out and will now longer be processed");
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
