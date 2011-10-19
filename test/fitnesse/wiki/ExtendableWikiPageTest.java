// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wiki;

import fitnesse.FitnesseBaseTestCase;
import fitnesse.testutil.MockExtendableWikiPage;
import fitnesse.testutil.SimpleExtension;
import org.junit.Test;

import static org.junit.Assert.*;

public class ExtendableWikiPageTest extends FitnesseBaseTestCase {
    @Test
    public void testAddExtention() throws Exception {
        Extension e = new SimpleExtension();
        WikiPage page = new MockExtendableWikiPage(e, injector);

        assertFalse(page.hasExtension("blah"));
        assertEquals(null, page.getExtension("blah"));

        assertTrue(page.hasExtension(e.getName()));
        assertSame(e, page.getExtension(e.getName()));
    }
}
