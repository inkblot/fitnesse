package fitnesse.responders.run;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
import fitnesse.http.MockRequest;
import fitnesse.http.MockResponseSender;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.testutil.FitSocketReceiver;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static util.RegexAssertions.assertSubString;


public class StopTestResponderTest extends FitnesseBaseTestCase {

    private Request request = null;
    private FitNesseContext context = null;
    private StoppedRecorder stoppableA = new StoppedRecorder();
    private StoppedRecorder stoppableB = new StoppedRecorder();
    private HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory) {
        this.htmlPageFactory = htmlPageFactory;
    }

    @Before
    public void setUp() throws Exception {

        request = new MockRequest();
        context = makeContext();
    }

    @Test
    public void testStopAll() throws Exception {
        context.runningTestingTracker.addStartedProcess(stoppableA);
        context.runningTestingTracker.addStartedProcess(stoppableB);

        StopTestResponder stopResponder = new StopTestResponder(htmlPageFactory);
        String response = runResponder(stopResponder);

        assertTrue(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());

        assertSubString("all", response);
        assertSubString("2", response);

    }

    @Test
    public void testStopB() throws Exception {
        context.runningTestingTracker.addStartedProcess(stoppableA);
        final String bId = context.runningTestingTracker.addStartedProcess(stoppableB);

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

        StopTestResponder stopResponder = new StopTestResponder(htmlPageFactory);
        String response = runResponder(stopResponder);

        assertFalse(stoppableA.wasStopped());
        assertTrue(stoppableB.wasStopped());

        assertSubString("Stopped 1 test", response);
    }


    private String runResponder(StopTestResponder responder) throws Exception {
        context.port = new FitSocketReceiver(0, context.socketDealer).receiveSocket();
        Response response = responder.makeResponse(context, request);
        MockResponseSender sender = new MockResponseSender();
        sender.doSending(response);
        return sender.sentData();
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