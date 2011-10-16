package fitnesse.authentication;

import fitnesse.http.Request;
import fitnesse.wiki.WikiPage;

public class InsecureOperation implements SecureOperation {
    public boolean shouldAuthenticate(WikiPage root, Request request) {
        return false;
    }
}
