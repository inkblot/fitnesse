// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import com.google.inject.Injector;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.widgets.WikiWordWidget;
import org.apache.commons.io.IOUtils;
import util.*;

import java.io.*;
import java.lang.reflect.Method;
import java.util.Date;

public class FileSystemPage extends CachingPage {
    private static final long serialVersionUID = 1L;

    public static final String contentFilename = "/content.txt";
    public static final String propertiesFilename = "/properties.xml";

    private final String path;
    private final VersionsController versionsController;
    private CmSystem cmSystem = new CmSystem();

    public static WikiPage makeRoot(Injector injector, String rootPath, String rootPageName) throws IOException {
        return new FileSystemPage(rootPath, rootPageName, injector.getInstance(FileSystem.class), injector.getInstance(VersionsController.class));
    }

    private FileSystemPage(final String path, final String name, final FileSystem fileSystem, final VersionsController versionsController) throws IOException {
        super(name, null);
        this.path = path;
        this.versionsController = versionsController;
        createDirectoryIfNewPage(fileSystem);
    }

    public FileSystemPage(final String path, final String name) throws Exception {
        this(path, name, new DiskFileSystem(), new ZipFileVersionsController());
    }

    public FileSystemPage(final String name, final FileSystemPage parent, final FileSystem fileSystem) throws Exception {
        super(name, parent);
        path = parent.getFileSystemPath();
        versionsController = parent.versionsController;
        createDirectoryIfNewPage(fileSystem);
    }

    @Override
    public void removeChildPage(final String name) throws Exception {
        super.removeChildPage(name);
        String pathToDelete = getFileSystemPath() + "/" + name;
        final File fileToBeDeleted = new File(pathToDelete);
        cmSystem.preDelete(pathToDelete);
        FileUtil.deleteFileSystemDirectory(fileToBeDeleted);
        cmSystem.delete(pathToDelete);
    }

    @Override
    public boolean hasChildPage(final String pageName) throws Exception {
        final File f = new File(getFileSystemPath() + "/" + pageName);
        if (f.exists()) {
            addChildPage(pageName);
            return true;
        }
        return false;
    }

    protected synchronized void saveContent(String content) throws Exception {
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

    protected synchronized void saveAttributes(final WikiPageProperties attributes)
            throws Exception {
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
        } catch (final Exception e) {
            System.err.println("Failed to save properties file: \""
                    + propertiesFilePath + "\" (exception: " + e + ").");
            e.printStackTrace();
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
    protected WikiPage createChildPage(final String name) throws Exception {
        //return new FileSystemPage(getFileSystemPath(), name, this, this.versionsController);
        return new PageRepository().makeChildPage(name, this);
    }

    private void loadContent(final PageData data) throws IOException {
        String content = "";
        final String name = getFileSystemPath() + contentFilename;
        final File input = new File(name);
        if (input.exists()) {
            final byte[] bytes = readContentBytes(input);
            try {
                content = new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new ImpossibleException("UTF-8 is a supported encoding", e);
            }
        }
        data.setContent(content);
    }

    @Override
    protected void loadChildren() throws Exception {
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

    private byte[] readContentBytes(final File input) throws IOException {
        FileInputStream inputStream = null;
        try {
            final byte[] bytes = new byte[(int) input.length()];
            inputStream = new FileInputStream(input);
            inputStream.read(bytes);
            return bytes;
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
    }

    private boolean fileIsValid(final String filename, final File dir) {
        if (WikiWordWidget.isWikiWord(filename)) {
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

    private void loadAttributes(final PageData data) throws Exception {
        final File file = new File(getFileSystemPath() + propertiesFilename);
        if (file.exists()) {
            try {
                long lastModifiedTime = getLastModifiedTime();
                attemptToReadPropertiesFile(file, data, lastModifiedTime);
            } catch (final Exception e) {
                System.err.println("Could not read properties file:" + file.getPath());
                e.printStackTrace();
            }
        }
    }

    private long getLastModifiedTime() throws Exception {
        long lastModifiedTime;

        final File file = new File(getFileSystemPath() + contentFilename);
        if (file.exists()) {
            lastModifiedTime = file.lastModified();
        } else {
            lastModifiedTime = ClockUtil.currentTimeInMillis();
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
    public void doCommit(final PageData data) throws Exception {
        saveContent(data.getContent());
        saveAttributes(data.getProperties());
        this.versionsController.prune(this);
    }

    @Override
    protected PageData makePageData() throws Exception {
        final PageData pagedata = new PageData(this);
        loadContent(pagedata);
        loadAttributes(pagedata);
        pagedata.addVersions(this.versionsController.history(this));
        return pagedata;
    }

    public PageData getDataVersion(final String versionName) throws Exception {
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
    protected VersionInfo makeVersion() throws Exception {
        final PageData data = getData();
        return makeVersion(data);
    }

    protected VersionInfo makeVersion(final PageData data) throws Exception {
        return this.versionsController.makeVersion(this, data);
    }

    protected void removeVersion(final String versionName) throws Exception {
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
            if (cmSystemClassName == null || cmSystemClassName.equals(""))
                return null;

            return cmSystemClassName;
        }

        private String getCmSystemVariable() throws Exception {
            return getData().getVariable("CM_SYSTEM");
        }
    }
}
