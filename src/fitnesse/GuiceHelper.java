package fitnesse;

import com.google.inject.*;
import com.google.inject.name.Names;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesseMain.FitNesseMain;

import java.io.File;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/18/11
 * Time: 7:25 AM
 */
public class GuiceHelper {
    static void bindAuthenticator(Binder binder, Properties properties, final String userpass) {
        if (userpass != null) {
            if (new File(userpass).exists()) {
                binder.bind(String.class).annotatedWith(Names.named("fitnesse.auth.multiUser.passwordFile")).toInstance(userpass);
                binder.bind(Authenticator.class).to(MultiUserAuthenticator.class);
            } else {
                final String[] values = userpass.split(":");
                binder.bind(String.class).annotatedWith(Names.named("fitnesse.auth.singleUser.username")).toInstance(values[0]);
                binder.bind(String.class).annotatedWith(Names.named("fitnesse.auth.singleUser.password")).toInstance(values[1]);
                binder.bind(Authenticator.class).to(OneUserAuthenticator.class);
            }
        } else {
            bindFromProperty(binder, Authenticator.class, properties);
        }
    }

    static void bindWikiPageClass(Binder binder, Properties properties) {
        binder.bind(new TypeLiteral<Class<? extends WikiPage>>() {
        })
                .annotatedWith(Names.named(WikiPageFactory.WIKI_PAGE_CLASS))
                .toInstance(getClassFromProperty(properties, WikiPage.class, FileSystemPage.class));
    }

    static <T> void bindFromProperty(Binder binder, Class<T> bindingClass, Properties properties) {
        Class<? extends T> implClass = getClassFromProperty(properties, bindingClass, null);
        if (implClass != null) {
            binder.bind(bindingClass).to(implClass);
        }
    }

    static <T> Class<? extends T> getClassFromProperty(Properties properties, Class<T> interfaceClass, Class<? extends T> defaultImplClass) {
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

    public static Injector makeContext(Properties properties, String userpass, String rootPath, String rootPageName, int port, boolean enableChunking) throws Exception {
        return Guice.createInjector(new FitNesseModule(properties, userpass, rootPath, rootPageName, port, enableChunking));
    }

    public static Injector makeContext(FitNesseMain.Arguments arguments, Properties pluginProperties) throws Exception {
        return makeContext(pluginProperties, arguments.getUserpass(), arguments.getRootPath(), arguments.getRootDirectory(), arguments.getPort(), arguments.getCommand() == null);
    }
}
