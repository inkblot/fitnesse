// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.authentication;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MultiUserAuthenticatorTest extends FitnesseBaseTestCase {
    private File passwd;
    private MultiUserAuthenticator a;
    private WikiPage root;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Before
    public void setUp() throws Exception {
        String passwordFilename = "testpasswd";
        passwd = new File(passwordFilename);
        PrintStream ps = new PrintStream(new FileOutputStream(passwd));
        ps.println("uncle:bob");
        ps.println("micah:boy");
        ps.close();
        a = new MultiUserAuthenticator(passwordFilename, root, injector);
    }

    @After
    public void tearDown() throws Exception {
        passwd.delete();
    }

    @Test
    public void testBuildAuthenticator() throws Exception {
        assertEquals(2, a.userCount());
        assertEquals("bob", a.getPasswd("uncle"));
        assertEquals("boy", a.getPasswd("micah"));
    }

    @Test
    public void testAuthenticRequest() throws Exception {
        assertTrue(a.isAuthenticated("uncle", "bob"));
    }

    @Test
    public void testInauthenticRequest() throws Exception {
        assertFalse(a.isAuthenticated("bill", "boob"));
    }
}
