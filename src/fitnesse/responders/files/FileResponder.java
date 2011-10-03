// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.InputStreamResponse;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.NotFoundResponder;
import util.ImpossibleException;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.text.ParseException;
import java.util.Date;

public class FileResponder implements Responder {
    private static FileNameMap fileNameMap = URLConnection.getFileNameMap();
    public String resource;
    public File requestedFile;
    public Date lastModifiedDate;
    public String lastModifiedDateString;

    public static Responder makeResponder(String resource, String rootPath, HtmlPageFactory htmlPageFactory) {
        if (fileNameHasSpaces(resource))
            resource = restoreRealSpacesInFileName(resource);

        File requestedFile = new File(rootPath + "/" + resource);
        if (!requestedFile.exists())
            return new NotFoundResponder(htmlPageFactory);

        if (requestedFile.isDirectory())
            return new DirectoryResponder(resource, requestedFile, htmlPageFactory);
        else
            return new FileResponder(resource, requestedFile);
    }

    public FileResponder(String resource, File requestedFile) {
        this.resource = resource;
        this.requestedFile = requestedFile;
    }

    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
        InputStreamResponse response = new InputStreamResponse();
        determineLastModifiedInfo();

        if (isNotModified(request))
            return createNotModifiedResponse(context);
        else {
            response.setBody(requestedFile);
            setContentType(requestedFile, response);
            response.setLastModifiedHeader(lastModifiedDateString);
        }
        return response;
    }

    public static boolean fileNameHasSpaces(String resource) {
        return resource.indexOf("%20") != 0;
    }

    public static String restoreRealSpacesInFileName(String resource) {
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

    private Response createNotModifiedResponse(FitNesseContext context) {
        Response response = new SimpleResponse();
        response.setStatus(304);
        response.addHeader("Date", SimpleResponse.makeStandardHttpDateFormat().format(context.getClock().currentClockDate()));
        response.addHeader("Cache-Control", "private");
        response.setLastModifiedHeader(lastModifiedDateString);
        return response;
    }

    private void determineLastModifiedInfo() {
        lastModifiedDate = new Date(requestedFile.lastModified());
        lastModifiedDateString = SimpleResponse.makeStandardHttpDateFormat().format(lastModifiedDate);

        try  // remove milliseconds
        {
            lastModifiedDate = SimpleResponse.makeStandardHttpDateFormat().parse(lastModifiedDateString);
        } catch (java.text.ParseException e) {
            e.printStackTrace();
        }
    }

    private void setContentType(File file, Response response) {
        String contentType = getContentType(file.getName());
        response.setContentType(contentType);
    }

    public static String getContentType(String filename) {
        String contentType = fileNameMap.getContentTypeFor(filename);
        if (contentType == null) {
            if (filename.endsWith(".css"))
                contentType = "text/css";
            else if (filename.endsWith(".jar"))
                contentType = "application/x-java-archive";
            else
                contentType = "text/plain";
        }
        return contentType;
    }
}
