// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Injector;
import fitnesse.wikitext.WikiWordUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Date;

import static org.apache.commons.lang.StringUtils.isEmpty;

public class FileSystemPage extends CachingPage {
    private static final Logger logger = LoggerFactory.getLogger(FileSystemPage.class);
    private static final long serialVersionUID = 1L;

    public static final String contentFilename = "/content.txt";
    public static final String propertiesFilename = "/properties.xml";

    private final String path;
    private final VersionsController versionsController;
    private final CmSystem cmSystem = new CmSystem();
    private transient final Clock clock;

    public static WikiPage makeRoot(Injector injector, String rootPath, String rootPageName) throws IOException {
        return new FileSystemPage(rootPath, rootPageName, injector.getInstance(FileSystem.class), injector.getInstance(VersionsController.class), injector, injector.getInstance(Clock.class));
    }

    private FileSystemPage(final String path, final String name, final FileSystem fileSystem, final VersionsController versionsController, Injector injector, Clock clock) throws IOException {
        super(name, null, injector);
        this.path = path;
        this.versionsController = versionsController;
        createDirectoryIfNewPage(fileSystem);
        this.clock = clock;
    }

    public FileSystemPage(final String name, final FileSystemPage parent, final FileSystem fileSystem, Clock clock, Injector injector) throws IOException {
        super(name, parent, injector);
        path = parent.getFileSystemPath();
        versionsController = parent.versionsController;
        createDirectoryIfNewPage(fileSystem);
        this.clock = clock;
    }

    @Override
    public void removeChildPage(final String name) {
        super.removeChildPage(name);
        String pathToDelete = getFileSystemPath() + "/" + name;
        final File fileToBeDeleted = new File(pathToDelete);
        cmSystem.preDelete(pathToDelete);
        FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
        cmSystem.delete(pathToDelete);
    }

    @Override
    public boolean hasChildPage(final String pageName) throws IOException {
        final File f = new File(getFileSystemPath() + "/" + pageName);
        if (f.exists()) {
            addChildPage(pageName);
            return true;
        }
        return false;
    }

    protected synchronized void saveContent(String content) throws IOException {
        if (content == null) {
            return;
        }

        final String separator = System.getProperty("line.separator");

        if (content.endsWith("|")) {
            content += separator;
        }

        //First replace every windows style to unix
        content = content.replaceAll("\r\n", "\n");
        //Then do the replace to match the OS.  This works around
        //a strange behavior on windows.
        content = content.replaceAll("\n", separator);

        String contentPath = getFileSystemPath() + contentFilename;
        final File output = new File(contentPath);
        OutputStreamWriter writer = null;
        try {
            if (output.exists())
                cmSystem.edit(contentPath);
            writer = new OutputStreamWriter(new FileOutputStream(output), "UTF-8");
            writer.write(content);
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        } finally {
            if (writer != null) {
                IOUtils.closeQuietly(writer);
                cmSystem.update(contentPath);
            }
        }
    }

    protected synchronized void saveAttributes(final WikiPageProperties attributes) throws IOException {
        OutputStream output = null;
        String propertiesFilePath = "<unknown>";
        try {
            propertiesFilePath = getFileSystemPath() + propertiesFilename;
            File propertiesFile = new File(propertiesFilePath);
            if (propertiesFile.exists())
                cmSystem.edit(propertiesFilePath);
            output = new FileOutputStream(propertiesFile);
            WikiPageProperties propertiesToSave = new WikiPageProperties(attributes);
            removeAlwaysChangingProperties(propertiesToSave);
            propertiesToSave.save(output);
        } catch (final IOException e) {
            logger.error("Failed to save properties file: \"" + propertiesFilePath, e);
            throw e;
        } catch (final RuntimeException e) {
            logger.error("Failed to save properties file: \"" + propertiesFilePath, e);
            throw e;
        } finally {
            if (output != null) {
                output.close();
                cmSystem.update(propertiesFilePath);
            }
        }
    }

    private void removeAlwaysChangingProperties(WikiPageProperties properties) {
        properties.remove(PageData.PropertyLAST_MODIFIED);
    }

    @Override
    protected WikiPage createChildPage(final String name) throws IOException {
        //return new FileSystemPage(getFileSystemPath(), name, this, this.versionsController);
        return new PageRepository(new DiskFileSystem()).makeChildPage(name, this);
    }

    private void loadContent(final PageData data) throws IOException {
        final File input = new File(getFileSystemPath() + contentFilename);
        data.setContent(input.exists() ? readContent(input) : "");
    }

    @Override
    protected void loadChildren() throws IOException {
        final File thisDir = new File(getFileSystemPath());
        if (thisDir.exists()) {
            final String[] subFiles = thisDir.list();
            for (final String subFile : subFiles) {
                if (fileIsValid(subFile, thisDir) && !this.children.containsKey(subFile)) {
                    this.children.put(subFile, getChildPage(subFile));
                }
            }
        }
    }

    private String readContent(final File input) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(input);
            return IOUtils.toString(inputStream, "UTF-8");
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private boolean fileIsValid(final String filename, final File dir) {
        if (WikiWordUtil.isWikiWord(filename)) {
            final File f = new File(dir, filename);
            if (f.isDirectory()) {
                return true;
            }
        }
        return false;
    }

    private String getParentFileSystemPath() {
        return this.parent != null ? ((FileSystemPage) this.parent).getFileSystemPath() : this.path;
    }

    public String getFileSystemPath() {
        return getParentFileSystemPath() + "/" + getName();
    }

    private void loadAttributes(final PageData data) {
        final File file = new File(getFileSystemPath() + propertiesFilename);
        if (file.exists()) {
            try {
                long lastModifiedTime = getLastModifiedTime();
                attemptToReadPropertiesFile(file, data, lastModifiedTime);
            } catch (final Exception e) {
                logger.error("Could not read properties file: " + file.getPath(), e);
            }
        }
    }

    private long getLastModifiedTime() throws Exception {
        long lastModifiedTime;

        final File file = new File(getFileSystemPath() + contentFilename);
        if (file.exists()) {
            lastModifiedTime = file.lastModified();
        } else {
            lastModifiedTime = getClock().currentClockTimeInMillis();
        }
        return lastModifiedTime;
    }

    private void attemptToReadPropertiesFile(File file, PageData data,
                                             long lastModifiedTime) throws Exception {
        InputStream input = null;
        try {
            final WikiPageProperties props = new WikiPageProperties();
            input = new FileInputStream(file);
            props.loadFromXmlStream(input);
            props.setLastModificationTime(new Date(lastModifiedTime));
            data.setProperties(props);
        } finally {
            if (input != null)
                input.close();
        }
    }

    @Override
    public void doCommit(final PageData data) throws IOException {
        saveContent(data.getContent());
        saveAttributes(data.getProperties());
        this.versionsController.prune(this);
    }

    @Override
    protected PageData makePageData() throws IOException {
        final PageData pagedata = new PageData(this);
        loadContent(pagedata);
        loadAttributes(pagedata);
        pagedata.addVersions(this.versionsController.history(this));
        return pagedata;
    }

    public PageData getDataVersion(final String versionName) {
        return this.versionsController.getRevisionData(this, versionName);
    }

    private void createDirectoryIfNewPage(FileSystem fileSystem) throws IOException {
        String pagePath = getFileSystemPath();
        if (!fileSystem.exists(pagePath)) {
            fileSystem.makeDirectory(pagePath);
            cmSystem.update(pagePath);
        }
    }

    @Override
    protected VersionInfo makeVersion() throws IOException {
        final PageData data = getData();
        return makeVersion(data);
    }

    protected VersionInfo makeVersion(final PageData data) {
        return this.versionsController.makeVersion(this, data);
    }

    protected void removeVersion(final String versionName) {
        this.versionsController.removeVersion(this, versionName);
    }

    @Override
    public String toString() {
        try {
            return getClass().getName() + " at " + this.getFileSystemPath();
        } catch (final Exception e) {
            return super.toString();
        }
    }

    public Clock getClock() {
        return clock;
    }

    class CmSystem {
        public void update(String fileName) {
            invokeCmMethod("cmUpdate", fileName);
        }


        public void edit(String fileName) {
            invokeCmMethod("cmEdit", fileName);
        }

        public void delete(String fileToBeDeleted) {
            invokeCmMethod("cmDelete", fileToBeDeleted);
        }

        public void preDelete(String fileToBeDeleted) {
            invokeCmMethod("cmPreDelete", fileToBeDeleted);
        }

        private void invokeCmMethod(String method, String newPagePath) {
            String cmSystemClassName = null;
            try {
                cmSystemClassName = getCmSystemClassName();
                if (cmSystemClassName != null) {
                    Class<?> cmSystem = Class.forName(getCmSystemClassName());
                    Method updateMethod = cmSystem.getMethod(method, String.class, String.class);
                    updateMethod.invoke(null, newPagePath, getCmSystemVariable());
                }
            } catch (Exception e) {
                System.err.println("Could not invoke static " + method + "(path,payload) of " + cmSystemClassName);
                e.printStackTrace();
            }
        }

        private String getCmSystemClassName() throws Exception {
            String cmSystemVariable = getCmSystemVariable();
            if (cmSystemVariable == null)
                return null;
            String cmSystemClassName = cmSystemVariable.split(" ")[0].trim();
            if (isEmpty(cmSystemClassName))
                return null;

            return cmSystemClassName;
        }

        private String getCmSystemVariable() throws IOException {
            return getData().getVariable("CM_SYSTEM");
        }
    }
}
