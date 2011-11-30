// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;


import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.wiki.*;

import java.io.IOException;

public class MovePageResponder extends PageMovementResponder implements SecureResponder {

    private String newParentName;

    @Inject
    public MovePageResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE) WikiPage root) {
        super(htmlPageFactory, root);
    }

    @Override
    protected boolean getAndValidateNewParentPage(Request request, WikiPage root) throws IOException {
        PageCrawler crawler = root.getPageCrawler();

        newParentName = getNameOfNewParent(request);
        if (newParentName == null)
            return false;

        newParentPath = PathParser.parse(newParentName);
        newParentPage = crawler.getPage(root, newParentPath);

        return (newParentPage != null);
    }

    private static String getNameOfNewParent(Request request) {
        String newParentName = (String) request.getInput("newLocation");
        if (".".equals(newParentName)) {
            return "";
        }
        return newParentName;
    }

    @Override
    protected boolean getAndValidateRefactoringParameters(Request request) throws Exception {
        PageCrawler crawler = oldRefactoredPage.getPageCrawler();

        WikiPagePath pageToBeMovedPath = crawler.getFullPath(oldRefactoredPage);
        WikiPagePath newParentPath = crawler.getFullPath(newParentPage);

        return !pageToBeMovedPath.equals(newParentPath) &&
                !selfPage(pageToBeMovedPath, newParentPath) &&
                !pageIsAncestorOfNewParent(pageToBeMovedPath, newParentPath);
    }

    private boolean selfPage(WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) throws Exception {
        WikiPagePath originalParentPath = pageToBeMovedPath.parentPath();
        return originalParentPath.equals(newParentPath);
    }

    boolean pageIsAncestorOfNewParent(WikiPagePath pageToBeMovedPath, WikiPagePath newParentPath) throws Exception {
        return newParentPath.startsWith(pageToBeMovedPath);
    }

    @Override
    protected ReferenceRenamer getReferenceRenamer(WikiPage root) {
        return new MovedPageReferenceRenamer(root, oldRefactoredPage, newParentName);
    }

    @Override
    protected void execute() throws Exception {
        final WikiPage newPage = newParentPage.addChildPage(getNewPageName());
        movePage(oldRefactoredPage, newPage);
    }

    @Override
    protected String getNewPageName() {
        return oldRefactoredPage.getName();
    }

    @Override
    protected String getErrorMessageHeader() throws Exception {
        return "Cannot move " + makeLink(oldNameOfPageToBeMoved) + " below " + newParentName;
    }

}
