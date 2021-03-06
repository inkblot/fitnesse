package fitnesse.components;

import fitnesse.wiki.WikiPage;

import java.io.IOException;
import java.util.List;

public interface PageFinder {

    public abstract List<WikiPage> search(WikiPage page) throws IOException;

}