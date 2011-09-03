// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import fit.FitProtocol;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.components.ClassPathBuilder;
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
    private FitNesseContext context;
    private PageCrawler crawler;
    private String resource;
    private WikiPage page;
    private boolean shouldIncludePaths;
    private String suiteFilter;

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        this.context = context;
        crawler = context.root.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        resource = request.getResource();
        shouldIncludePaths = request.hasInput("includePaths");
        suiteFilter = (String) request.getInput("suiteFilter");
        return new PuppetResponse(this);
    }

    public void readyToSend(ResponseSender sender) throws Exception {
        WikiPagePath pagePath = PathParser.parse(resource);
        OutputStream output = sender.getOutputStream();
        if (!crawler.pageExists(context.root, pagePath))
            FitProtocol.writeData(notFoundMessage(), output);
        else {
            page = crawler.getPage(context.root, pagePath);
            PageData data = page.getData();

            InputStream input = sender.getInputStream();
            if (data.hasAttribute("Suite"))
                handleSuitePage(input, output, page, context.root);
            else if (data.hasAttribute("Test"))
                handleTestPage(input, output, data);
            else
                FitProtocol.writeData(notATestMessage(), output);
        }
        sender.close();
    }

    private void handleTestPage(InputStream input, OutputStream output, PageData data) throws Exception {
        FitClient client = startClient(input, output);

        if (shouldIncludePaths) {
            String classpath = new ClassPathBuilder().getClasspath(page);
            client.send(classpath);
        }

        sendPage(data, client, true);
        closeClient(client);
    }

    private void handleSuitePage(InputStream input, OutputStream output, WikiPage page, WikiPage root) throws Exception {
        FitClient client = startClient(input, output);
        SuiteFilter filter = new SuiteFilter(suiteFilter, null, null);
        SuiteContentsFinder suiteTestFinder = new SuiteContentsFinder(page, filter, root);
        List<WikiPage> testPages = suiteTestFinder.makePageList();

        if (shouldIncludePaths) {
            MultipleTestsRunner runner = new MultipleTestsRunner(testPages, context, page, null);
            String classpath = runner.buildClassPath();
            client.send(classpath);
        }

        for (WikiPage testPage : testPages) {
            PageData testPageData = testPage.getData();
            sendPage(testPageData, client, false);
        }
        closeClient(client);
    }

    private void sendPage(PageData data, FitClient client, boolean includeSuiteSetup) throws Exception {
        String pageName = crawler.getRelativeName(page, data.getWikiPage());
        SetupTeardownAndLibraryIncluder.includeInto(data, includeSuiteSetup);
        String testableHtml = data.getHtml();
        String sendableHtml = pageName + "\n" + testableHtml;
        client.send(sendableHtml);
    }

    private void closeClient(FitClient client) throws Exception {
        client.done();
        client.join();
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

    public void acceptOutputFirst(String output) throws Exception {
    }

    public void testComplete(TestSummary testSummary) throws Exception {
    }

    public void exceptionOccurred(Throwable e) {
    }
}
