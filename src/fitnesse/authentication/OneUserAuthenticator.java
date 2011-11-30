// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;

public class OneUserAuthenticator extends Authenticator {
    private String theUsername;
    private String thePassword;

    @Inject
    public OneUserAuthenticator(
            @Named("fitnesse.auth.singleUser.username") String theUsername,
            @Named("fitnesse.auth.singleUser.password") String thePassword,
            @Named(WikiModule.ROOT_PAGE) WikiPage root, Injector injector) {
        super(root, injector);
        this.theUsername = theUsername;
        this.thePassword = thePassword;
    }

    public boolean isAuthenticated(String username, String password) {
        return (theUsername.equals(username) && thePassword.equals(password));
    }

    public String getUser() {
        return theUsername;
    }

    public String getPassword() {
        return thePassword;
    }
}
