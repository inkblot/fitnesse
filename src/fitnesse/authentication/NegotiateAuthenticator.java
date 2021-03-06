package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import fitnesse.FitNesseModule;
import fitnesse.Responder;
import fitnesse.components.Base64;
import fitnesse.html.HtmlPage;
import fitnesse.html.HtmlPageFactory;
import fitnesse.html.HtmlUtil;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import org.ietf.jgss.*;
import util.ImpossibleException;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

/**
 * HTTP SPNEGO (GSSAPI Negotiate) authenticator.
 * <p/>
 * <strong>How to enable for Kerberos/Active Directory</strong>
 * <p/>
 * Enable this plugin by editing plugins.properties and adding the line:
 * <p/>
 * <pre>
 * Authenticator = fitnesse.authentication.NegotiateAuthenticator
 * </pre>
 * <p/>
 * If using Kerberos on Unix, create a jaas-krb5.conf file with these contents:
 * <p/>
 * <pre>
 * com.sun.security.jgss.accept  {
 *       com.sun.security.auth.module.Krb5LoginModule required
 *       storeKey=true
 *       isInitiator=false
 *       principal=&quot;HTTP/your.web.server@YOUR.REALM&quot;
 *       useKeyTab=true
 *       keyTab=&quot;/path/to/your/http.keytab&quot;
 *       ;
 *    };
 * </pre>
 * <p/>
 * Next, define these system properties when running the FitNesse server:
 * <p/>
 * <pre>
 * -Djavax.security.auth.useSubjectCredsOnly=false
 * -Djava.security.auth.login.config=/path/to/jaas-krb5.conf
 * -Dsun.security.krb5.debug=true
 * </pre>
 * <p/>
 * You can remove the krb5.debug property later, when you know it's working.
 *
 * @author David Leonard Released into the Public domain, 2009. No warranty:
 *         Provided as-is.
 */
public class NegotiateAuthenticator extends Authenticator {

    public static final String NEGOTIATE = "Negotiate";

    protected String serviceName;         /* Server's GSSAPI name, or null for default */
    protected Oid serviceNameType;        /* Name type of serviceName */
    protected Oid mechanism;              /* Restricted authentication mechanism, unless null */
    protected boolean stripRealm = true;  /* Strip the realm off the authenticated user's name */

    protected GSSManager manager;
    protected GSSCredential serverCreds;
    private final HtmlPageFactory htmlPageFactory;

    public NegotiateAuthenticator(GSSManager manager, Properties properties, WikiPage root, Injector injector, HtmlPageFactory htmlPageFactory) throws Exception {
        super(root, injector);
        this.manager = manager;
        this.htmlPageFactory = htmlPageFactory;
        configure(properties);
        initServiceCredentials();
    }

    @Inject
    public NegotiateAuthenticator(@Named(FitNesseModule.PROPERTIES_FILE) Properties properties, @Named(WikiModule.ROOT_PAGE) WikiPage root, Injector injector, HtmlPageFactory htmlPageFactory) throws Exception {
        this(GSSManager.getInstance(), properties, root, injector, htmlPageFactory);
    }

    protected void initServiceCredentials() throws Exception {
        if (serviceName == null)
            serverCreds = null;
        else {
            GSSName name = manager.createName(serviceName, serviceNameType, mechanism);
            serverCreds = manager.createCredential(name,
                    GSSCredential.INDEFINITE_LIFETIME, mechanism,
                    GSSCredential.ACCEPT_ONLY);
        }
    }

    protected void configure(Properties properties) throws Exception {
        serviceName = properties.getProperty("NegotiateAuthenticator.serviceName", null);
        serviceNameType = new Oid(properties.getProperty("NegotiateAuthenticator.serviceNameType",
                GSSName.NT_HOSTBASED_SERVICE.toString()));
        String mechanismProperty = properties.getProperty("NegotiateAuthenticator.mechanism", null);
        mechanism = mechanismProperty == null ? null : new Oid(mechanismProperty);
        stripRealm = Boolean.parseBoolean(properties.getProperty("NegotiateAuthenticator.stripRealm", "true"));
    }

    public GSSCredential getServerCredentials() {
        return serverCreds;
    }

    public Oid getServiceNameType() {
        return serviceNameType;
    }

    public Oid getMechanism() {
        return mechanism;
    }

    // Responder used when negotiation has not started or completed
    static protected class UnauthenticatedNegotiateResponder implements Responder {
        private final String token;
        private final HtmlPageFactory htmlPageFactory;

        public UnauthenticatedNegotiateResponder(final String token, HtmlPageFactory htmlPageFactory) {
            this.token = token;
            this.htmlPageFactory = htmlPageFactory;
        }

        public Response makeResponse(Request request)
                throws Exception {
            SimpleResponse response = new SimpleResponse(401);
            response.addHeader("WWW-Authenticate", token == null ? NEGOTIATE : NEGOTIATE + " " + token);
            HtmlPage html = htmlPageFactory.newPage();
            HtmlUtil.addTitles(html, "Negotiated authentication required");
            if (request == null)
                html.main.add("This request requires authentication");
            else
                html.main.add("Your client failed to complete required authentication");
            response.setContent(html.html());
            return response;
        }
    }

    @Override
    protected Responder unauthorizedResponder(Request request) {
        return new UnauthenticatedNegotiateResponder(request.getAuthorizationPassword(), htmlPageFactory);
    }

    /*
    * If negotiation succeeds, sets the username field in the request.
    * Otherwise, stores the next token to send in the password field and sets request username to null.
    * XXX It would be better to allow associating generic authenticator data to each request.
    */
    protected void negotiateCredentials(Request request) throws GSSException {
        String authHeader = (String) request.getHeader("Authorization");
        if (authHeader == null || !authHeader.toLowerCase().startsWith(NEGOTIATE.toLowerCase()))
            request.setCredentials(null, null);
        else {
            setCredentials(request, getToken(authHeader));
        }
    }

    static byte[] getToken(String authHeader) {
        byte[] inputTokenEncoded;
        try {
            inputTokenEncoded = authHeader.substring(NEGOTIATE.length()).trim().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        return Base64.decode(inputTokenEncoded);
    }

    private void setCredentials(Request request, byte[] inputToken) throws GSSException {
        /*
          * XXX Nowhere to attach a partial context to a TCP connection, so we are limited to
        * single-round auth mechanisms.
        */
        GSSContext gssContext = manager.createContext(serverCreds);
        byte[] replyTokenBytes = gssContext.acceptSecContext(inputToken, 0, inputToken.length);
        String replyToken;
        try {
            replyToken = replyTokenBytes == null ? null : new String(Base64.encode(replyTokenBytes), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new ImpossibleException("UTF-8 is a supported encoding", e);
        }
        if (!gssContext.isEstablished())
            request.setCredentials(null, replyToken);
        else {
            String authenticatedUser = gssContext.getSrcName().toString();

            if (stripRealm) {
                int at = authenticatedUser.indexOf('@');
                if (at != -1)
                    authenticatedUser = authenticatedUser.substring(0, at);
            }

            request.setCredentials(authenticatedUser, replyToken);
        }
    }

    @Override
    public Responder authenticate(Request request, Responder privilegedResponder) throws Exception {
        negotiateCredentials(request);
        return super.authenticate(request, privilegedResponder);
    }

    public boolean isAuthenticated(String username, String password) {
        return username != null;
    }

}
