// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.refactoring;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class RenamePageResponder extends PageMovementResponder {
    private String newName;

    @Inject
    public RenamePageResponder(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        super(htmlPageFactory, root);
    }

    @Override
    protected boolean getAndValidateNewParentPage(Request request, WikiPage root) {
        newParentPath = PathParser.parse(oldNameOfPageToBeMoved).parentPath();
        newParentPage = oldRefactoredPage.getParent();
        return (newParentPage != null);
    }

    @Override
    protected boolean getAndValidateRefactoringParameters(Request request) throws Exception {
        newName = (String) request.getInput("newName");
        return (newName != null && WikiWordWidget.isSingleWikiWord(newName) && !"FrontPage".equals(oldNameOfPageToBeMoved));
    }

    @Override
    protected ReferenceRenamer getReferenceRenamer(WikiPage root) {
        return new PageReferenceRenamer(root, oldRefactoredPage, getNewPageName());
    }

    @Override
    protected void execute() throws Exception {
        WikiPage parentOfPageToRename = oldRefactoredPage.getParent();

        WikiPage renamedPage = parentOfPageToRename.addChildPage(newName);

        movePage(oldRefactoredPage, renamedPage);
    }

    @Override
    protected String getNewPageName() {
        return newName;
    }

    @Override
    protected String getErrorMessageHeader() throws Exception {
        return "Cannot rename " + makeLink(oldNameOfPageToBeMoved) + " to " + newName;
    }
}
