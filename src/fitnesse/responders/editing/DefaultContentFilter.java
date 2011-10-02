package fitnesse.responders.editing;

/**
 * Created by IntelliJ IDEA.
 * User: inkblot
 * Date: 10/1/11
 * Time: 8:27 AM
 */
public class DefaultContentFilter implements ContentFilter {
    @Override
    public boolean isContentAcceptable(String content, String page) {
        return true;
    }
}
