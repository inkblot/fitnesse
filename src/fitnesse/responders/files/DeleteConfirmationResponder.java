// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.html.*;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.WikiModule;

import java.io.File;

public class DeleteConfirmationResponder implements SecureResponder {
    private String resource;
    private final HtmlPageFactory htmlPageFactory;
    private final String rootPagePath;

    @Inject
    public DeleteConfirmationResponder(HtmlPageFactory htmlPageFactory, @Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.htmlPageFactory = htmlPageFactory;
        this.rootPagePath = rootPagePath;
    }

    public Response makeResponse(Request request) {
        SimpleResponse response = new SimpleResponse();
        resource = request.getResource();
        String filename = (String) request.getInput("filename");
        response.setContent(makeDirectoryListingPage(filename));
        response.setLastModifiedHeader("Delete");
        return response;
    }

    private String makeDirectoryListingPage(String filename) {
        HtmlPage page = htmlPageFactory.newPage();
        page.title.use("Delete File(s): ");
        page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(resource + filename, "/", "Delete File"));
        page.main.use(makeConfirmationHTML(filename));

        return page.html();
    }

    private HtmlTag makeConfirmationHTML(String filename) {
        String path = rootPagePath + "/" + resource + filename;
        File file = new File(path);
        boolean isDir = file.isDirectory();

        TagGroup group = new TagGroup();
        group.add(messageText(filename, isDir, file));

        group.add(HtmlUtil.BR);
        group.add(HtmlUtil.BR);
        group.add(HtmlUtil.BR);
        group.add(makeYesForm(filename));
        group.add(makeNoForm());
        group.add(HtmlUtil.NBSP);
        group.add(HtmlUtil.NBSP);
        return group;
    }

    private String messageText(String filename, boolean dir, File file) {
        String message = "Are you sure you would like to delete <b>" + filename + "</b> ";
        if (dir)
            message += " and all " + file.listFiles().length + " files inside";

        return message + "?";
    }

    private HtmlTag makeNoForm() {
        HtmlTag noForm = HtmlUtil.makeFormTag("get", "/" + resource);
        HtmlTag noButton = HtmlUtil.makeInputTag("submit", "", "No");
        noButton.addAttribute("accesskey", "n");
        noForm.add(noButton);
        return noForm;
    }

    private HtmlTag makeYesForm(String filename) {
        HtmlTag yesForm = HtmlUtil.makeFormTag("get", "/" + resource);
        HtmlTag yesButton = HtmlUtil.makeInputTag("submit", "", "Yes");
        yesButton.addAttribute("accesskey", "y");
        yesForm.add(yesButton);
        yesForm.add(HtmlUtil.makeInputTag("hidden", "responder", "deleteFile"));
        yesForm.add(HtmlUtil.makeInputTag("hidden", "filename", filename));
        return yesForm;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
