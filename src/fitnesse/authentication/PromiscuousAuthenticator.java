// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

public class PromiscuousAuthenticator extends Authenticator {
    @Inject
    public PromiscuousAuthenticator(@Named(WikiModule.ROOT_PAGE) WikiPage root, Injector injector) {
        super(root, injector);
    }

    public boolean isAuthenticated(String username, String password) {
        return true;
    }
}
