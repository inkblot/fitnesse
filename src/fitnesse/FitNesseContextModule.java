package fitnesse;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 9/28/11
* Time: 11:53 PM
*/
public class FitNesseContextModule extends AbstractModule {
    private final String rootPath;
    private final String rootPageName;

    public FitNesseContextModule(String rootPath, String rootPageName) {
        this.rootPath = rootPath;
        this.rootPageName = rootPageName;
    }

    @Override
    protected void configure() {
        bind(String.class).annotatedWith(Names.named("fitnesse.rootPath")).toInstance(rootPath);
        bind(String.class).annotatedWith(Names.named("fitnesse.rootPageName")).toInstance(rootPageName);
    }
}
