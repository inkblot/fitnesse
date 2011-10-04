// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;

import java.io.File;

public class RenameFileResponder implements SecureResponder {
    String newFilename;
    private final String rootPagePath;

    @Inject
    public RenameFileResponder(@Named(FitNesseContext.ROOT_PAGE_PATH) String rootPagePath) {
        this.rootPagePath = rootPagePath;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        Response response = new SimpleResponse();
        String resource = request.getResource();
        String filename = (String) request.getInput("filename");
        newFilename = (String) request.getInput("newName");
        newFilename = newFilename.trim();

        String path = rootPagePath + "/" + resource;
        File file = new File(path + filename);
        file.renameTo(new File(path + newFilename));
        response.redirect("/" + resource);
        return response;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
