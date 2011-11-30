package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import java.lang.reflect.Method;

@Singleton
public class WikiPageFactory {

    private final Injector injector;
    private final String rootPath;
    private final String rootPageName;

    private Class<? extends WikiPage> wikiPageClass;

    @Inject
    public WikiPageFactory(Injector injector,
                           @Named(WikiModule.WIKI_PAGE_CLASS) Class<? extends WikiPage> wikiPageClass,
                           @Named(WikiModule.ROOT_PATH) String rootPath,
                           @Named(WikiModule.ROOT_PAGE_NAME) String rootPageName) {
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
