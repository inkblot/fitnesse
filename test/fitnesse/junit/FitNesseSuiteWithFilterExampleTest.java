package fitnesse.junit;

import fitnesse.junit.FitNesseSuite.*;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith(FitNesseSuite.class)
@Name("FitNesse.SuiteAcceptanceTests.SuiteSlimTests")
@FitnesseDir("..")
@OutputDir(systemProperty = "java.io.tmpdir", pathExtension = "fitnesse")
@SuiteFilter("testSuite")
@DebugMode(true)
public class FitNesseSuiteWithFilterExampleTest {
    @Test
    public void dummy() {

    }
}
