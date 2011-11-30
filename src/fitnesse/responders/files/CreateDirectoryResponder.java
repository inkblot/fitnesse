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

import java.io.File;

public class CreateDirectoryResponder implements SecureResponder {
    private final String rootPagePath;

    @Inject
    public CreateDirectoryResponder(@Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.rootPagePath = rootPagePath;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();

        String resource = request.getResource();
        String dirname = (String) request.getInput("dirname");
        String path = rootPagePath + "/" + resource + dirname;
        File file = new File(path);
        if (!file.exists())
            file.mkdir();

        response.redirect("/" + resource);
        return response;
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
