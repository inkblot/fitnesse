// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package util;

public class StringUtil {

    public static String replaceAll(String original, String target, String replacement) {
        StringBuilder result = new StringBuilder();
        int fromIndex = 0;
        while (true) {
            int foundIndex = original.indexOf(target, fromIndex);
            if (foundIndex == -1) {
                result.append(original.substring(fromIndex));
                break;
            }
            result.append(original.substring(fromIndex, foundIndex));
            result.append(replacement);
            fromIndex = foundIndex + target.length();
        }
        return result.toString();
    }

    public static String stripCarriageReturns(String s) {
        if (s == null)
            return null;
        else
            return s.replaceAll("\r", "");
    }
}
