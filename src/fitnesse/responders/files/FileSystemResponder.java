// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.files;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.http.Request;
import util.Clock;
import util.StringUtil;

import java.io.*;
import java.util.Calendar;
import java.util.Date;

public class FileSystemResponder extends FileResponder {

    private final String rootPath;

    @Inject
    public FileSystemResponder(@Named(FitNesseContext.ROOT_PAGE_PATH) String rootPagePath, Clock clock) {
        super(clock);
        this.rootPath = rootPagePath;
    }

    public Integer getContentLength(Request request) {
        return (int) getFile(request).length();
    }

    public FileInputStream getFileStream(Request request) throws IOException {
        return new FileInputStream(getFile(request));
    }

    private File getFile(Request request) {
        return new File(rootPath + File.separator + StringUtil.decodeURLText(request.getResource()));
    }

    public Date getLastModifiedDate(Request request) {
        Calendar lastModified = Calendar.getInstance();
        lastModified.setTime(new Date(getFile(request).lastModified()));
        // remove milliseconds
        lastModified.set(Calendar.MILLISECOND, 0);
        return lastModified.getTime();
    }

}
