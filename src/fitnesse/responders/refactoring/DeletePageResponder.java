// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;

import java.util.List;

public class DeletePageResponder implements SecureResponder {
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    @Inject
    public DeletePageResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    public Response makeResponse(final Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();
        String qualifiedPageName = request.getResource();
        WikiPagePath path = PathParser.parse(qualifiedPageName);

        if ("FrontPage".equals(qualifiedPageName)) {
            response.redirect("FrontPage");
            return response;
        }

        String confirmedString = (String) request.getInput("confirmed");
        if (!"yes".equals(confirmedString)) {
            response.setContent(buildConfirmationHtml(root, qualifiedPageName));
            return response;
        }

        String nameOfPageToBeDeleted = path.last();
        path.removeNameFromEnd();
        WikiPage parentOfPageToBeDeleted = root.getPageCrawler().getPage(root, path);
        if (parentOfPageToBeDeleted != null) {
            parentOfPageToBeDeleted.removeChildPage(nameOfPageToBeDeleted);
        }
        redirect(path, response);

        return response;
    }

    private void redirect(final WikiPagePath path, final SimpleResponse response) {
        String location = PathParser.render(path);
        if (location == null || location.length() == 0) {
            response.redirect("root");
        } else {
            response.redirect(location);
        }
    }

    private String buildConfirmationHtml(final WikiPage root, final String qualifiedPageName) throws Exception {
        HtmlPage html = htmlPageFactory.newPage();
        html.title.use("Delete Confirmation");
        html.header.use(HtmlUtil.makeBreadCrumbsWithPageType(qualifiedPageName, "/", "Confirm Deletion"));
        html.main.use(makeMainContent(root, qualifiedPageName));
        return html.html();
    }

    private String makeMainContent(final WikiPage root, final String qualifiedPageName) throws Exception {
        WikiPagePath path = PathParser.parse(qualifiedPageName);
        WikiPage pageToDelete = root.getPageCrawler().getPage(root, path);
        List<WikiPage> children = pageToDelete.getChildren();
        boolean addSubPageWarning = true;
        if (children == null || children.isEmpty()) {
            addSubPageWarning = false;
        }

        HtmlTag divTag = HtmlUtil.makeDivTag("centered");
        divTag.add(makeHeadingTag(addSubPageWarning, qualifiedPageName));
        divTag.add(HtmlUtil.BR);
        divTag.add(new RawHtml("<center><table class = \"confirmation-form\"><tr><td class = \"confirmation-form\">"));
        HtmlTag deletePageYesForm = HtmlUtil.makeFormTag("POST", qualifiedPageName + "?responder=deletePage&confirmed=yes", "deletePageYesForm");
        HtmlTag submitYesButton = HtmlUtil.makeInputTag("submit", "deletePageYesSubmit", "Yes");
        deletePageYesForm.add(submitYesButton);
        divTag.add(deletePageYesForm.html());

        divTag.add(new RawHtml("</td><td class = \"confirmation-form\">"));

        HtmlTag deletePageNoForm = HtmlUtil.makeFormTag("POST", qualifiedPageName, "deletePageNoForm");
        HtmlTag submitNoButton = HtmlUtil.makeInputTag("submit", "deletePageYesSubmit", "No");
        deletePageNoForm.add(submitNoButton);
        divTag.add(deletePageNoForm.html());

        divTag.add(new RawHtml("</tr></table></center>"));
        divTag.add(HtmlUtil.BR);

        return divTag.html();
    }

    private HtmlTag makeHeadingTag(final boolean addSubPageWarning, final String qualifiedPageName) {
        HtmlTag h3Tag = new HtmlTag("H3");
        if (addSubPageWarning) {
            h3Tag.add("Warning, this page contains one or more subpages.");
            h3Tag.add(HtmlUtil.BR);
        }
        h3Tag.add("Are you sure you want to delete " + qualifiedPageName + "?");
        return h3Tag;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
