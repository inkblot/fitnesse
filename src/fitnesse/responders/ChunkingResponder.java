// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.ChunkedResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.net.SocketException;

public abstract class ChunkingResponder implements Responder {
    protected Request request;
    protected ChunkedResponse response;
    private boolean dontChunk = false;
    private final HtmlPageFactory htmlPageFactory;

    public ChunkingResponder(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        this.request = request;
        String format = (String) request.getInput("format");
        response = new ChunkedResponse(format);
        if (dontChunk || context.doNotChunk || request.hasInput("nochunk"))
            response.turnOffChunking();
        WikiPagePath path = getWikiPagePath(request);
        WikiPage page = context.root.getPageCrawler().getPage(context.root, path);
        if (page == null && shouldRespondWith404())
            return pageNotFoundResponse(context, request);

        Thread respondingThread = new Thread(new RespondingRunnable(context, context.root, path, page), getClass() + ": Responding Thread");
        respondingThread.start();

        return response;
    }

    public static WikiPagePath getWikiPagePath(Request request) {
        return PathParser.parse(request.getResource());
    }

    public void turnOffChunking() {
        dontChunk = true;
    }

    private Response pageNotFoundResponse(FitNesseContext context, Request request) throws Exception {
        return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);
    }

    protected boolean shouldRespondWith404() {
        return true;
    }

    private void startSending(FitNesseContext context, WikiPage root, WikiPagePath path, WikiPage page) {
        try {
            doSending(context, root, path, page);
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
        private final WikiPage root;
        private final WikiPagePath path;
        private final WikiPage page;

        public RespondingRunnable(FitNesseContext context, WikiPage root, WikiPagePath path, WikiPage page) {
            this.context = context;
            this.root = root;
            this.path = path;
            this.page = page;
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
            startSending(this.context, this.root, this.path, this.page);
        }
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    protected abstract void doSending(FitNesseContext context, WikiPage root, WikiPagePath path, WikiPage page) throws Exception;
}
