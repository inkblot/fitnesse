// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.io.IOException;
import java.net.SocketException;

public abstract class ChunkingResponder implements Responder {
    protected WikiPage root;
    public WikiPage page;
    protected WikiPagePath path;
    protected Request request;
    protected ChunkedResponse response;
    private boolean dontChunk = false;
    private final HtmlPageFactory htmlPageFactory;

    public ChunkingResponder(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        this.request = request;
        this.root = context.root;
        String format = (String) request.getInput("format");
        response = new ChunkedResponse(format);
        if (dontChunk || context.doNotChunk || request.hasInput("nochunk"))
            response.turnOffChunking();
        getRequestedPage(request);
        if (page == null && shouldRespondWith404())
            return pageNotFoundResponse(context, request);

        Thread respondingThread = new Thread(new RespondingRunnable(context), getClass() + ": Responding Thread");
        respondingThread.start();

        return response;
    }

    public void turnOffChunking() {
        dontChunk = true;
    }

    private void getRequestedPage(Request request) throws IOException {
        path = PathParser.parse(request.getResource());
        page = getPageCrawler().getPage(root, path);
    }

    protected PageCrawler getPageCrawler() {
        return root.getPageCrawler();
    }

    private Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception {
        return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);
    }

    protected boolean shouldRespondWith404() {
        return true;
    }

    private void startSending(FitNesseContext context) {
        try {
            doSending(context);
        } catch (SocketException e) {
            // normal. someone stopped the request.
        } catch (Exception e) {
            addExceptionAndCloseResponse(e);
        }
    }

    private void addExceptionAndCloseResponse(Exception e) {
        try {
            response.add(ErrorResponder.makeExceptionString(e));
            response.closeAll();
        } catch (Exception e1) {
        }
    }

    protected class RespondingRunnable implements Runnable {
        private final FitNesseContext context;

        public RespondingRunnable(FitNesseContext context) {
            this.context = context;
        }

        public void run() {
            while (!response.isReadyToSend()) {
                try {
                    synchronized (response) {
                        response.notifyAll();
                        response.wait();
                    }
                } catch (InterruptedException e) {
                    //ok
                }
            }
            startSending(this.context);
        }
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    protected abstract void doSending(FitNesseContext context) throws Exception;
}
