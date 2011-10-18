package fitnesse.responders;

import fitnesse.http.MockRequest;
import fitnesse.wiki.WikiPageFactory;
import org.junit.Before;
import org.junit.Test;
import util.FileUtil;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 9/30/11
 * Time: 8:20 AM
 */
public class SerializedPageResponderWithFileSystemTest extends SerializedPageResponderTestCase {

    @Override
    protected Properties getProperties() {
        Properties properties = super.getProperties();
        properties.remove(WikiPageFactory.WIKI_PAGE_CLASS);
        return properties;
    }

    @Before
    public void setUp() throws Exception {
        crawler = root.getPageCrawler();
        request = new MockRequest();
    }

    @Test
    public void testWithFileSystem() throws Exception {
        Object obj = doSetUpWith("bones");
        FileUtil.deleteFileSystemDirectory("RooT");
        doTestWith(obj);
    }

}
