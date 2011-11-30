package fitnesse.wiki;

import com.google.inject.*;
import fitnesse.wikitext.parser.SymbolProviderModule;
import util.GuiceHelper;
import fitnesse.html.HtmlPageFactory;

import java.io.File;
import java.util.Properties;

import static com.google.inject.name.Names.named;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 11/29/11
 * Time: 6:24 PM
 */
public class WikiModule extends AbstractModule {
    public static final String ROOT_PAGE_PATH = "fitnesse.rootPagePath";
    public static final String WIKI_PAGE_CLASS = "WikiPage";
    public static final String ROOT_PATH = "fitnesse.rootPath";
    public static final String ROOT_PAGE_NAME = "fitnesse.rootPageName";
    public static final String ROOT_PAGE = "fitnesse.rootPage";

    private final String rootPath;
    private final String rootPageName;
    private final Properties properties;

    public WikiModule(String rootPath, String rootPageName, Properties properties) {
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
        this.properties = properties;
    }

    public static void bindWikiPageClass(Binder binder, Properties properties) {
        binder.bind(new TypeLiteral<Class<? extends WikiPage>>() {
        })
                .annotatedWith(named(WIKI_PAGE_CLASS))
                .toInstance(GuiceHelper.getClassFromProperty(properties, WikiPage.class, FileSystemPage.class));
    }

    @Override
    protected void configure() {
        bindWikiPageClass(binder(), properties);
        GuiceHelper.bindFromProperty(binder(), VersionsController.class, properties);
        GuiceHelper.bindFromProperty(binder(), HtmlPageFactory.class, properties);
        bind(String.class).annotatedWith(named(ROOT_PATH)).toInstance(rootPath);
        bind(String.class).annotatedWith(named(ROOT_PAGE_NAME)).toInstance(rootPageName);
        String rootPagePath = rootPath + File.separator + rootPageName;
        bind(String.class).annotatedWith(named(ROOT_PAGE_PATH)).toInstance(rootPagePath);
        bind(WikiPage.class).annotatedWith(named(ROOT_PAGE)).toProvider(RootPageProvider.class);
        install(new SymbolProviderModule());
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
