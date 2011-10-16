// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.updates;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseContextModule;
import util.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdaterImplementation extends UpdaterBase {

    private ArrayList<String> updateDoNotCopyOver = new ArrayList<String>();
    private ArrayList<String> updateList = new ArrayList<String>();
    private String fitNesseVersion = FitNesse.VERSION.toString();
    private final FitNesseContext context;

    @Inject
    public UpdaterImplementation(FitNesseContext context, @Named(FitNesseContextModule.ROOT_PAGE_PATH) String rootPagePath) throws IOException {
        super(rootPagePath);
        this.context = context;
        createUpdateAndDoNotCopyOverLists();
        updates = makeAllUpdates();
    }

    private Update[] makeAllUpdates() {
        List<Update> updates = new ArrayList<Update>();
        addAllFilesToBeReplaced(updates);
        addAllFilesThatShouldNotBeCopiedOver(updates);
        return updates.toArray(new Update[updates.size()]);

    }

    private void addAllFilesThatShouldNotBeCopiedOver(List<Update> updates) {
        for (String nonCopyableFile : updateDoNotCopyOver) {
            String path = getCorrectPathForTheDestination(nonCopyableFile);
            String source = getCorrectPathFromJar(nonCopyableFile);
            updates.add(new FileUpdate(context.rootPath, source, path));
        }
    }

    private void addAllFilesToBeReplaced(List<Update> updates) {
        for (String updatableFile : updateList) {
            String path = getCorrectPathForTheDestination(updatableFile);
            String source = getCorrectPathFromJar(updatableFile);
            updates.add(new ReplacingFileUpdate(context.rootPath, source, path));
        }
    }

    public String getCorrectPathFromJar(String updatableFile) {
        return "Resources/" + updatableFile;
    }


    public String getCorrectPathForTheDestination(String updatableFile) {
        if (updatableFile.startsWith("FitNesseRoot"))
            updatableFile = updatableFile.replace("FitNesseRoot", context.root.getName());
        return FileUtil.getPathOfFile(updatableFile);
    }

    private void createUpdateAndDoNotCopyOverLists() {
        tryToGetUpdateFilesFromJarFile();
        File updateFileList = new File(rootPagePath, "updateList");
        File updateDoNotCopyOverFileList = new File(rootPagePath, "updateDoNotCopyOverList");
        tryToParseTheFileIntoTheList(updateFileList, updateList);
        tryToParseTheFileIntoTheList(updateDoNotCopyOverFileList, updateDoNotCopyOver);
    }

    private void tryToGetUpdateFilesFromJarFile() {
        try {
            getUpdateFilesFromJarFile();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void getUpdateFilesFromJarFile() throws IOException {
        Update update = new FileUpdate(rootPagePath, "Resources/updateList", ".");
        update.doUpdate();
        update = new FileUpdate(rootPagePath, "Resources/updateDoNotCopyOverList", ".");
        update.doUpdate();
    }

    public void tryToParseTheFileIntoTheList(File updateFileList, ArrayList<String> list) {
        if (!updateFileList.exists())
            throw new RuntimeException("Could Not Find UpdateList");

        try {
            parseTheFileContentToAList(updateFileList, list);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private void parseTheFileContentToAList(File updateFileList, ArrayList<String> list) throws IOException {
        String content = FileUtil.getFileContent(updateFileList);
        String[] filePaths = content.split("\n");
        Collections.addAll(list, filePaths);
    }

    public void update() throws IOException {
        if (shouldUpdate()) {
            System.err.println("Unpacking new version of FitNesse resources.  Please be patient.");
            super.update();
            getProperties().put("Version", fitNesseVersion);
            saveProperties();
        }
    }

    private boolean shouldUpdate() {
        String versionProperty = getProperties().getProperty("Version");
        return versionProperty == null || !versionProperty.equals(fitNesseVersion);
    }

    public void setFitNesseVersion(String version) {
        fitNesseVersion = version;
    }
}
