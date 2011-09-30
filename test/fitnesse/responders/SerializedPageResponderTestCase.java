package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.wiki.*;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/30/11
 * Time: 8:22 AM
 */
public class SerializedPageResponderTestCase extends FitnesseBaseTestCase {
    protected PageCrawler crawler;
    protected WikiPage root;
    protected MockRequest request;
    protected FitNesseContext context;

    protected void doTestWith(Object obj) throws Exception {
        assertNotNull(obj);
        assertEquals(true, obj instanceof ProxyPage);
        WikiPage page = (WikiPage) obj;
        assertEquals("PageOne", page.getName());
    }

    protected Object doSetUpWith(String proxyType) throws Exception {
        WikiPage page1 = crawler.addPage(root, PathParser.parse("PageOne"), "this is page one");
        PageData data = page1.getData();
        data.setAttribute("Attr1", "true");
        page1.commit(data);
        crawler.addPage(page1, PathParser.parse("ChildOne"), "this is child one");

        request.addInput("type", proxyType);
        request.setResource("PageOne");

        return getObject();
    }

    protected Object getObject() throws Exception {
        Responder responder = new SerializedPageResponder();
        SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(response.getContentBytes()));
        return ois.readObject();
    }
}
