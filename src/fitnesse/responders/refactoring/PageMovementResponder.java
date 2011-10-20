package fitnesse.responders.refactoring;

import fitnesse.Responder;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.components.ReferenceRenamer;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ErrorResponder;
import fitnesse.responders.NotFoundResponder;
import fitnesse.wiki.*;

import java.io.IOException;
import java.util.List;

public abstract class PageMovementResponder implements SecureResponder {

    protected String oldNameOfPageToBeMoved;
    protected WikiPage oldRefactoredPage;
    protected WikiPage newParentPage;
    protected WikiPagePath newParentPath;
    private final HtmlPageFactory htmlPageFactory;
    private final WikiPage root;

    public PageMovementResponder(HtmlPageFactory htmlPageFactory, WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    protected abstract boolean getAndValidateNewParentPage(Request request, WikiPage root) throws IOException;

    protected abstract boolean getAndValidateRefactoringParameters(Request request) throws Exception;

    protected abstract ReferenceRenamer getReferenceRenamer(WikiPage root);

    protected abstract String getNewPageName() ;

    protected abstract String getErrorMessageHeader() throws Exception;

    protected abstract void execute() throws Exception;

    public Response makeResponse(Request request) throws Exception {
        if (!getAndValidateRefactoredPage(request, root)) {
            return new NotFoundResponder(htmlPageFactory).makeResponse(request);
        }

        if (!getAndValidateNewParentPage(request, root)) {
            return makeErrorMessageResponder(newParentPath == null ? "null" : newParentPath.toString() + " does not exist.").makeResponse(request);
        }

        if (!getAndValidateRefactoringParameters(request)) {
            return makeErrorMessageResponder("").makeResponse(request);
        }

        if (targetPageExists()) {
            return makeErrorMessageResponder(makeLink(getNewPageName()) + " already exists").makeResponse(request);
        }

        if (request.hasInput("refactorReferences")) {
            getReferenceRenamer(root).renameReferences();
        }
        execute();

        SimpleResponse response = new SimpleResponse();
        response.redirect(createRedirectionUrl(newParentPage, getNewPageName()));

        return response;
    }

    protected boolean getAndValidateRefactoredPage(Request request, WikiPage root) throws Exception {
        PageCrawler crawler = root.getPageCrawler();

        oldNameOfPageToBeMoved = request.getResource();

        WikiPagePath path = PathParser.parse(oldNameOfPageToBeMoved);
        oldRefactoredPage = crawler.getPage(root, path);
        return (oldRefactoredPage != null);
    }

    private Responder makeErrorMessageResponder(String message) throws Exception {
        return new ErrorResponder(getErrorMessageHeader() + "<br/>" + message, htmlPageFactory);
    }

    private boolean targetPageExists() throws Exception {
        return newParentPage.hasChildPage(getNewPageName());
    }

    protected String makeLink(String page) throws Exception {
        return HtmlUtil.makeLink(page, page).html();
    }

    protected String createRedirectionUrl(WikiPage newParent, String newName) throws Exception {
        PageCrawler crawler = newParent.getPageCrawler();
        if (crawler.isRoot(newParent)) {
            return newName;
        }
        return PathParser.render(crawler.getFullPath(newParent).addNameToEnd(newName));
    }

    protected void movePage(WikiPage movedPage, WikiPage targetPage) throws Exception {
        PageData pageData = movedPage.getData();

        targetPage.commit(pageData);

        moveChildren(movedPage, targetPage);

        WikiPage parentOfMovedPage = movedPage.getParent();
        parentOfMovedPage.removeChildPage(movedPage.getName());
    }

    protected void moveChildren(WikiPage movedPage, WikiPage newParentPage) throws Exception {
        List<WikiPage> children = movedPage.getChildren();
        for (WikiPage page : children) {
            movePage(page, newParentPage.addChildPage(page.getName()));
        }
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }

}