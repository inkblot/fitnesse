// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders;

import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.PageData;
import fitnesse.wiki.WikiPageProperties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImportAndViewResponderTest extends ImporterTestCase {
    private ImportAndViewResponder responder;

    @Before
    public void setUp() throws Exception {
        fitNesseUtil.startFitnesse(remoteContext);
        responder = new ImportAndViewResponder();
    }

    @After
    public void tearDown() throws Exception {
        fitNesseUtil.stopFitnesse();
    }

    @Test
    public void testRedirect() throws Exception {
        Response response = getResponse();

        assertEquals(303, response.getStatus());
        assertEquals("PageTwo", response.getHeader("Location"));
    }

    private Response getResponse() throws Exception {
        MockRequest request = new MockRequest();
        request.setResource("PageTwo");
        return responder.makeResponse(localContext, request);
    }

    @Test
    public void testPageContentIsUpdated() throws Exception {
        PageData data = pageTwo.getData();
        WikiPageProperties props = data.getProperties();

        WikiImportProperty importProps = new WikiImportProperty(FitNesseUtil.URL + "PageTwo");
        importProps.addTo(props);
        pageTwo.commit(data);

        getResponse();

        data = pageTwo.getData();
        assertEquals("page two", data.getContent());
    }
}
