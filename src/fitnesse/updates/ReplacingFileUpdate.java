// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import org.apache.commons.io.IOUtils;
import util.ImpossibleException;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ReplacingFileUpdate extends FileUpdate {
    public ReplacingFileUpdate(String rootDirectory, String source, String destination) {
        super(rootDirectory, source, destination);
    }

    public boolean shouldBeApplied() throws IOException {
        return super.shouldBeApplied() || !filesMatch();
    }

    private boolean filesMatch() throws IOException {
        String classPathSum = md5sum(getResource(source).openStream());
        String fsSum = md5sum(new FileInputStream(destinationFile()));
        return classPathSum.equals(fsSum);
    }

    private String md5sum(InputStream input) throws IOException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new ImpossibleException("MD5 is a supported message digest", e);
        }
        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = input.read(buffer)) > 0) {
                digest.update(buffer, 0 , read);
            }
        } finally {
            IOUtils.closeQuietly(input);
        }
        return new BigInteger(1, digest.digest()).toString(16);
    }
}
