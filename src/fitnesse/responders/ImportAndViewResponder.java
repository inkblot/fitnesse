// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureReadOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import fitnesse.wikitext.WikiImportProperty;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class ImportAndViewResponder implements SecureResponder, WikiImporterClient {
    private WikiPage page;
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public ImportAndViewResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(Request request) throws Exception {
        String resource = request.getResource();

        if (isEmpty(resource))
            resource = "FrontPage";

        loadPage(resource, root);
        if (page == null)
            return new NotFoundResponder(htmlPageFactory).makeResponse(request);
        loadPageData();

        SimpleResponse response = new SimpleResponse();
        response.redirect(resource);

        return response;
    }

    protected void loadPage(String resource, WikiPage root) throws Exception {
        WikiPagePath path = PathParser.parse(resource);
        PageCrawler crawler = root.getPageCrawler();
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        page = crawler.getPage(root, path);
    }

    protected void loadPageData() throws Exception {
        PageData pageData = page.getData();

        WikiImportProperty importProperty = WikiImportProperty.createFrom(pageData.getProperties());

        if (importProperty != null) {
            WikiImporter importer = new WikiImporter();
            importer.setWikiImporterClient(this);
            importer.parseUrl(importProperty.getSourceUrl());
            importer.importRemotePageContent(page);
        }
    }

    public void pageImported(WikiPage localPage) {
    }

    public void pageImportError(WikiPage localPage, Exception e) {
        e.printStackTrace();
    }

    public SecureOperation getSecureOperation() {
        return new SecureReadOperation();
    }
}
