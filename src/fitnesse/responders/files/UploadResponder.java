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
import fitnesse.http.UploadedFile;
import fitnesse.wiki.WikiModule;
import org.apache.commons.io.IOUtils;
import util.FileUtil;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UploadResponder implements SecureResponder {
    private static final Pattern filenamePattern = Pattern.compile("([^/\\\\]*[/\\\\])*([^/\\\\]*)");

    private final String rootPagePath;

    @Inject
    public UploadResponder(@Named(WikiModule.ROOT_PAGE_PATH) String rootPagePath) {
        this.rootPagePath = rootPagePath;
    }

    public Response makeResponse(Request request) throws Exception {
        SimpleResponse response = new SimpleResponse();
        String resource = request.getResource().replace("%20", " ");
        UploadedFile uploadedFile = (UploadedFile) request.getInput("file");
        if (uploadedFile.isUsable()) {
            File file = makeFileToCreate(uploadedFile, resource);
            writeFile(file, uploadedFile);
        }

        response.redirect("/" + resource.replace(" ", "%20"));
        return response;
    }

    public void writeFile(File file, UploadedFile uploadedFile) throws Exception {
        boolean renamed = uploadedFile.getFile().renameTo(file);
        if (!renamed) {
            InputStream input = null;
            OutputStream output = null;
            try {
                input = new BufferedInputStream(new FileInputStream(uploadedFile.getFile()));
                output = new BufferedOutputStream(new FileOutputStream(file));
                FileUtil.copyBytes(input, output);
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);
                uploadedFile.delete();
            }
        }
    }

    private File makeFileToCreate(UploadedFile uploadedFile, String resource) {
        String relativeFilename = makeRelativeFilename(uploadedFile.getName());
        String filename = relativeFilename;
        int prefix = 1;
        File file = new File(makeFullFilename(resource, filename));
        while (file.exists()) {
            filename = makeNewFilename(relativeFilename, prefix++);
            file = new File(makeFullFilename(resource, filename));
        }
        return file;
    }

    private String makeFullFilename(String resource, String filename) {
        return this.rootPagePath + "/" + resource + filename;
    }

    public static String makeRelativeFilename(String name) {
        Matcher match = filenamePattern.matcher(name);
        if (match.find())
            return match.group(2);
        else
            return name;
    }

    public static String makeNewFilename(String filename, int copyId) {
        String[] parts = filename.split("\\.");

        if (parts.length == 1)
            return filename + "_copy" + copyId;
        else {
            String newName = "";
            for (int i = 0; i < parts.length - 1; i++) {
                if (i != 0)
                    newName += ".";
                newName += parts[i];
            }
            newName += "_copy" + copyId + "." + parts[parts.length - 1];
            return newName;
        }
    }

    public SecureOperation getSecureOperation() {
        return new AlwaysSecureOperation();
    }
}
