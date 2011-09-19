package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.VersionsController;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolProviderModule;
import fitnesseMain.FitNesseMain;
import util.FileUtil;
import util.UtilModule;

import java.io.File;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 8/29/11
 * Time: 11:02 PM
 */
public class FitNesseModule extends AbstractModule {
    private final Properties properties;
    private final String userpass;

    public FitNesseModule(FitNesseMain.Arguments args) {
        this(
                FileUtil.loadProperties(new File(args.getRootPath(), ComponentFactory.PROPERTIES_FILE)),
                args.getUserpass());
    }

    public FitNesseModule(Properties properties, String userpass) {
        this.properties = properties;
        this.userpass = userpass;
    }

    @Override
    protected void configure() {
        bind(Properties.class).annotatedWith(Names.named(ComponentFactory.PROPERTIES_FILE)).toInstance(properties);
        bindAuthenticator();
        bindWikiPageClass();
        bindFromProperty(VersionsController.class);
        install(new SymbolProviderModule());
        install(new UtilModule());
    }

    private void bindAuthenticator() {
        if (userpass != null) {
            if (new File(userpass).exists()) {
                bind(Authenticator.class).toInstance(new MultiUserAuthenticator(userpass));
            } else {
                String[] values = userpass.split(":");
                bind(Authenticator.class).toInstance(new OneUserAuthenticator(values[0], values[1]));
            }
        } else {
            bindFromProperty(Authenticator.class);
        }
    }

    private void bindWikiPageClass() {
        bind(new TypeLiteral<Class<? extends WikiPage>>(){})
                .annotatedWith(Names.named(WikiPageFactory.WIKI_PAGE_CLASS))
                .toInstance(getClassFromProperty(properties, WikiPage.class, FileSystemPage.class));
    }

    private <T> void bindFromProperty(Class<T> bindingClass) {
        Class<? extends T> implClass = getClassFromProperty(properties, bindingClass, null);
        if (implClass != null) {
            bind(bindingClass).to(implClass);
        }
    }

    private static <T> Class<? extends T> getClassFromProperty(Properties properties, Class<T> interfaceClass, Class<? extends T> defaultImplClass) {
        String implClassName = properties.getProperty(interfaceClass.getSimpleName());
        if (implClassName != null) {
            try {
                Class<?> someClass = Class.forName(implClassName);
                if (interfaceClass.isAssignableFrom(someClass)) {
                    //noinspection unchecked
                    return (Class<? extends T>) someClass;
                }
            } catch (ClassNotFoundException e) {
                // ignore
            }
        }
        return defaultImplClass;
    }

}
