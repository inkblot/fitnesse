package fitnesse.responders.files;

import com.google.inject.Inject;
import fitnesse.FitNesseVersion;
import fitnesse.http.Request;
import util.Clock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/13/11
 * Time: 7:37 AM
 */
public class ClassPathResponder extends FileResponder {

    @Inject
    public ClassPathResponder(Clock clock) {
        super(clock);
    }

    @Override
    public InputStream getFileStream(Request request) throws IOException {
        return Thread.currentThread().getContextClassLoader().getResourceAsStream("Resources/FitNesseRoot/" + request.getResource());
    }

    @Override
    public Integer getContentLength(Request request) {
        return null;
    }

    @Override
    public Date getLastModifiedDate(Request request) {
        return FitNesseVersion.getBuildDate();
    }

    public static boolean exists(String resource) {
        return Thread.currentThread().getContextClassLoader().getResource("Resources/FitNesseRoot/" + resource) != null;
    }
}
