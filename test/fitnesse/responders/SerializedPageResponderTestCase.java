package fitnesse.responders;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseModule;
import fitnesse.Responder;
import fitnesse.FitnesseBaseTestCase;
import fitnesse.html.HtmlPageFactory;
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
public abstract class SerializedPageResponderTestCase extends FitnesseBaseTestCase {
    protected PageCrawler crawler;
    protected WikiPage root;
    protected MockRequest request;
    protected FitNesseContext context;
    protected HtmlPageFactory htmlPageFactory;

    @Inject
    public void inject(HtmlPageFactory htmlPageFactory, @Named(FitNesseModule.ROOT_PAGE) WikiPage root, FitNesseContext context) {
        this.htmlPageFactory = htmlPageFactory;
        this.root = root;
        this.context = context;
    }

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
        Responder responder = new SerializedPageResponder(htmlPageFactory, root);
        SimpleResponse response = (SimpleResponse) responder.makeResponse(request);

        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(response.getContentBytes()));
        return ois.readObject();
    }
}
