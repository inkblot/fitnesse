package fitnesse.responders;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.authentication.SecureWriteOperation;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;
import fitnesse.wikitext.widgets.WikiWordWidget;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class AddChildPageResponder implements SecureResponder {
    private WikiPage currentPage;
    private PageCrawler crawler;
    private String childName;
    private WikiPagePath childPath;
    private String childContent;
    private String pageType;
    private final HtmlPageFactory htmlPageFactory;

    @Inject
    public AddChildPageResponder(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    public SecureOperation getSecureOperation() {
        return new SecureWriteOperation();
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        parseRequest(request, context.root);
        if (currentPage == null)
            return notFoundResponse(context, request);
        else if (nameIsInvalid(childName))
            return errorResponse(context, request);
        else {
            return createChildPageAndMakeResponse();
        }
    }

    private void parseRequest(Request request, WikiPage root) throws IOException {
        childName = (String) request.getInput("name");
        childName = childName == null ? "null" : childName;
        childPath = PathParser.parse(childName);
        WikiPagePath currentPagePath = PathParser.parse(request.getResource());
        crawler = root.getPageCrawler();
        currentPage = crawler.getPage(root, currentPagePath);
        childContent = (String) request.getInput("content");
        pageType = (String) request.getInput("pageType");
        if (childContent == null)
            childContent = "!contents\n";
        if (pageType == null)
            pageType = "Default";
    }

    private Response createChildPageAndMakeResponse() throws IOException {
        createChildPage();
        SimpleResponse response = new SimpleResponse();
        WikiPagePath fullPathOfCurrentPage = crawler.getFullPath(currentPage);
        response.redirect(fullPathOfCurrentPage.toString());
        return response;
    }

    private boolean nameIsInvalid(String name) {
        return isEmpty(name) || !WikiWordWidget.isSingleWikiWord(name);
    }

    private void createChildPage() throws IOException {
        WikiPage childPage = crawler.addPage(currentPage, childPath, childContent);
        setTestAndSuiteAttributes(childPage);
    }

    private void setTestAndSuiteAttributes(WikiPage childPage) throws IOException {
        PageData childPageData = childPage.getData();
        if (pageType.equals("Normal")) {
            childPageData.getProperties().remove("Test");
            childPageData.getProperties().remove("Suite");
        } else if ("Test".equals(pageType) || "Suite".equals(pageType))
            childPageData.setAttribute(pageType);
        childPage.commit(childPageData);
    }

    private Response errorResponse(FitNesseContext context, Request request) throws Exception {
        return new ErrorResponder("Invalid Child Name", htmlPageFactory).makeResponse(context, request);
    }

    private Response notFoundResponse(FitNesseContext context, Request request) throws Exception {
        return new NotFoundResponder(htmlPageFactory).makeResponse(context, request);
    }
}

