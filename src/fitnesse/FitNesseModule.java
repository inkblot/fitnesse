package fitnesse;

import com.google.inject.*;
import com.google.inject.name.Names;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.updates.NoOpUpdater;
import fitnesse.updates.UpdaterImplementation;
import fitnesse.wiki.VersionsController;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolProviderModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.UtilModule;

import java.io.File;
import java.util.Properties;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 9/28/11
* Time: 11:53 PM
*/
public class FitNesseModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseModule.class);

    public static final String ROOT_PAGE = "fitnesse.rootPage";
    public static final String ROOT_PATH = "fitnesse.rootPath";
    public static final String ROOT_PAGE_NAME = "fitnesse.rootPageName";
    public static final String ROOT_PAGE_PATH = "fitnesse.rootPagePath";
    public static final String PORT = "fitnesse.port";
    public static final String PROPERTIES_FILE = "plugins.properties";

    private final Properties properties;
    private final String userpass;
    private final String rootPath;
    private final String rootPageName;
    private final int port;
    private final boolean omitUpdates;

    public FitNesseModule(Properties properties, String userpass, String rootPath, String rootPageName, int port, boolean omitUpdates) {
        String absolutePath = new File(rootPath).getAbsolutePath();
        if (!absolutePath.equals(rootPath)) {
            logger.warn("rootPath is not absolute: rootPath=" + rootPath + " absolutePath=" + absolutePath, new RuntimeException());
        }

        this.properties = properties;
        this.userpass = userpass;
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
        this.port = port;
        this.omitUpdates = omitUpdates;
    }

    @Override
    protected void configure() {
        bind(Properties.class).annotatedWith(Names.named(FitNesseModule.PROPERTIES_FILE)).toInstance(properties);
        GuiceHelper.bindAuthenticator(binder(), properties, userpass);
        GuiceHelper.bindWikiPageClass(binder(), properties);
        GuiceHelper.bindFromProperty(binder(), VersionsController.class, properties);
        GuiceHelper.bindFromProperty(binder(), ContentFilter.class, properties);
        GuiceHelper.bindFromProperty(binder(), HtmlPageFactory.class, properties);
        install(new SymbolProviderModule());
        install(new UtilModule());
        bind(String.class).annotatedWith(Names.named(ROOT_PATH)).toInstance(rootPath);
        bind(String.class).annotatedWith(Names.named(ROOT_PAGE_NAME)).toInstance(rootPageName);
        String rootPagePath = rootPath + File.separator + rootPageName;
        bind(String.class).annotatedWith(Names.named(ROOT_PAGE_PATH)).toInstance(rootPagePath);
        bind(Integer.class).annotatedWith(Names.named(PORT)).toInstance(port);
        bindUpdater(omitUpdates);
        bind(WikiPage.class).annotatedWith(Names.named(ROOT_PAGE)).toProvider(RootPageProvider.class);
    }

    private void bindUpdater(boolean omitUpdates) {
        if (omitUpdates) {
            bind(Updater.class).to(NoOpUpdater.class);
        } else {
            bind(Updater.class).to(UpdaterImplementation.class);
        }
    }

    @Singleton
    public static class RootPageProvider implements Provider<WikiPage> {
        private final WikiPage rootPage;

        @Inject
        public RootPageProvider(WikiPageFactory wikiPageFactory) {
            try {
                this.rootPage = wikiPageFactory.makeRootPage();
            } catch (Exception e) {
                throw new ProvisionException("Could not create root page", e);
            }
        }

        @Override
        public WikiPage get() {
            return rootPage;
        }

    }
}
