package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.testutil.FitSocketReceiver;
import org.junit.Before;
import org.junit.Test;

import java.net.BindException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;

public class StopTestResponderTest extends FitnesseBaseTestCase {

    private Request request = null;
    private StoppedRecorder stoppableA = new StoppedRecorder();
    private StoppedRecorder stoppableB = new StoppedRecorder();
    private HtmlPageFactory htmlPageFactory;
    private SocketDealer socketDealer;
    private RunningTestingTracker runningTestingTracker;

    @Override
    protected int getPort() {
        return 9124;
    }

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, SocketDealer socketDealer, RunningTestingTracker runningTestingTracker) {
        this.htmlPageFactory = htmlPageFactory;
        this.socketDealer = socketDealer;
        this.runningTestingTracker = runningTestingTracker;
    }

    @Before
    public void setUp() throws Exception {
        request = new MockRequest();
    }

    @Test
    public void testStopAll() throws Exception {
        runningTestingTracker.addStartedProcess(stoppableA);
        runningTestingTracker.addStartedProcess(stoppableB);

        StopTestResponder stopResponder = new StopTestResponder(htmlPageFactory, runningTestingTracker);
        String response = runResponder(stopResponder);

        assertTrue(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());

        assertSubString("all", response);
        assertSubString("2", response);

    }

    @Test
    public void testStopB() throws Exception {
        runningTestingTracker.addStartedProcess(stoppableA);
        final String bId = runningTestingTracker.addStartedProcess(stoppableB);

        request = new MockRequest() {
            @Override
            public boolean hasInput(String key) {
                return ("id".equalsIgnoreCase(key));
            }

            @Override
            public Object getInput(String key) {
                return bId;
            }
        };

        StopTestResponder stopResponder = new StopTestResponder(htmlPageFactory, runningTestingTracker);
        String response = runResponder(stopResponder);

        assertFalse(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());

        assertSubString("Stopped 1 test", response);
    }


    private String runResponder(StopTestResponder responder) throws Exception {
        FitSocketReceiver fitSocketReceiver = new FitSocketReceiver(getPort(), socketDealer);
        try {
            fitSocketReceiver.receiveSocket();
            Response response = responder.makeResponse(request);
            MockResponseSender sender = new MockResponseSender();
            sender.doSending(response);
            return sender.sentData();
        } catch (BindException e) {
            fitSocketReceiver = null;
            throw e;
        } finally {
            if (fitSocketReceiver != null)
                fitSocketReceiver.close();
        }
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