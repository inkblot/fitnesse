// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.MockRequest;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SecureOperationTest extends FitnesseBaseTestCase {
    private SecureReadOperation sro;
    private WikiPage root;
    private MockRequest request;
    private PageCrawler crawler;
    private WikiPagePath parentPagePath;
    private WikiPagePath childPagePath;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        sro = new SecureReadOperation();
        request = new MockRequest();
        crawler = root.getPageCrawler();
        parentPagePath = PathParser.parse("ParentPage");
        childPagePath = PathParser.parse("ChildPage");
    }

    @Test
    public void testNormalPageDoesNotRequireAuthentication() throws Exception {
        String insecurePageName = "InsecurePage";
        WikiPagePath insecurePagePath = PathParser.parse(insecurePageName);
        crawler.addPage(root, insecurePagePath);
        request.setResource(insecurePageName);
        assertFalse(sro.shouldAuthenticate(root, request));
    }

    @Test
    public void testReadSecurePageRequresAuthentication() throws Exception {
        String securePageName = "SecurePage";
        WikiPagePath securePagePath = PathParser.parse(securePageName);
        WikiPage securePage = crawler.addPage(root, securePagePath);
        makeSecure(securePage);
        request.setResource(securePageName);
        assertTrue(sro.shouldAuthenticate(root, request));
    }

    private void makeSecure(WikiPage securePage) throws Exception {
        PageData data = securePage.getData();
        data.setAttribute(PageData.PropertySECURE_READ);
        securePage.commit(data);
    }

    @Test
    public void testChildPageOfSecurePageRequiresAuthentication() throws Exception {
        WikiPage parentPage = crawler.addPage(root, parentPagePath);
        makeSecure(parentPage);
        crawler.addPage(parentPage, childPagePath);
        request.setResource("ParentPage.ChildPage");
        assertTrue(sro.shouldAuthenticate(root, request));
    }

    @Test
    public void testNonExistentPageCanBeAuthenticated() throws Exception {
        request.setResource("NonExistentPage");
        assertFalse(sro.shouldAuthenticate(root, request));
    }

    @Test
    public void testParentOfNonExistentPageStillSetsPriviledges() throws Exception {
        WikiPage parentPage = crawler.addPage(root, parentPagePath);
        makeSecure(parentPage);
        request.setResource("ParentPage.NonExistentPage");
        assertTrue(sro.shouldAuthenticate(root, request));
    }

    @Test
    public void testChildPageIsRestricted() throws Exception {
        WikiPage parentPage = crawler.addPage(root, parentPagePath);
        WikiPage childPage = crawler.addPage(parentPage, childPagePath);
        makeSecure(childPage);
        request.setResource("ParentPage.ChildPage");
        assertTrue(sro.shouldAuthenticate(root, request));
    }

    @Test
    public void testBlankResource() throws Exception {
        request.setResource("");
        assertFalse(sro.shouldAuthenticate(root, request));
    }
}
