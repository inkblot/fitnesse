package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import fitnesse.updates.NoOpUpdater;
import fitnesse.updates.UpdaterImplementation;

import java.io.File;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 9/28/11
* Time: 11:53 PM
*/
public class FitNesseContextModule extends AbstractModule {
    public static final String ROOT_PATH = "fitnesse.rootPath";
    public static final String ROOT_PAGE_NAME = "fitnesse.rootPageName";
    public static final String ROOT_PAGE_PATH = "fitnesse.rootPagePath";
    public static final String PORT = "fitnesse.port";
    public static final String PROPERTIES_FILE = "plugins.properties";

    private final String rootPath;
    private final String rootPageName;
    private final int port;
    private final boolean omitUpdates;

    public FitNesseContextModule(String rootPath, String rootPageName, int port, boolean omitUpdates) {
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
        this.port = port;
        this.omitUpdates = omitUpdates;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named(ROOT_PATH)).toInstance(rootPath);
        bind(String.class).annotatedWith(Names.named(ROOT_PAGE_NAME)).toInstance(rootPageName);
        String rootPagePath = rootPath + File.separator + rootPageName;
        bind(String.class).annotatedWith(Names.named(ROOT_PAGE_PATH)).toInstance(rootPagePath);
        bind(Integer.class).annotatedWith(Names.named(PORT)).toInstance(port);
        bindUpdater(omitUpdates);
    }

    private void bindUpdater(boolean omitUpdates) {
        if (omitUpdates) {
            bind(Updater.class).to(NoOpUpdater.class);
        } else {
            bind(Updater.class).to(UpdaterImplementation.class);
        }
    }
}
