package fitnesse.wikitext.test;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.wiki.*;

public class TestRoot {
    public WikiPage root;
    private PageCrawler crawler;

    @Inject
    public TestRoot(@Named(WikiModule.ROOT_PAGE) WikiPage root) throws Exception {
        this.root = root;
        crawler = this.root.getPageCrawler();
    }

    public WikiPage makePage(String pageName) throws Exception {
        return makePage(root, pageName);
    }

    public WikiPage makePage(WikiPage parent, String pageName) throws Exception {
        return crawler.addPage(parent, PathParser.parse(pageName));
    }

    public WikiPage makePage(String pageName, String content) throws Exception {
        return makePage(root, pageName, content);
    }

    public WikiPage makePage(WikiPage parent, String pageName, String content) throws Exception {
        WikiPage page = makePage(parent, pageName);
        setPageData(page, content);
        return page;
    }

    public void setPageData(WikiPage page, String content) throws Exception {
        PageData data = page.getData();
        data.setContent(content);
        page.commit(data);
    }
}
