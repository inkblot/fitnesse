package fitnesseMain;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import fitnesse.ComponentFactory;
import fitnesse.FitNesseModule;
import fitnesse.authentication.Authenticator;
import fitnesse.authentication.MultiUserAuthenticator;
import fitnesse.authentication.OneUserAuthenticator;
import fitnesse.authentication.PromiscuousAuthenticator;
import fitnesse.testutil.SimpleAuthenticator;
import fitnesse.wiki.*;
import fitnesse.wiki.zip.ZipFileVersionsController;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import util.FileSystem;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/11/11
 * Time: 6:36 AM
 */
public class FitNesseModuleTest {
    private static final String passwordFilename = "testpasswd";
    private File passwd;
    private Properties testProperties;

    @Before
    public void setUp() throws IOException {
        testProperties = new Properties();
        passwd = new File(passwordFilename);
        assertTrue(passwd.createNewFile());
    }

    @After
    public void tearDown() {
        assertTrue(passwd.delete());
        passwd = null;
        testProperties = null;
    }

    @Test
    public void testMakeDefaultAuthenticator() throws Exception {
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        Authenticator auth = injector.getInstance(Authenticator.class);
        assertTrue(auth instanceof PromiscuousAuthenticator);
    }

    @Test
    public void testMakeOneUserAuthenticator() throws Exception {
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, "bob:uncle"));
        Authenticator auth = injector.getInstance(Authenticator.class);
        assertTrue(auth instanceof OneUserAuthenticator);
        OneUserAuthenticator oua = (OneUserAuthenticator) auth;
        assertEquals("bob", oua.getUser());
        assertEquals("uncle", oua.getPassword());
    }

    @Test
    public void testMakeMultiUserAuthenticator() throws Exception {
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, passwordFilename));
        Authenticator auth = injector.getInstance(Authenticator.class);
        assertTrue(auth instanceof MultiUserAuthenticator);
    }


    @Test
    public void testAuthenticatorCustomCreation() throws Exception {
        testProperties.setProperty(Authenticator.class.getSimpleName(), SimpleAuthenticator.class.getName());

        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        Authenticator authenticator = injector.getInstance(Authenticator.class);

        assertNotNull(authenticator);
        assertEquals(SimpleAuthenticator.class, authenticator.getClass());
    }

    @Test
    public void testWikiPageClassDefault() {
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        Class wikiPageClass = injector.getInstance(Key.get(new TypeLiteral<Class<? extends WikiPage>>(){}, Names.named(WikiPageFactory.WIKI_PAGE_CLASS)));
        assertEquals(wikiPageClass, FileSystemPage.class);
    }

    @Test
    public void testInMemoryWikiPageClass() {
        testProperties.setProperty(WikiPageFactory.WIKI_PAGE_CLASS, InMemoryPage.class.getName());
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        Class wikiPageClass = injector.getInstance(Key.get(new TypeLiteral<Class<? extends WikiPage>>(){}, Names.named(WikiPageFactory.WIKI_PAGE_CLASS)));
        assertEquals(wikiPageClass, InMemoryPage.class);
    }

    @Test
    public void testShouldUseZipFileRevisionControllerAsDefault() {
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        VersionsController defaultRevisionController = injector.getInstance(VersionsController.class);
        assertEquals(ZipFileVersionsController.class, defaultRevisionController.getClass());
    }

    @Test
    public void testShouldUseSpecifiedRevisionController() {
        testProperties.setProperty(VersionsController.class.getSimpleName(), NullVersionsController.class.getName());
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        VersionsController defaultRevisionController = injector.getInstance(VersionsController.class);
        assertEquals(NullVersionsController.class, defaultRevisionController.getClass());
    }

    @Test
    public void allThingsInjectable() {
        Injector injector = Guice.createInjector(new FitNesseModule(testProperties, null));
        assertNotNull(injector.getInstance(ComponentFactory.class));
        assertNotNull(injector.getInstance(WikiPageFactory.class));
        assertNotNull(injector.getInstance(Authenticator.class));
        assertNotNull(injector.getInstance(VersionsController.class));
        assertNotNull(injector.getInstance(Key.get(new TypeLiteral<Class<? extends WikiPage>>(){}, Names.named(WikiPageFactory.WIKI_PAGE_CLASS))));
        assertNotNull(injector.getInstance(FileSystem.class));
        assertNotNull(injector.getInstance(Key.get(Properties.class, Names.named(ComponentFactory.PROPERTIES_FILE))));
    }
}
