// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.Responder;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.testutil.SimpleAuthenticator;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthenticatorTest extends FitnesseBaseTestCase {
    SimpleAuthenticator authenticator;
    private MockRequest request;
    private Class<? extends Responder> responderType;
    private DummySecureResponder privilegedResponder;
    private WikiPage root;

    class DummySecureResponder implements SecureResponder {

        public SecureOperation getSecureOperation() {
            return new AlwaysSecureOperation();
        }

        public Response makeResponse(Request request) {
            return null;
        }

    }

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        WikiPage frontpage = root.addChildPage("FrontPage");
        makeReadSecure(frontpage);
        authenticator = new SimpleAuthenticator(root, injector);
        privilegedResponder = new DummySecureResponder();

        request = new MockRequest();
        request.setResource("FrontPage");
    }

    private void makeReadSecure(WikiPage frontpage) throws Exception {
        PageData data = frontpage.getData();
        data.setAttribute(PageData.PropertySECURE_READ);
        frontpage.commit(data);
    }

    @Test
    public void testNotAuthenticated() throws Exception {
        makeResponder();
        assertEquals(UnauthorizedResponder.class, responderType);
    }

    @Test
    public void testAuthenticated() throws Exception {
        authenticator.authenticated = true;
        makeResponder();
        assertEquals(DummySecureResponder.class, responderType);
    }

    private void makeResponder() throws Exception {
        Responder responder = authenticator.authenticate(request, privilegedResponder);
        responderType = responder.getClass();
    }
}
