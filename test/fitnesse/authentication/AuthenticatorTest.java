// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.SingleContextBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.testutil.SimpleAuthenticator;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPage;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AuthenticatorTest extends SingleContextBaseTestCase {
    SimpleAuthenticator authenticator;
    private MockRequest request;
    private Class<? extends Responder> responderType;
    private DummySecureResponder privilegedResponder;
    private FitNesseContext context;

    class DummySecureResponder implements SecureResponder {

        public SecureOperation getSecureOperation() {
            return new AlwaysSecureOperation();
        }

        public Response makeResponse(FitNesseContext context, Request request) {
            return null;
        }

    }

    @Inject
    public void inject(FitNesseContext context) {
        this.context = context;
    }

    @Before
    public void setUp() throws Exception {
        WikiPage root = context.root;
        WikiPage frontpage = root.addChildPage("FrontPage");
        makeReadSecure(frontpage);
        authenticator = new SimpleAuthenticator();
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
        Responder responder = authenticator.authenticate(context, request, privilegedResponder);
        responderType = responder.getClass();
    }
}
