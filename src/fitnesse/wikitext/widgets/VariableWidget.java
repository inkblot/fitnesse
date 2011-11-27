// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VariableWidget extends ParentWidget {
    public static final String prefix = "\\$\\{";
    public static final String REGEXP = prefix + "[\\w\\.]+\\}";
    public static final Pattern pattern = Pattern.compile(prefix + "([\\w\\.]+)\\}", Pattern.MULTILINE + Pattern.DOTALL);
    public static final String prefixDisabled = "!\\{";

    private String name = null;
    private String renderedText;

    public VariableWidget(ParentWidget parent, String text) {
        super(parent);
        Matcher match = pattern.matcher(text);
        if (match.find()) {
            name = match.group(1);
        }
    }

    public String render() throws IOException {
        if (renderedText == null) {
            Maybe<String> value = getVariableSource().findVariable(name);
            if (value.isNothing()) {
                renderedText = HtmlUtil.metaText("undefined variable: " + name);
            } else {
                addChildWidgets(value.getValue());
                renderedText = childHtml();
            }
        }
        return renderedText;
    }

    public String asWikiText() {
        return "${" + name + "}";
    }
}


