// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fit.FitProtocol;
import fitnesse.FitNesseModule;
import fitnesse.Responder;
import fitnesse.components.FitClient;
import fitnesse.html.SetupTeardownAndLibraryIncluder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.ResponseSender;
import fitnesse.wiki.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

public class FitClientResponder implements Responder, ResponsePuppeteer, TestSystemListener {

    private final WikiPage root;
    private final Integer port;
    private final SocketDealer socketDealer;
    private final RunningTestingTracker runningTestingTracker;
    private final Injector injector;

    private PageCrawler crawler;
    private String resource;
    private WikiPage page;
    private boolean shouldIncludePaths;
    private String suiteFilter;

    @Inject
    public FitClientResponder(@Named(WikiModule.ROOT_PAGE) WikiPage root, @Named(FitNesseModule.PORT) Integer port, SocketDealer socketDealer, RunningTestingTracker runningTestingTracker, Injector injector) {
        this.root = root;
        this.port = port;
        this.socketDealer = socketDealer;
        this.runningTestingTracker = runningTestingTracker;
        this.injector = injector;
    }

    public Response makeResponse(Request request) {
        crawler = root.getPageCrawler();
        resource = request.getResource();
        shouldIncludePaths = request.hasInput("includePaths");
        suiteFilter = (String) request.getInput("suiteFilter");
        return new PuppetResponse(this);
    }

    public void readyToSend(ResponseSender sender) throws IOException {
        WikiPagePath pagePath = PathParser.parse(resource);
        OutputStream output = sender.getOutputStream();
        if (!crawler.pageExists(root, pagePath))
            FitProtocol.writeData(notFoundMessage(), output);
        else {
            page = crawler.getPage(root, pagePath);
            PageData data = page.getData();

            InputStream input = sender.getInputStream();
            if (data.hasAttribute("Suite"))
                handleSuitePage(input, output, page, root);
            else if (data.hasAttribute("Test"))
                handleTestPage(input, output, data);
            else
                FitProtocol.writeData(notATestMessage(), output);
        }
        sender.close();
    }

    private void handleTestPage(InputStream input, OutputStream output, PageData data) throws IOException {
        FitClient client = startClient(input, output);

        if (shouldIncludePaths) {
            String classpath = new ClassPathBuilder().getClasspath(page);
            client.send(classpath);
        }

        sendPage(data, client, true);
        closeClient(client);
    }

    private void handleSuitePage(InputStream input, OutputStream output, WikiPage page, WikiPage root) throws IOException {
        FitClient client = startClient(input, output);
        SuiteFilter filter = new SuiteFilter(suiteFilter, null, null);
        SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
        List<WikiPage> testPages = suiteTestFinder.makePageList();

        if (shouldIncludePaths) {
            MultipleTestsRunner runner = new MultipleTestsRunner(testPages, runningTestingTracker, page, null, root, port, socketDealer, injector);
            String classpath = runner.buildClassPath();
            client.send(classpath);
        }

        for (WikiPage testPage : testPages) {
            PageData testPageData = testPage.getData();
            sendPage(testPageData, client, false);
        }
        closeClient(client);
    }

    private void sendPage(PageData data, FitClient client, boolean includeSuiteSetup) throws IOException {
        String pageName = crawler.getRelativeName(page, data.getWikiPage());
        SetupTeardownAndLibraryIncluder.includeInto(data, includeSuiteSetup);
        String testableHtml = data.getHtml();
        String sendableHtml = pageName + "\n" + testableHtml;
        client.send(sendableHtml);
    }

    private void closeClient(FitClient client) throws IOException {
        client.done();
        try {
            client.join();
        } catch (InterruptedException e) {
            // ok, done waiting
        }
    }

    private FitClient startClient(InputStream input, OutputStream output) throws IOException {
        FitClient client = new FitClient(this);
        client.acceptSocket(input, output);
        return client;
    }

    private String notATestMessage() {
        return resource + " is neither a Test page nor a Suite page.";
    }

    private String notFoundMessage() {
        return "The page " + resource + " was not found.";
    }

    public void acceptOutputFirst(String output) {
    }

    public void testComplete(TestSummary testSummary) {
    }

    public void exceptionOccurred(Throwable e) {
    }
}
