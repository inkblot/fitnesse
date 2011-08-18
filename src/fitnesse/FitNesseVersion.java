// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Properties;

public class FitNesseVersion {
    private final String version;

    private static Properties getBuildProperties() {
        Properties buildProperties = new Properties();
        InputStream propertyStream = null;
        Reader propertyReader = null;
        try {
            propertyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("fitnesse/build.properties");
            propertyReader = new InputStreamReader(propertyStream);
            buildProperties.load(propertyReader);
        } catch (NullPointerException e) {
            throw new RuntimeException("Could not load build.properties from the classpath.  Are you sure the jar is packaged correctly?", e);
        } catch (IOException e) {
            throw new RuntimeException("Could not load build.properties from the classpath.  Are you sure the jar is packaged correctly?", e);
        } finally {
            IOUtils.closeQuietly(propertyReader);
            IOUtils.closeQuietly(propertyStream);
        }
        return buildProperties;
    }

    public FitNesseVersion() {
        this(getBuildProperties().getProperty("build.version"));
    }

    public FitNesseVersion(String version) {
        this.version = version;
    }

    public String toString() {
        return version;
    }

    public boolean isAtLeast(String requiredVersion) {
        long thisVersion = Long.parseLong(version.substring(1));
        long otherVersion = Long.parseLong(requiredVersion.substring(1));
        return thisVersion >= otherVersion;
    }
}
