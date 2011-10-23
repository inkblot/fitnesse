// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;

public class FileUpdate implements Update {

    protected final String destination;
    protected final String source;
    protected final File destinationDir;
    protected final String rootDir;
    protected final String filename;

    public FileUpdate(String rootDirectory, String source, String destination) {
        URL url = getResource(source);
        if (url == null) throw new NullPointerException("source");
        this.destination = destination;
        this.source = source;
        rootDir = rootDirectory;
        destinationDir = new File(new File(rootDir), destination);

        filename = new File(source).getName();
    }

    public void doUpdate() throws IOException {
        destinationDir.mkdirs();

        InputStream input = null;
        OutputStream output = null;
        try {
            input = getResource(source).openStream();
            output = new FileOutputStream(destinationFile(), false);
            IOUtils.copy(input, output);
        } finally {
            IOUtils.closeQuietly(input);
            IOUtils.closeQuietly(output);
        }
    }

    protected URL getResource(String resource) {
        return ClassLoader.getSystemResource(resource);
    }

    public String getMessage() {
        return ".";
    }

    protected File destinationFile() {
        return new File(destinationDir, filename);
    }

    public String getName() {
        return "FileUpdate(" + filename + ")";
    }

    public boolean shouldBeApplied() throws IOException {
        return !destinationFile().exists();
    }
}
