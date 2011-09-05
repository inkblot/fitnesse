package util;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/5/11
 * Time: 9:23 AM
 */
class Option {
    public boolean active = false;
    private String[] argumentNames;
    private String[] argumentValues;
    private int argumentIndex = 0;

    protected void parseArgumentDescriptor(String arguments) {
        argumentNames = split(arguments);
        argumentValues = new String[argumentNames.length];
    }

    public String getArgument(String argName) {
        for (int i = 0; i < argumentNames.length; i++) {
            String requiredArgumentName = argumentNames[i];
            if (requiredArgumentName.equals(argName))
                return argumentValues[i];
        }
        return null;
    }

    public boolean needsMoreArguments() {
        return argumentIndex < argumentNames.length;
    }

    public void addArgument(String value) {
        argumentValues[argumentIndex++] = value;
    }

    protected String[] split(String value) {
        String[] tokens = value.split(" ");
        List<String> usableTokens = new LinkedList<String>();
        for (String token : tokens) {
            if (token.length() > 0)
                usableTokens.add(token);
        }
        return usableTokens.toArray(new String[usableTokens.size()]);
    }
}
