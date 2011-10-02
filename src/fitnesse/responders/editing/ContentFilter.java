// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.editing;

import com.google.inject.ImplementedBy;

@ImplementedBy(DefaultContentFilter.class)
public interface ContentFilter {
    boolean isContentAcceptable(String content, String page);
}
