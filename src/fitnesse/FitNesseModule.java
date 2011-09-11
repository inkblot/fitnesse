package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
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
    private String userpass;

    public FitNesseModule(FitNesseMain.Arguments args) {
        this(FileUtil.loadProperties(new File(args.getRootPath(), ComponentFactory.PROPERTIES_FILE)), args.getUserpass());
    }

    public FitNesseModule(Properties properties, String userpass) {
        this.properties = properties;
        this.userpass = userpass;
    }

    @Override
    protected void configure() {
        bind(Properties.class).annotatedWith(Names.named(ComponentFactory.PROPERTIES_FILE)).toInstance(properties);
        bindAuthenticator();
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
            String authClassName = properties.getProperty(Authenticator.class.getSimpleName());
            if (authClassName != null) {
                try {
                    Class<?> someClass = Class.forName(authClassName);
                    if (Authenticator.class.isAssignableFrom(someClass)) {
                        Class<? extends Authenticator> authenticatorClass = (Class<Authenticator>) someClass;
                        bind(Authenticator.class).to(authenticatorClass);
                    }
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
    }
}
