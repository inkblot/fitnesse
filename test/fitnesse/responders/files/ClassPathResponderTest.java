package fitnesse.responders.files;

import com.google.inject.Inject;
import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.http.*;
import org.junit.Before;
import org.junit.Test;
import util.Clock;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static util.RegexAssertions.assertSubString;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/13/11
 * Time: 7:29 PM
 */
public class ClassPathResponderTest extends FitnesseBaseTestCase {

    private ClassPathResponder responder;
    private Clock clock;
    private FitNesseContext context;

    @Inject
    public void inject(Clock clock) {
        this.clock = clock;
    }

    @Before
    public void setUp() throws Exception {
        context = makeContext();
        responder = new ClassPathResponder(clock);
    }

    @Test
    public void requestForResource() throws Exception {
        MockRequest request = new MockRequest();
        MockResponseSender sender = new MockResponseSender();
        request.setResource("files/css/fitnesse_base.css");

        Response response = responder.makeResponse(context, request);
        sender.doSending(response);
        assertThat(response, instanceOf(InputStreamResponse.class));
        assertThat(response.getStatus(), equalTo(200));
        assertSubString("a:hover", sender.sentData());
    }

}
