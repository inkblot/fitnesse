// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import fitnesse.authentication.Authenticator;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.HttpException;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.ResponderFactory;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Clock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class FitNesseExpediter implements ResponseSender {
    private transient final Logger logger = LoggerFactory.getLogger(getClass());

    private FitNesseContext context;
    private HtmlPageFactory htmlPageFactory;
    private ResponderFactory responderFactory;
    private Provider<Authenticator> authenticatorProvider;
    private Clock clock;

    private final Socket socket;
    private final InputStream input;
    private final OutputStream output;
    private final long requestParsingTimeLimit;

    private Response response;
    private long requestProgress;
    private long requestParsingDeadline;
    private volatile boolean hasError;

    @Inject
    public void inject(FitNesseContext context, HtmlPageFactory htmlPageFactory, ResponderFactory responderFactory, Provider<Authenticator> authenticatorProvider, Clock clock) {
        this.context = context;
        this.htmlPageFactory = htmlPageFactory;
        this.responderFactory = responderFactory;
        this.authenticatorProvider = authenticatorProvider;
        this.clock = clock;
    }

    public FitNesseExpediter(Injector injector, Socket s) throws IOException {
        this(injector, s, 10000L);
    }

    public FitNesseExpediter(Injector injector, Socket s, long requestParsingTimeLimit) throws IOException {
        injector.injectMembers(this);
        socket = s;
        input = s.getInputStream();
        output = s.getOutputStream();
        this.requestParsingTimeLimit = requestParsingTimeLimit;
    }

    public void start() throws Exception {
        try {
            Request request = new Request(input);
            makeResponse(request);
            sendResponse();
        } catch (SocketException se) {
            // can be thrown by makeResponse or sendResponse.
        } catch (Throwable e) {
            e.printStackTrace();
        }
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
            response = new ErrorResponder(e, htmlPageFactory).makeResponse(context, request);
        }
        return response;
    }

    public Response createGoodResponse(Request request) throws Exception {
        Response response;
        if (StringUtils.isEmpty(request.getResource()) && StringUtils.isEmpty(request.getQueryString()))
            request.setResource("FrontPage");
        Responder responder = responderFactory.makeResponder(request);
        responder = authenticatorProvider.get().authenticate(context, request, responder);
        response = responder.makeResponse(context, request);
        response.addHeader("Server", "FitNesse-" + FitNesse.VERSION);
        response.addHeader("Connection", "close");
        return response;
    }

    private void waitForRequest(Request request) throws InterruptedException {
        long now = clock.currentClockTimeInMillis();
        requestParsingDeadline = now + requestParsingTimeLimit;
        requestProgress = 0;
        while (!hasError && !request.hasBeenParsed()) {
            Thread.sleep(10);
            if (timeIsUp(now) && parsingIsUnproductive(request))
                reportError(408, "The client request has been unproductive for too long.  It has timed out and will no longer be processed", request);
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
        now = clock.currentClockTimeInMillis();
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
                            reportError(400, e.getMessage(), request);
                        } catch (Exception e) {
                            reportError(e, request);
                        }
                    }
                };
    }

    private void reportError(int status, String message, Request request) {
        try {
            response = new ErrorResponder(message, htmlPageFactory).makeResponse(context, request);
            response.setStatus(status);
            hasError = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void reportError(Exception e, Request request) {
        try {
            response = new ErrorResponder(e, htmlPageFactory).makeResponse(context, request);
            hasError = true;
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
