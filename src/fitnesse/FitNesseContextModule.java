package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

import java.io.File;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 9/28/11
* Time: 11:53 PM
*/
public class FitNesseContextModule extends AbstractModule {
    private final String rootPath;
    private final String rootPageName;
    private final int port;

    public FitNesseContextModule(String rootPath, String rootPageName, int port) {
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
        this.port = port;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named(FitNesseContext.ROOT_PATH)).toInstance(rootPath);
        bind(String.class).annotatedWith(Names.named(FitNesseContext.ROOT_PAGE_NAME)).toInstance(rootPageName);
        String rootPagePath = rootPath + File.separator + rootPageName;
        bind(String.class).annotatedWith(Names.named(FitNesseContext.ROOT_PAGE_PATH)).toInstance(rootPagePath);
        bind(Integer.class).annotatedWith(Names.named(FitNesseContext.PORT)).toInstance(port);
    }
}
