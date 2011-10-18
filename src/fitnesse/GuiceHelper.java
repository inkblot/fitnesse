package fitnesse;

import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;

import java.io.File;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/18/11
 * Time: 7:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class GuiceHelper {
    static void bindAuthenticator(Binder binder, Properties properties, final String userpass) {
        if (userpass != null) {
            if (new File(userpass).exists()) {
                binder.bind(Authenticator.class).toProvider(new Provider<Authenticator>() {
                    @Override
                    public Authenticator get() {
                        return new MultiUserAuthenticator(userpass);
                    }
                });
            } else {
                final String[] values = userpass.split(":");
                binder.bind(Authenticator.class).toProvider(new Provider<Authenticator>() {
                    @Override
                    public Authenticator get() {
                        return new OneUserAuthenticator(values[0], values[1]);
                    }
                });
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
}
