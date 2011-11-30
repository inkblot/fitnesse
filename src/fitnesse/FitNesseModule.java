package fitnesse;

import com.google.inject.*;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.wiki.WikiModule;
import fitnesse.wikitext.parser.Context;
import fitnesse.wikitext.parser.MapVariableSource;
import fitnesse.wikitext.parser.VariableSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.UtilModule;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.google.inject.name.Names.named;
import static util.GuiceHelper.bindFromProperty;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 9/28/11
* Time: 11:53 PM
*/
public class FitNesseModule extends AbstractModule {
    private static final Logger logger = LoggerFactory.getLogger(FitNesseModule.class);

    public static final String PORT = "fitnesse.port";
    public static final String PROPERTIES_FILE = "plugins.properties";
    public static final String TEST_RESULTS_PATH = "fitnesse.testResultsPath";
    public static final String ENABLE_CHUNKING = "fitnesse.enableChunking";

    private final Properties properties;
    private final String userpass;
    private final String rootPath;
    private final String rootPageName;
    private final int port;
    private final boolean enableChunking;

    public FitNesseModule(Properties properties, String userpass, String rootPath, String rootPageName, int port, boolean enableChunking) {
        String absolutePath = new File(rootPath).getAbsolutePath();
        if (!absolutePath.equals(rootPath)) {
            logger.warn("rootPath is not absolute: rootPath=" + rootPath + " absolutePath=" + absolutePath, new RuntimeException());
        }

        this.properties = properties;
        this.userpass = userpass;
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
        this.port = port;
        this.enableChunking = enableChunking;
    }

    @Override
    protected void configure() {
        bind(Properties.class).annotatedWith(named(FitNesseModule.PROPERTIES_FILE)).toInstance(properties);
        bindAuthenticator(binder(), properties, userpass);
        bindFromProperty(binder(), ContentFilter.class, properties);
        install(new WikiModule(rootPath, rootPageName, properties));
        install(new UtilModule());
        String rootPagePath = rootPath + File.separator + rootPageName;
        String testResultPath = rootPagePath + File.separator + "files" + File.separator + "testResults";
        bind(String.class).annotatedWith(named(TEST_RESULTS_PATH)).toInstance(testResultPath);
        bind(Integer.class).annotatedWith(named(PORT)).toInstance(port);
        bind(Boolean.class).annotatedWith(named(ENABLE_CHUNKING)).toInstance(enableChunking);

        Map<String, String> contextVariables = new HashMap<String, String>();
        contextVariables.put("FITNESSE_PORT", Integer.toString(port));
        contextVariables.put("FITNESSE_ROOTPATH", rootPath);
        bind(VariableSource.class).annotatedWith(Context.class).toInstance(new MapVariableSource(contextVariables));
    }

    static void bindAuthenticator(Binder binder, Properties properties, final String userpass) {
        if (userpass != null) {
            if (new File(userpass).exists()) {
                binder.bind(String.class).annotatedWith(named("fitnesse.auth.multiUser.passwordFile")).toInstance(userpass);
                binder.bind(Authenticator.class).to(MultiUserAuthenticator.class);
            } else {
                final String[] values = userpass.split(":");
                binder.bind(String.class).annotatedWith(named("fitnesse.auth.singleUser.username")).toInstance(values[0]);
                binder.bind(String.class).annotatedWith(named("fitnesse.auth.singleUser.password")).toInstance(values[1]);
                binder.bind(Authenticator.class).to(OneUserAuthenticator.class);
            }
        } else {
            bindFromProperty(binder, Authenticator.class, properties);
        }
    }

}
