// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.ImplementedBy;
import com.google.inject.Injector;
import fitnesse.Responder;
import fitnesse.http.Request;
import fitnesse.wiki.WikiPage;

@ImplementedBy(PromiscuousAuthenticator.class)
public abstract class Authenticator {

    private final Injector injector;
    private final WikiPage root;


    public Authenticator(WikiPage root, Injector injector) {
        this.root = root;
        this.injector = injector;
    }

    public Responder authenticate(Request request, Responder privilegedResponder) throws Exception {
        request.getCredentials();
        String username = request.getAuthorizationUsername();
        String password = request.getAuthorizationPassword();

        if (isAuthenticated(username, password))
            return privilegedResponder;
        else if (!isSecureResponder(privilegedResponder))
            return privilegedResponder;
        else
            return verifyOperationIsSecure(privilegedResponder, request);
    }

    private Responder verifyOperationIsSecure(Responder privilegedResponder, Request request) {
        SecureOperation so = ((SecureResponder) privilegedResponder).getSecureOperation();
        if (so.shouldAuthenticate(root, request))
            return unauthorizedResponder(request);
        else
            return privilegedResponder;
    }

    protected Responder unauthorizedResponder(Request request) {
        return injector.getInstance(UnauthorizedResponder.class);
    }

    private boolean isSecureResponder(Responder privilegedResponder) {
        return (privilegedResponder instanceof SecureResponder);
    }

    public abstract boolean isAuthenticated(String username, String password);

    public String toString() {
        return getClass().getName();
    }
}
