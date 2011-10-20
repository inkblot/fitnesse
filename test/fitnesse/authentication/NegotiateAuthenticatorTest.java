package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.Responder;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.components.Base64;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.WikiPage;
import org.ietf.jgss.*;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import util.ImpossibleException;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;
import static util.RegexAssertions.assertSubString;

public class NegotiateAuthenticatorTest extends FitnesseBaseTestCase {
    private GSSManager manager;
    private Properties properties;
    private final String TOKEN = "xxxxxxxx";
    private HtmlPageFactory htmlPageFactory;
    private WikiPage root;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
    }

    @Before
    public void setUp() {
        manager = mock(GSSManager.class);
        properties = new Properties();
    }

    @Test
    public void credentialsShouldBeNullIfNoServiceName() throws Exception {
        NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties, root, injector, htmlPageFactory);
        assertNull(authenticator.getServerCredentials());
        verify(manager, never()).createName(anyString(), (Oid) anyObject(), (Oid) anyObject());
    }

    @Test
    public void credentialsShouldBeNonNullIfServiceNamePresent() throws Exception {
        properties.setProperty("NegotiateAuthenticator.serviceName", "service");
        properties.setProperty("NegotiateAuthenticator.serviceNameType", "1.1");
        properties.setProperty("NegotiateAuthenticator.mechanism", "1.2");
        GSSName gssName = mock(GSSName.class);
        GSSCredential gssCredential = mock(GSSCredential.class);
        when(manager.createName(anyString(), (Oid) anyObject(), (Oid) anyObject())).thenReturn(gssName);
        when(manager.createCredential((GSSName) anyObject(), anyInt(), (Oid) anyObject(), anyInt())).thenReturn(gssCredential);
        NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties, root, injector, htmlPageFactory);
        Oid serviceNameType = authenticator.getServiceNameType();
        Oid mechanism = authenticator.getMechanism();
        verify(manager).createName("service", serviceNameType, mechanism);
        assertEquals("1.1", serviceNameType.toString());
        assertEquals("1.2", mechanism.toString());
        verify(manager).createCredential(gssName, GSSCredential.INDEFINITE_LIFETIME, mechanism, GSSCredential.ACCEPT_ONLY);
        assertEquals(gssCredential, authenticator.getServerCredentials());
    }

    @Test
    public void negotiationErrorScreenForFailureToComplete() throws Exception {
        Responder responder = new NegotiateAuthenticator.UnauthenticatedNegotiateResponder("token", htmlPageFactory);
        Request request = new MockRequest();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);
        assertEquals("Negotiate token", response.getHeader("WWW-Authenticate"));
        String content = response.getContent();
        assertSubString("Negotiated authentication required", content);
        assertSubString("Your client failed to complete required authentication", content);
    }

    @Test
    public void negotiationErrorScreenForNeedingAuthentication() throws Exception {
        Responder responder = new NegotiateAuthenticator.UnauthenticatedNegotiateResponder("token", htmlPageFactory);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(null);
        String content = response.getContent();
        assertSubString("This request requires authentication", content);
    }

    @Test
    public void noAuthorizationHeaderShouldProduceNullCredentials() throws Exception {
        MockRequest request = new MockRequest();
        NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties, root, injector, htmlPageFactory);
        authenticator.negotiateCredentials(request);
        assertNull(request.getAuthorizationUsername());
        assertNull(request.getAuthorizationPassword());
    }

    @Test
    public void invalidAuthorizationHeaderShouldProduceNullCredentials() throws Exception {
        MockRequest request = new MockRequest();
        request.addHeader("Authorization", "blah");
        NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties, root, injector, htmlPageFactory);
        authenticator.negotiateCredentials(request);
        assertNull(request.getAuthorizationUsername());
        assertNull(request.getAuthorizationPassword());
    }

    @Test
    public void validAuthorizationHeaderAndEstablishedContextShouldProduceUserAndPassword() throws Exception {
        String userName = "username";
        String password = "password";
        String encodedPassword = base64Encode(password);
        GSSContext gssContext = makeMockGssContext(userName, password);
        when(gssContext.isEstablished()).thenReturn(true);
        MockRequest request = new MockRequest();
        doNegotiation(request);
        assertEquals(userName, request.getAuthorizationUsername());
        assertEquals(encodedPassword, request.getAuthorizationPassword());
    }

    private void doNegotiation(MockRequest request) throws Exception {
        request.addHeader("Authorization", NegotiateAuthenticator.NEGOTIATE + " " + TOKEN);
        NegotiateAuthenticator authenticator = new NegotiateAuthenticator(manager, properties, root, injector, htmlPageFactory);
        authenticator.negotiateCredentials(request);
    }

    private GSSContext makeMockGssContext(String userName, String password) throws GSSException {
        GSSContext gssContext = mock(GSSContext.class);
        when(manager.createContext((GSSCredential) anyObject())).thenReturn(gssContext);
        when(gssContext.acceptSecContext((byte[]) anyObject(), anyInt(), anyInt())).thenReturn(password.getBytes());
        GSSName gssName = mock(GSSName.class);
        when(gssName.toString()).thenReturn(userName);
        when(gssContext.getSrcName()).thenReturn(gssName);
        return gssContext;
    }


    @Test
    public void validAuthorizationHeaderAndNoEstablishedContextShouldProducePasswordButNoUser() throws Exception {
        String userName = "username";
        String password = "password";
        String encodedPassword = base64Encode(password);
        GSSContext gssContext = makeMockGssContext(userName, password);
        when(gssContext.isEstablished()).thenReturn(false);
        MockRequest request = new MockRequest();
        doNegotiation(request);
        assertNull(request.getAuthorizationUsername());
        assertEquals(encodedPassword, request.getAuthorizationPassword());
    }

    @Test
    public void realmIsStrippedIfRequested() throws Exception {
        properties.setProperty("NegotiateAuthenticator.stripRealm", "true");
        String userName = "username@realm";
        String password = "password";
        String encodedPassword = base64Encode(password);
        GSSContext gssContext = makeMockGssContext(userName, password);
        when(gssContext.isEstablished()).thenReturn(true);
        MockRequest request = new MockRequest();
        doNegotiation(request);
        assertEquals("username", request.getAuthorizationUsername());
        assertEquals(encodedPassword, request.getAuthorizationPassword());
    }

    private String base64Encode(String s) {
        try {
            return new String(Base64.encode(s.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
    }

    @Test
    public void getTokenShouldReturnDecodedToken() {
        byte[] actual = NegotiateAuthenticator.getToken(NegotiateAuthenticator.NEGOTIATE + " " + TOKEN);
        byte[] expected;
        try {
            expected = Base64.decode(TOKEN.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        Assert.assertArrayEquals(expected, actual);
    }

}
