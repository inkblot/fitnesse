// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.components.RecentChanges;
import fitnesse.components.SaveRecorder;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import util.Clock;

public class SaveResponder implements SecureResponder {
    private final Provider<ContentFilter> contentFilterProvider;
    private final HtmlPageFactory htmlPageFactory;
    private final Clock clock;
    private final WikiPage root;

    private String user;
    private long ticketId;
    private String savedContent;
    private PageData data;
    private long editTimeStamp;

    @Inject
    public SaveResponder(Provider<ContentFilter> contentFilterProvider, HtmlPageFactory htmlPageFactory, Clock clock, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.contentFilterProvider = contentFilterProvider;
        this.htmlPageFactory = htmlPageFactory;
        this.clock = clock;
        this.root = root;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        editTimeStamp = getEditTime(request);
        ticketId = getTicketId(request);
        String resource = request.getResource();
        WikiPage page = getPage(resource, root);
        data = page.getData();
        user = request.getAuthorizationUsername();

        if (editsNeedMerge()) {
            return new MergeResponder(request, htmlPageFactory, clock, root).makeResponse(context, request);
        }
        else {
            savedContent = (String) request.getInput(EditResponder.CONTENT_INPUT_NAME);
            if (!contentFilterProvider.get().isContentAcceptable(savedContent, resource))
                return makeBannedContentResponse(resource);
            else
                return saveEdits(request, page);
        }
    }

    private Response makeBannedContentResponse(String resource) throws Exception {
        SimpleResponse response = new SimpleResponse();
        HtmlPage html = htmlPageFactory.newPage();
        html.title.use("Edit " + resource);
        html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource, "Banned Content"));
        html.main.use(new HtmlTag("h3", "The content you're trying to save has been " +
                "banned from this site.  Your changes will not be saved!"));
        response.setContent(html.html());
        return response;
    }

    private Response saveEdits(Request request, WikiPage page) throws Exception {
        Response response = new SimpleResponse();
        setData();
        VersionInfo commitRecord = page.commit(data);
        response.addHeader("Previous-Version", commitRecord.getName());
        RecentChanges.updateRecentChanges(data);

        if (request.hasInput("redirect"))
            response.redirect(request.getInput("redirect").toString());
        else
            response.redirect(request.getResource());

        return response;
    }

    private boolean editsNeedMerge() throws Exception {
        return SaveRecorder.changesShouldBeMerged(editTimeStamp, ticketId, data);
    }

    private long getTicketId(Request request) {
        if (!request.hasInput(EditResponder.TICKET_ID))
            return 0;
        String ticketIdString = (String) request.getInput(EditResponder.TICKET_ID);
        return Long.parseLong(ticketIdString);
    }

    private long getEditTime(Request request) {
        if (!request.hasInput(EditResponder.TIME_STAMP))
            return 0;
        String editTimeStampString = (String) request.getInput(EditResponder.TIME_STAMP);
        return Long.parseLong(editTimeStampString);
    }

    private WikiPage getPage(String resource, WikiPage root) throws Exception {
        WikiPagePath path = PathParser.parse(resource);
        PageCrawler pageCrawler = root.getPageCrawler();
        WikiPage page = pageCrawler.getPage(root, path);
        if (page == null)
            page = pageCrawler.addPage(root, PathParser.parse(resource));
        return page;
    }

    private void setData() throws Exception {
        data.setContent(savedContent);
        SaveRecorder.pageSaved(data, ticketId, clock);
        if (user != null)
            data.setAttribute(PageData.LAST_MODIFYING_USER, user);
        else
            data.removeAttribute(PageData.LAST_MODIFYING_USER);
    }

    public SecureOperation getSecureOperation() {
        return new SecureWriteOperation();
    }
}
