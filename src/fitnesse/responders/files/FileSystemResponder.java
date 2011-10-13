// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import util.Clock;
import util.ImpossibleException;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;

public class FileSystemResponder extends FileResponder {
    private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();

    private final String rootPath;

    private Date lastModifiedDate;

    @Inject
    public FileSystemResponder(@Named(FitNesseContext.ROOT_PAGE_PATH) String rootPagePath, Clock clock) {
        super(clock);
        this.rootPath = rootPagePath;
    }

    public static Responder makeResponder(Injector injector, String resource, String rootPath) {
        File requestedFile = getRequestedFile(rootPath, resource);
        if (requestedFile.exists()) {
            // serve the file or directory if it exists
            if (requestedFile.isDirectory())
                return injector.getInstance(DirectoryResponder.class);
            else
                return injector.getInstance(FileSystemResponder.class);
        } else {
            // it doesn't exist
            return injector.getInstance(NotFoundResponder.class);
        }
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        InputStreamResponse response = new InputStreamResponse();
        determineLastModifiedInfo(request.getResource());

        if (isNotModified(request))
            return createNotModifiedResponse();
        else {
            response.setBody(getResponseStream(request), getContentLength(request));
            String contentType = getContentType(getFileName(request.getResource()));
            response.setContentType(contentType);
            response.setLastModifiedHeader(lastModifiedDateString);
        }
        return response;
    }

    public int getContentLength(Request request) {
        return (int) getFile(request).length();
    }

    public FileInputStream getResponseStream(Request request) throws FileNotFoundException {
        return new FileInputStream(getFile(request));
    }

    private File getFile(Request request) {
        return getRequestedFile(rootPath, request.getResource());
    }

    private static File getRequestedFile(String rootPath, String resource) {
        return new File(rootPath + File.separator + decodeFileName(resource));
    }

    public static String decodeFileName(String resource) {
        try {
            return URLDecoder.decode(resource, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
    }

    public boolean isNotModified(Request request) {
        if (request.hasHeader("If-Modified-Since")) {
            String queryDateString = (String) request.getHeader("If-Modified-Since");
            try {
                Date queryDate = SimpleResponse.makeStandardHttpDateFormat().parse(queryDateString);
                if (!queryDate.before(lastModifiedDate))
                    return true;
            } catch (ParseException e) {
                //Some browsers use local date formats that we can't parse.
                //So just ignore this exception if we can't parse the date.
            }
        }
        return false;
    }

    private void determineLastModifiedInfo(String resource) {
        lastModifiedDate = new Date(getRequestedFile(rootPath, resource).lastModified());
        lastModifiedDateString = SimpleResponse.makeStandardHttpDateFormat().format(lastModifiedDate);

        try  // remove milliseconds
        {
            lastModifiedDate = SimpleResponse.makeStandardHttpDateFormat().parse(lastModifiedDateString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    public static String getFileName(String resource) {
        return resource.substring(resource.lastIndexOf(File.separator) + 1);
    }

    public static String getContentType(String filename) {
        if (fileNameMap.getContentTypeFor(filename) != null)
            return fileNameMap.getContentTypeFor(filename);
        else if (filename.endsWith(".css"))
            return "text/css";
        else if (filename.endsWith(".jar"))
            return "application/x-java-archive";
        else
            return "text/plain";
    }
}
