// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.components;

import com.google.inject.Inject;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.wiki.*;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SaveRecorderTest extends FitnesseBaseTestCase {
    public WikiPage somePage;
    public WikiPage root;
    private PageCrawler crawler;
    private Clock clock;

    @Inject
    public void inject(Clock clock) {
        this.clock = clock;
    }

    @Before
    public void setUp() throws Exception {
        root = InMemoryPage.makeRoot("RooT", injector);
        crawler = root.getPageCrawler();
        somePage = crawler.addPage(root, PathParser.parse("SomePage"), "some page");
    }

    @Test
    public void testTiming() throws Exception {
        PageData data = somePage.getData();
        long savedTicket = 0;
        long editTicket = 1;
        long time = SaveRecorder.pageSaved(data, savedTicket, clock);
        somePage.commit(data);
        assertTrue(SaveRecorder.changesShouldBeMerged(time - 1, editTicket, somePage.getData()));
        assertFalse(SaveRecorder.changesShouldBeMerged(time + 1, editTicket, somePage.getData()));
    }

    @Test
    public void testDefaultValues() throws Exception {
        WikiPage neverSaved = crawler.addPage(root, PathParser.parse("NeverSaved"), "never saved");
        assertFalse(SaveRecorder.changesShouldBeMerged(12345, 0, neverSaved.getData()));
    }

    @Test
    public void testCanSaveOutOfOrderIfFromSameEditSession() throws Exception {
        PageData data = somePage.getData();
        long ticket = 99;
        long time = SaveRecorder.pageSaved(data, ticket, clock);
        somePage.commit(data);
        assertFalse(SaveRecorder.changesShouldBeMerged(time - 1, ticket, data));
    }

}
