// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FitNesseContextTest extends FitnesseBaseTestCase {

    @Test
    public void shouldReportPortOfMinusOneIfNotInitialized() {
        FitNesseContext.globalContext = null;
        assertEquals(-1, FitNesseContext.getPort());
    }

    @Test
    public void shouldHavePortSetAfterFitNesseObjectConstructed() throws Exception {
        FitNesseContext context = makeContext();
        context.port = 9988;
        new FitNesse(context, false);
        assertEquals(9988, FitNesseContext.getPort());
    }
}
