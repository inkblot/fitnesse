// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import fitnesse.http.Request;
import fitnesse.wiki.WikiPage;

public class AlwaysSecureOperation implements SecureOperation {
    public boolean shouldAuthenticate(WikiPage root, Request request) {
        return true;
    }
}
