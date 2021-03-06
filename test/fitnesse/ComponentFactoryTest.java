// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Inject;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.WikiPageResponder;
import fitnesse.responders.editing.EditResponder;
import fitnesse.wiki.*;
import fitnesse.wikitext.parser.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static util.RegexAssertions.assertSubString;

public class ComponentFactoryTest extends FitnesseBaseTestCase {
    private Properties testProperties;
    private SymbolProvider testProvider;
    private WikiPageFactory wikiPageFactory;
    private ResponderFactory responderFactory;

    @Inject
    public void inject(WikiPageFactory wikiPageFactory, ResponderFactory responderFactory) {
        this.wikiPageFactory = wikiPageFactory;
        this.responderFactory = responderFactory;
    }

    @Before
    public void setUp() throws Exception {
        testProperties = new Properties();
        testProvider = new SymbolProvider();
    }

    @After
    public void tearDown() throws Exception {
        final File file = new File(FitNesseModule.PROPERTIES_FILE);
        FileOutputStream out = new FileOutputStream(file);
        out.write("".getBytes());
        out.close();
    }

    @Test
    public void testAddPlugins() throws Exception {
        testProperties.setProperty(ComponentFactory.PLUGINS, DummyPlugin.class.getName());

        assertMatch("!today", false);

        String output = ComponentFactory.loadPlugins(responderFactory, wikiPageFactory, testProvider, testProperties);

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

        String output = ComponentFactory.loadResponders(responderFactory, testProperties);

        assertSubString("custom1:" + WikiPageResponder.class.getName(), output);
        assertSubString("custom2:" + EditResponder.class.getName(), output);

        assertEquals(WikiPageResponder.class, responderFactory.getResponderClass("custom1"));
        assertEquals(EditResponder.class, responderFactory.getResponderClass("custom2"));
    }

    @Test
    public void testWikiWidgetPlugins() throws Exception {
        String symbolValues = Today.class.getName();
        testProperties.setProperty(ComponentFactory.SYMBOL_TYPES, symbolValues);

        String output = ComponentFactory.loadSymbolTypes(testProperties, testProvider);

        assertSubString(Today.class.getName(), output);

        assertMatch("!today", true);
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
