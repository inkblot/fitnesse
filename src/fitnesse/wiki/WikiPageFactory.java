package fitnesse.wiki;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.ComponentFactory;
import util.FileSystem;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

@Singleton
public class WikiPageFactory {
    public static final String WIKI_PAGE_CLASS = "WikiPage";

    private Class<? extends WikiPage> wikiPageClass;
    private final Properties properties;
    private final FileSystem fileSystem;

    @Inject
    public WikiPageFactory(FileSystem fileSystem, @Named(WikiPageFactory.WIKI_PAGE_CLASS) Class<? extends WikiPage> wikiPageClass, @Named(ComponentFactory.PROPERTIES_FILE) Properties properties) {
        this.fileSystem = fileSystem;
        this.wikiPageClass = wikiPageClass;
        this.properties = properties;
    }

    public WikiPage makeRootPage(String rootPath, String rootPageName, ComponentFactory componentFactory) throws Exception {
        try {
            Constructor<?> constructorMethod = wikiPageClass.getConstructor(String.class, String.class, FileSystem.class, ComponentFactory.class);
            return (WikiPage) constructorMethod.newInstance(rootPath, rootPageName, fileSystem, componentFactory);
        } catch (NoSuchMethodException e) {
            Method makeRootMethod = wikiPageClass.getMethod("makeRoot", Properties.class);
            return (WikiPage) makeRootMethod.invoke(wikiPageClass, properties);
        }
    }

    public Class<?> getWikiPageClass() {
        return wikiPageClass;
    }

    @Deprecated
    public void setWikiPageClass(Class<? extends WikiPage> wikiPageClass) {
        this.wikiPageClass = wikiPageClass;
    }
}
