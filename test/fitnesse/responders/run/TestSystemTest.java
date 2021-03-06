package fitnesse.responders.run;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import fitnesse.*;
import fitnesse.responders.run.TestSystem.Descriptor;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiModule;
import fitnesse.wiki.WikiPage;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSystemTest extends FitnesseBaseTestCase {
    private WikiPage root;

    @Inject
    public void inject(@Named(WikiModule.ROOT_PAGE) WikiPage root) {
        this.root = root;
    }

    @Test
    public void testCommandPatternCSharp() throws Exception {
        String specifiedPageText = "!define COMMAND_PATTERN {%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p}\n";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage.getData(), false);
        assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", defaultDescriptor2.commandPattern);

        Descriptor defaultDescriptor3 = TestSystem.getDescriptor(specifiedPage.getData(), true);
        assertEquals("%m -r fitSharp.Slim.Service.Runner,fitsharp.dll %p", defaultDescriptor3.commandPattern);
    }


    @Test
    public void testCommandPatternJava() throws Exception {

        String pageText = "!define TEST_SYSTEM {slim}\n";
        WikiPage page = makeTestPage(pageText);

        // TODO: The command pattern got complicated.... how to assert its value...
//        Descriptor defaultDescriptor = TestSystem.getDescriptor(page.getData(), false);
//        String sep = File.pathSeparator;
//        assertEquals("java -cp fitnesse.jar" + sep + "%p %m", defaultDescriptor.commandPattern);

        Descriptor debugDescriptor = TestSystem.getDescriptor(page.getData(), true);
        assertEquals(
                "java -Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -cp %p %m",
                debugDescriptor.commandPattern);

        String specifiedPageText = "!define COMMAND_PATTERN {java -specialParam -cp %p %m}\n"
                + "!define REMOTE_DEBUG_COMMAND {java -remoteDebug -cp %p %m}";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage.getData(), false);
        assertEquals("java -specialParam -cp %p %m", defaultDescriptor2.commandPattern);

        Descriptor debugDescriptor2 = TestSystem.getDescriptor(specifiedPage.getData(), true);
        assertEquals("java -remoteDebug -cp %p %m", debugDescriptor2.commandPattern);
    }

    @Test
    public void testCommandPatternWithVariable() throws Exception {
        String specifiedPageText = "!define COMMAND_PATTERN (${MY_RUNNER} %p %m)\n"
                + "!define MY_RUNNER {rubyslim}\n";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage.getData(), false);
        assertEquals("rubyslim %p %m", myDescriptor.commandPattern);
    }

    @Test
    public void testTestRunnerWithVariable() throws Exception {
        String specifiedPageText = "!define TEST_RUNNER (${MY_RUNNER}.rb)\n"
                + "!define MY_RUNNER {rubyslim}\n";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage.getData(), false);
        assertEquals("rubyslim.rb", myDescriptor.testRunner);
    }

    @Test
    public void testRunnerCSharp() throws Exception {
        String specifiedPageText = "!define TEST_RUNNER {..\\fitnesse\\fitsharp\\Runner.exe}";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor defaultDescriptor2 = TestSystem.getDescriptor(specifiedPage.getData(), false);
        assertEquals("..\\fitnesse\\fitsharp\\Runner.exe", defaultDescriptor2.testRunner);
        Descriptor defaultDescriptor3 = TestSystem.getDescriptor(specifiedPage.getData(), true);
        assertEquals("..\\fitnesse\\fitsharp\\runnerw.exe", defaultDescriptor3.testRunner);
    }

    @Test
    public void testRunnerDefault() throws Exception {
        String pageText = "!define TEST_SYSTEM {slim}\n";
        WikiPage page = makeTestPage(pageText);

        Descriptor defaultDescriptor2 = TestSystem.getDescriptor(page.getData(), false);
        assertEquals("fitnesse.slim.SlimService", defaultDescriptor2.testRunner);
        Descriptor defaultDescriptor3 = TestSystem.getDescriptor(page.getData(), true);
        assertEquals("fitnesse.slim.SlimService", defaultDescriptor3.testRunner);

        String specifiedPageText = "!define REMOTE_DEBUG_RUNNER {Different runner}";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor specifiedDescriptor = TestSystem.getDescriptor(specifiedPage.getData(), true);
        assertEquals("Different runner", specifiedDescriptor.testRunner);
    }

    WikiPage makeTestPage(String pageText) throws Exception {
        PageCrawler crawler = root.getPageCrawler();
        return crawler.addPage(root, PathParser.parse("TestPage"), pageText);
    }

    @Test
    public void testTestRunnerWithRootPathVariable() throws Exception {
        injector.getInstance(FitNesse.class);

        String specifiedPageText = "!define TEST_RUNNER (${FITNESSE_ROOTPATH}/rubyslim.rb)\n";
        WikiPage specifiedPage = makeTestPage(specifiedPageText);

        Descriptor myDescriptor = TestSystem.getDescriptor(specifiedPage.getData(), false);
        assertEquals(getRootPath() + "/rubyslim.rb", myDescriptor.testRunner);
    }

}
