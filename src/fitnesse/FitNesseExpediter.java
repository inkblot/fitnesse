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
import util.Clock;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import static org.apache.commons.io.IOUtils.closeQuietly;

public class FitNesseExpediter implements ResponseSender {

    private HtmlPageFactory htmlPageFactory;
    private ResponderFactory responderFactory;
    private Provider<Authenticator> authenticatorProvider;
    private Clock clock;

    private final InputStream input;
    private final OutputStream output;
    private final long requestParsingTimeLimit;

    private Response response;
    private long requestProgress;
    private long requestParsingDeadline;
    private volatile boolean hasError;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, ResponderFactory responderFactory, Provider<Authenticator> authenticatorProvider, Clock clock) {
        this.htmlPageFactory = htmlPageFactory;
        this.responderFactory = responderFactory;
        this.authenticatorProvider = authenticatorProvider;
        this.clock = clock;
    }

    public FitNesseExpediter(Injector injector, InputStream inputStream, OutputStream outputStream) {
        this(injector, inputStream, outputStream, 10000L);
    }

    public FitNesseExpediter(Injector injector, InputStream inputStream, OutputStream outputStream, long requestParsingTimeLimit) {
        injector.injectMembers(this);
        input = inputStream;
        output = outputStream;
        this.requestParsingTimeLimit = requestParsingTimeLimit;
    }

    public void start() throws Exception {
        try {
            Request request = new Request(input);
            makeResponse(request);
            sendResponse();
        } catch (SocketException se) {
            // can be thrown by makeResponse or sendResponse.
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
        closeQuietly(input);
        closeQuietly(output);
    }

    @Override
    public InputStream getInputStream() {
        return input;
    }

    @Override
    public OutputStream getOutputStream() {
        return output;
    }

    public void sendResponse() throws IOException {
        response.readyToSend(this);
    }

    private Response makeResponse(Request request) throws SocketException {
        try {
            Thread parseThread = createParsingThread(request);
            parseThread.start();

            waitForRequest(request);
            if (!hasError)
                response = createGoodResponse(request);
        } catch (SocketException se) {
            throw (se);
        } catch (Exception e) {
            response = new ErrorResponder(e, htmlPageFactory).makeResponse(request);
        }
        return response;
    }

    public Response createGoodResponse(Request request) throws Exception {
        Response response;
        if (StringUtils.isEmpty(request.getResource()) && StringUtils.isEmpty(request.getQueryString()))
            request.setResource("FrontPage");
        Responder responder = responderFactory.makeResponder(request);
        responder = authenticatorProvider.get().authenticate(request, responder);
        response = responder.makeResponse(request);
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
        response = new ErrorResponder(message, htmlPageFactory).makeResponse(request);
        response.setStatus(status);
        hasError = true;
    }

    private void reportError(Exception e, Request request) {
        response = new ErrorResponder(e, htmlPageFactory).makeResponse(request);
        hasError = true;
    }

}
