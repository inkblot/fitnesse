package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;

import java.lang.reflect.Method;

@Singleton
public class WikiPageFactory {
    public static final String WIKI_PAGE_CLASS = "WikiPage";

    private final Injector injector;
    private final String rootPath;
    private final String rootPageName;

    private Class<? extends WikiPage> wikiPageClass;

    @Inject
    public WikiPageFactory(Injector injector,
                           @Named(WikiPageFactory.WIKI_PAGE_CLASS) Class<? extends WikiPage> wikiPageClass,
                           @Named(FitNesseContext.ROOT_PATH) String rootPath,
                           @Named(FitNesseContext.ROOT_PAGE_NAME) String rootPageName) {
        this.injector = injector;
        this.wikiPageClass = wikiPageClass;
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
    }

    public WikiPage makeRootPage() throws Exception {
        Method makeRootMethod = wikiPageClass.getMethod("makeRoot", Injector.class, String.class, String.class);
        return (WikiPage) makeRootMethod.invoke(wikiPageClass, injector, this.rootPath, this.rootPageName);
    }

    public Class<?> getWikiPageClass() {
        return wikiPageClass;
    }

    @Deprecated
    public void setWikiPageClass(Class<? extends WikiPage> wikiPageClass) {
        this.wikiPageClass = wikiPageClass;
    }
}
