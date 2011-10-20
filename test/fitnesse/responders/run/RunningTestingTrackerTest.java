// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.FitnesseBaseTestCase;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RunningTestingTrackerTest extends FitnesseBaseTestCase {

    private RunningTestingTracker runningTestingTracker;

    @Inject
    public void inject(RunningTestingTracker runningTestingTracker) {
        this.runningTestingTracker = runningTestingTracker;
    }

    @Test
    public void testAddStoppable() {
        StoppedRecorder stoppableA = new StoppedRecorder();
        StoppedRecorder stoppableB = new StoppedRecorder();

        runningTestingTracker.addStartedProcess(stoppableA);
        runningTestingTracker.addStartedProcess(stoppableB);

        runningTestingTracker.stopAllProcesses();
        assertTrue(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());
    }

    @Test
    public void testRemoveStoppable() {
        StoppedRecorder stoppableA = new StoppedRecorder();
        StoppedRecorder stoppableB = new StoppedRecorder();
        StoppedRecorder stoppableC = new StoppedRecorder();

        runningTestingTracker.addStartedProcess(stoppableA);
        String idB = runningTestingTracker.addStartedProcess(stoppableB);
        runningTestingTracker.addStartedProcess(stoppableC);

        runningTestingTracker.removeEndedProcess(idB);

        runningTestingTracker.stopAllProcesses();
        assertTrue(stoppableA.wasStopped());
        assertFalse(stoppableB.wasStopped());
        assertTrue(stoppableC.wasStopped());
    }


    @Test
    public void testStopProcess() {
        StoppedRecorder stoppableA = new StoppedRecorder();
        StoppedRecorder stoppableB = new StoppedRecorder();
        StoppedRecorder stoppableC = new StoppedRecorder();

        runningTestingTracker.addStartedProcess(stoppableA);
        String idB = runningTestingTracker.addStartedProcess(stoppableB);
        runningTestingTracker.addStartedProcess(stoppableC);
        String results = runningTestingTracker.stopProcess(idB);

        assertFalse(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());
        assertFalse(stoppableC.wasStopped());

        assertTrue(results.contains("1"));
    }

    @Test
    public void testStopAllProcesses() {
        StoppedRecorder stoppableA = new StoppedRecorder();
        StoppedRecorder stoppableB = new StoppedRecorder();
        StoppedRecorder stoppableC = new StoppedRecorder();

        runningTestingTracker.addStartedProcess(stoppableA);
        runningTestingTracker.addStartedProcess(stoppableB);
        runningTestingTracker.addStartedProcess(stoppableC);

        String results = runningTestingTracker.stopAllProcesses();

        assertTrue(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());
        assertTrue(stoppableC.wasStopped());

        assertTrue(results.contains("3"));
    }


    class StoppedRecorder implements Stoppable {
        private boolean wasStopped = false;

        public synchronized void stop() throws Exception {
            wasStopped = true;
        }

        public synchronized boolean wasStopped() {
            return wasStopped;
        }
    }
}
