// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import fitnesse.html.HtmlPageFactory;
import fitnesse.responders.ResponderFactory;
import fitnesse.responders.editing.ContentFilter;
import fitnesse.responders.editing.SaveResponder;
import fitnesse.wiki.VersionsController;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.zip.ZipFileVersionsController;
import fitnesse.wikitext.parser.SymbolProvider;
import fitnesse.wikitext.parser.SymbolType;
import util.TodoException;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import static util.FileUtil.loadProperties;

@Singleton
public class ComponentFactory {
    private final String endl = System.getProperty("line.separator");
    public static final String PROPERTIES_FILE = "plugins.properties";
    public static final String PLUGINS = "Plugins";
    public static final String RESPONDERS = "Responders";
    public static final String SYMBOL_TYPES = "SymbolTypes";
    public static final String DEFAULT_NEWPAGE_CONTENT = "newpage.default.content";

    private final Properties properties;
    private final SymbolProvider symbolProvider;

    public ComponentFactory(String propertiesLocation) {
        this(loadProperties(new File(propertiesLocation, PROPERTIES_FILE)), SymbolProvider.wikiParsingProvider);
    }

    @Inject
    public ComponentFactory(@Named(PROPERTIES_FILE) Properties properties, @Named(SymbolProvider.WIKI_PARSING) SymbolProvider symbolProvider) {
        this.properties = properties;
        this.symbolProvider = symbolProvider;
    }

    Properties getProperties() {
        return properties;
    }

    public String getProperty(String propertyName) {
        return getProperties().getProperty(propertyName);
    }

    public <T> T createComponent(Class<T> componentType, Class<? extends T> defaultComponent) {
        String componentClassName = properties.getProperty(componentType.getSimpleName());
        Class<? extends T> componentClass = defaultComponent;
        try {
            if (componentClassName != null) {
                Class<?> someClass = Class.forName(componentClassName);
                if (componentType.isAssignableFrom(someClass)) {
                    //noinspection unchecked
                    componentClass = (Class<? extends T>) someClass;
                }
            }

            if (componentClass != null) {
                Constructor<? extends T> constructor = componentClass.getConstructor(Properties.class);
                return constructor.newInstance(properties);
            } else {
                return null;
            }
        } catch (ClassNotFoundException e) {
            throw new TodoException(e);
        } catch (NoSuchMethodException e) {
            throw new TodoException(e);
        } catch (InvocationTargetException e) {
            throw new TodoException(e);
        } catch (InstantiationException e) {
            throw new TodoException(e);
        } catch (IllegalAccessException e) {
            throw new TodoException(e);
        }
    }

    public <T> T createComponent(Class<T> componentType) {
        return createComponent(componentType, null);
    }

    public HtmlPageFactory getHtmlPageFactory(HtmlPageFactory defaultPageFactory) {
        HtmlPageFactory htmlPageFactory = createComponent(HtmlPageFactory.class);
        return htmlPageFactory == null ? defaultPageFactory : htmlPageFactory;
    }

    public String loadPlugins(ResponderFactory responderFactory, WikiPageFactory wikiPageFactory) throws Exception {
        StringBuffer buffer = new StringBuffer();
        String[] responderPlugins = getListFromProperties(PLUGINS);
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

    private void loadWikiPageFromPlugin(Class<?> pluginClass, WikiPageFactory wikiPageFactory, StringBuffer buffer)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = pluginClass.getMethod("registerWikiPage", WikiPageFactory.class);
            method.invoke(pluginClass, wikiPageFactory);
            buffer.append("\t\t").append("wikiPage:").append(pluginClass.getName()).append(endl);
        } catch (NoSuchMethodException e) {
            // ok, no wiki page to register in this plugin
        }
    }

    private void loadRespondersFromPlugin(Class<?> pluginClass, ResponderFactory responderFactory, StringBuffer buffer)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = pluginClass.getMethod("registerResponders", ResponderFactory.class);
            method.invoke(pluginClass, responderFactory);
            buffer.append("\t\t").append("responders:").append(pluginClass.getName()).append(endl);
        } catch (NoSuchMethodException e) {
            // ok, no responders to register in this plugin
        }
    }

    private void loadSymbolTypesFromPlugin(Class<?> pluginClass, SymbolProvider symbolProvider, StringBuffer buffer)
            throws IllegalAccessException, InvocationTargetException {
        try {
            Method method = pluginClass.getMethod("registerSymbolTypes", SymbolProvider.class);
            method.invoke(pluginClass, symbolProvider);
            buffer.append("\t\t").append("widgets:").append(pluginClass.getName()).append(endl);
        } catch (NoSuchMethodException e) {
            // ok, no widgets to register in this plugin
        }
    }

    public String loadResponders(ResponderFactory responderFactory) throws Exception {
        StringBuilder buffer = new StringBuilder();
        String[] responderList = getListFromProperties(RESPONDERS);
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

    private String[] getListFromProperties(String propertyName) {
        String value = properties.getProperty(propertyName);
        if (value == null)
            return null;
        else
            return value.split(",");
    }

    public String loadSymbolTypes() throws Exception {
        StringBuilder buffer = new StringBuilder();
        String[] symbolTypeNames = getListFromProperties(SYMBOL_TYPES);
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

    public String loadContentFilter() {
        ContentFilter filter = createComponent(ContentFilter.class);
        if (filter != null) {
            SaveResponder.contentFilter = filter;
            return "\tContent filter installed: " + filter.getClass().getName() + "\n";
        }
        return "";
    }

    public VersionsController loadVersionsController() {
        return createComponent(VersionsController.class, ZipFileVersionsController.class);
    }
}
