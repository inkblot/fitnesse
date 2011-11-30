// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.authentication.SecureOperation;
import fitnesse.authentication.SecureResponder;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.WikiModule;
import util.FileUtil;

import java.io.File;

public class DeleteFileResponder implements SecureResponder {
    public String resource;
    private final String rootPagePath;

    @Inject
    public DeleteFileResponder(@Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.rootPagePath = rootPagePath;
    }

    public Response makeResponse(Request request) throws Exception {
        Response response = new SimpleResponse();
        resource = request.getResource();
        String filename = (String) request.getInput("filename");
        String path = rootPagePath + "/" + resource + filename;
        File file = new File(path);

        if (file.isDirectory())
            FileUtil.deleteFileSystemDirectory(file);
        else
            file.delete();

        response.redirect("/" + resource);
        return response;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
