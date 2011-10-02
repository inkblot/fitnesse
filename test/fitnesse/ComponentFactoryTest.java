// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.editing.EditResponder;
import fitnesse.wiki.*;
import fitnesse.wikitext.WidgetInterceptor;
import fitnesse.wikitext.WikiWidget;
import fitnesse.wikitext.parser.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static util.RegexAssertions.assertSubString;

public class ComponentFactoryTest extends FitnesseBaseTestCase {
    private Properties testProperties;
    private ComponentFactory factory;
    private SymbolProvider testProvider;
    private WikiPageFactory wikiPageFactory;
    private ResponderFactory responderFactory;

    @Before
    public void setUp() throws Exception {
        FitNesseContext context = makeContext();
        wikiPageFactory = context.getWikiPageFactory();
        testProperties = new Properties();
        testProvider = new SymbolProvider(new SymbolType[]{});
        factory = new ComponentFactory(testProperties, testProvider);
        responderFactory = context.getResponderFactory();
    }

    @After
    public void tearDown() throws Exception {
        final File file = new File(ComponentFactory.PROPERTIES_FILE);
        FileOutputStream out = new FileOutputStream(file);
        out.write("".getBytes());
        out.close();
        TestWidgetInterceptor.widgetsIntercepted.clear();
    }

    @Test
    public void testDefaultHtmlPageFactory() throws Exception {
        HtmlPageFactory pageFactory = factory.getHtmlPageFactory(new HtmlPageFactory());
        assertNotNull(pageFactory);
        assertEquals(HtmlPageFactory.class, pageFactory.getClass());
    }

    @Test
    public void testHtmlPageFactoryCreation() throws Exception {
        testProperties.setProperty(HtmlPageFactory.class.getSimpleName(), TestPageFactory.class.getName());

        HtmlPageFactory pageFactory = factory.getHtmlPageFactory(null);
        assertNotNull(pageFactory);
        assertEquals(TestPageFactory.class, pageFactory.getClass());
    }

    @Test
    public void testAddPlugins() throws Exception {
        testProperties.setProperty(ComponentFactory.PLUGINS, DummyPlugin.class.getName());

        assertMatch("!today", false);

        String output = factory.loadPlugins(responderFactory, wikiPageFactory);

        assertSubString(DummyPlugin.class.getName(), output);

        assertEquals(InMemoryPage.class, wikiPageFactory.getWikiPageClass());
        assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
        assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
        assertMatch("!today", true);
    }

    private void assertMatch(String input, boolean expected) {
        SymbolMatch match = testProvider.findMatch(new ScanString(input, 0), new MatchableFilter() {
            public boolean isValid(Matchable candidate) {
                return true;
            }
        });
        assertEquals(match.isMatch(), expected);
    }

    @Test
    public void testAddResponderPlugins() throws Exception {
        String respondersValue = "custom1:" + WikiPageResponder.class.getName() + ",custom2:" + EditResponder.class.getName();
        testProperties.setProperty(ComponentFactory.RESPONDERS, respondersValue);

        ResponderFactory responderFactory = new ResponderFactory(injector, ".");
        String output = factory.loadResponders(responderFactory);

        assertSubString("custom1:" + WikiPageResponder.class.getName(), output);
        assertSubString("custom2:" + EditResponder.class.getName(), output);

        assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
        assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
    }

    @Test
    public void testWikiWidgetPlugins() throws Exception {
        String symbolValues = Today.class.getName();
        testProperties.setProperty(ComponentFactory.SYMBOL_TYPES, symbolValues);

        String output = factory.loadSymbolTypes();

        assertSubString(Today.class.getName(), output);

        assertMatch("!today", true);
    }

    public static class TestWidgetInterceptor implements WidgetInterceptor {
        public static List<Class<?>> widgetsIntercepted = new ArrayList<Class<?>>();

        public void intercept(WikiWidget widget) {
            widgetsIntercepted.add(widget.getClass());
        }
    }

    public static class TestPageFactory extends HtmlPageFactory {
        public TestPageFactory(Properties p) {
            p.propertyNames();
        }
    }

    static class DummyPlugin {
        public static void registerWikiPage(WikiPageFactory factory) {
            // TODO: find a better way for a plugin to register a wiki page class.
            // Or, alternately, can't a plugin user just put the damn jar on the classpath and set the
            // WikiPage property in the wiki properties?
            factory.setWikiPageClass(InMemoryPage.class);
        }

        public static void registerResponders(ResponderFactory factory) {
            factory.addResponder("custom1", WikiPageResponder.class);
            factory.addResponder("custom2", EditResponder.class);
        }

        public static void registerSymbolTypes(SymbolProvider provider) {
            provider.add(new Today());
        }
    }
}
