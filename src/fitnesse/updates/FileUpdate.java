// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.regex.Pattern;

public class FileUpdate implements Update {

    protected String destination;
    protected String source;
    protected File destinationDir;
    protected String rootDir;
    protected String filename;

    public FileUpdate(String rootDirectory, String source, String destination) {
        this.destination = destination;
        this.source = source;
        rootDir = rootDirectory;
        destinationDir = new File(new File(rootDir), destination);

        filename = new File(source).getName();
    }

    public void doUpdate() throws IOException {
        makeSureDirectoriesExist();
        copyResource();
    }

    private void makeSureDirectoriesExist() {
        String[] subDirectories = destination.split(Pattern.quote(File.separator));
        String currentDirPath = rootDir;

        for (String subDirectory : subDirectories) {
            currentDirPath = currentDirPath + File.separator + subDirectory;
            File directory = new File(currentDirPath);
            directory.mkdir();
        }
    }

    private void copyResource() throws IOException {
        URL url = getResource(source);
        if (url == null) throw new NullPointerException("source");

        InputStream input = null;
        OutputStream output = null;
        try {
            input = url.openStream();
            output = new FileOutputStream(destinationFile());

            int b;
            while ((b = input.read()) != -1)
                output.write(b);
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
