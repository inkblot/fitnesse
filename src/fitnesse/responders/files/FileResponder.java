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
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;

public class FileResponder implements Responder {
    private static final FileNameMap fileNameMap = URLConnection.getFileNameMap();

    private final String rootPath;
    private final Clock clock;

    private Date lastModifiedDate;
    private String lastModifiedDateString;

    @Inject
    public FileResponder(@Named(FitNesseContext.ROOT_PAGE_PATH) String rootPagePath, Clock clock) {
        this.rootPath = rootPagePath;
        this.clock = clock;
    }

    public static Responder makeResponder(Injector injector, String resource, String rootPath) {
        File requestedFile = getRequestedFile(rootPath, resource);
        if (requestedFile.exists()) {
            // serve the file or directory if it exists
            if (requestedFile.isDirectory())
                return injector.getInstance(DirectoryResponder.class);
            else
                return injector.getInstance(FileResponder.class);
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
            File requestedFile = getRequestedFile(rootPath, request.getResource());
            FileInputStream input = new FileInputStream(requestedFile);
            int size = (int) requestedFile.length();
            response.setBody(input, size);
            String contentType = getContentType(getFileName(request.getResource()));
            response.setContentType(contentType);
            response.setLastModifiedHeader(lastModifiedDateString);
        }
        return response;
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

    private boolean isNotModified(Request request) {
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

    private Response createNotModifiedResponse() {
        Response response = new SimpleResponse();
        response.setStatus(304);
        response.addHeader("Date", SimpleResponse.makeStandardHttpDateFormat().format(clock.currentClockDate()));
        response.addHeader("Cache-Control", "private");
        response.setLastModifiedHeader(lastModifiedDateString);
        return response;
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
