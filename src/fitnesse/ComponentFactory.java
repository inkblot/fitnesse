// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import fitnesse.responders.ResponderFactory;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

public class ComponentFactory {
    private static final String endl = System.getProperty("line.separator");
    public static final String PROPERTIES_FILE = "plugins.properties";
    public static final String PLUGINS = "Plugins";
    public static final String RESPONDERS = "Responders";
    public static final String SYMBOL_TYPES = "SymbolTypes";
    public static final String DEFAULT_NEWPAGE_CONTENT = "newpage.default.content";

    private ComponentFactory() {}

    public static String loadPlugins(ResponderFactory responderFactory, WikiPageFactory wikiPageFactory, SymbolProvider symbolProvider, Properties properties) throws Exception {
        StringBuffer buffer = new StringBuffer();
        String[] responderPlugins = getListFromProperties(PLUGINS, properties);
        if (responderPlugins != null) {
            buffer.append("\tCustom plugins loaded:").append(endl);
            for (String responderPlugin : responderPlugins) {
                Class<?> pluginClass = Class.forName(responderPlugin);
                loadWikiPageFromPlugin(pluginClass, wikiPageFactory, buffer);
                loadRespondersFromPlugin(pluginClass, responderFactory, buffer);
                loadSymbolTypesFromPlugin(pluginClass, symbolProvider, buffer);
            }
        }
        return buffer.toString();
    }

    private static void loadWikiPageFromPlugin(Class<?> pluginClass, WikiPageFactory wikiPageFactory, StringBuffer buffer)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = pluginClass.getMethod("registerWikiPage", WikiPageFactory.class);
            method.invoke(pluginClass, wikiPageFactory);
            buffer.append("\t\t").append("wikiPage:").append(pluginClass.getName()).append(endl);
        } catch (NoSuchMethodException e) {
            // ok, no wiki page to register in this plugin
        }
    }

    private static void loadRespondersFromPlugin(Class<?> pluginClass, ResponderFactory responderFactory, StringBuffer buffer)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = pluginClass.getMethod("registerResponders", ResponderFactory.class);
            method.invoke(pluginClass, responderFactory);
            buffer.append("\t\t").append("responders:").append(pluginClass.getName()).append(endl);
        } catch (NoSuchMethodException e) {
            // ok, no responders to register in this plugin
        }
    }

    private static void loadSymbolTypesFromPlugin(Class<?> pluginClass, SymbolProvider symbolProvider, StringBuffer buffer)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = pluginClass.getMethod("registerSymbolTypes", SymbolProvider.class);
            method.invoke(pluginClass, symbolProvider);
            buffer.append("\t\t").append("widgets:").append(pluginClass.getName()).append(endl);
        } catch (NoSuchMethodException e) {
            // ok, no widgets to register in this plugin
        }
    }

    public static String loadResponders(ResponderFactory responderFactory, Properties properties) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String[] responderList = getListFromProperties(RESPONDERS, properties);
        if (responderList != null) {
            buffer.append("\tCustom responders loaded:").append(endl);
            for (String responder : responderList) {
                String[] values = responder.trim().split(":");
                String key = values[0];
                String className = values[1];
                responderFactory.addResponder(key, className);
                buffer.append("\t\t").append(key).append(":").append(className).append(endl);
            }
        }
        return buffer.toString();
    }

    private static String[] getListFromProperties(String propertyName, Properties properties) {
        String value = properties.getProperty(propertyName);
        if (value == null)
            return null;
        else
            return value.split(",");
    }

    public static String loadSymbolTypes(Properties properties, SymbolProvider symbolProvider) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String[] symbolTypeNames = getListFromProperties(SYMBOL_TYPES, properties);
        if (symbolTypeNames != null) {
            buffer.append("\tCustom symbol types loaded:").append(endl);
            for (String symbolTypeName : symbolTypeNames) {
                Class<?> symbolTypeClass = Class.forName(symbolTypeName.trim());
                symbolProvider.add((SymbolType) symbolTypeClass.newInstance());
                buffer.append("\t\t").append(symbolTypeClass.getName()).append(endl);
            }
        }
        return buffer.toString();
    }

}
