// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.search;

import fitnesse.FitNesseContext;
import fitnesse.VelocityFactory;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.SearchObserver;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ChunkingResponder;
import fitnesse.responders.templateUtilities.PageTitle;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;

import java.io.IOException;
import java.io.StringWriter;

import static org.apache.commons.lang.StringUtils.isEmpty;

public abstract class ResultResponder extends ChunkingResponder implements
        SearchObserver, SecureResponder {
    private int hits;

    public ResultResponder(HtmlPageFactory htmlPageFactory, WikiPage root, FitNesseContext context) {
        super(htmlPageFactory, root, context);
    }

    @Override
    protected void doSending(FitNesseContext context, WikiPage root, WikiPagePath path, WikiPage page) throws Exception {
        response.add(createSearchResultsHeader());

        startSearching(root, page);

        response.add(createSearchResultsFooter(root, page));
        response.closeAll();
    }

    private String createSearchResultsFooter(WikiPage root, WikiPage page) throws Exception {
        VelocityContext velocityContext = new VelocityContext();

        StringWriter writer = new StringWriter();

        Template template = VelocityFactory.getVelocityEngine().getTemplate(
                "fitnesse/templates/searchResultsFooter.vm");
        if (page == null)
            page = root.getPageCrawler().getPage(root, PathParser.parse("FrontPage"));
        velocityContext.put("hits", hits);
        if (isEmpty(request.getQueryString()))
            velocityContext.put("request", request.getBody());
        else
            velocityContext.put("request", request.getQueryString());
        velocityContext.put("page", page);

        template.merge(velocityContext, writer);

        return writer.toString();
    }

    private String createSearchResultsHeader() throws Exception {
        VelocityContext velocityContext = new VelocityContext();

        StringWriter writer = new StringWriter();

        Template template = VelocityFactory.getVelocityEngine().getTemplate(
                "fitnesse/templates/searchResultsHeader.vm");

        velocityContext.put("page_title", getTitle());
        velocityContext.put("pageTitle", new PageTitle(getTitle()) {
            @Override
            public String getTitle() {
                return "search";
            }

            @Override
            public String getLink() {
                return "search";
            }
        });

        template.merge(velocityContext, writer);

        return writer.toString();
    }

    public static String getDateFormatJavascriptRegex() {
        return "/^(\\w+) (jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec) (\\d+) (\\d+).(\\d+).(\\d+) (\\w+) (\\d+)$/";
    }

    @Override
    public void hit(WikiPage page) throws IOException {
        hits++;
        response.add(createSearchResultsEntry(page));
    }

    private String createSearchResultsEntry(WikiPage result) throws IOException {
        VelocityContext velocityContext = new VelocityContext();

        StringWriter writer = new StringWriter();

        try {
            Template template = VelocityFactory.getVelocityEngine().getTemplate("fitnesse/templates/searchResultsEntry.vm");

            velocityContext.put("resultsRow", getRow());
            velocityContext.put("result", result);

            template.merge(velocityContext, writer);

            return writer.toString();
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private int nextRow = 0;

    private int getRow() {
        return (nextRow++ % 2) + 1;
    }

    protected abstract String getTitle();

    protected void startSearching(WikiPage root, WikiPage page) throws IOException {
        hits = 0;
    }

    @Override
    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
