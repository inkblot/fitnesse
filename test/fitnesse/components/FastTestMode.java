package fitnesse.components;

import fit.FitServer;
import fitnesse.http.MockCommandRunner;

import java.util.Map;

/**
* Created by IntelliJ IDEA.
* User: inkblot
* Date: 10/22/11
* Time: 12:09 AM
*/
public class FastTestMode implements CommandRunningFitClient.FitTestMode {

    private Thread fastFitServer;

    void createFitServer(String args) {
        final String fitArgs = args;

        Runnable fastFitServerRunnable = new Runnable() {
            public void run() {
                try {
                    while (!tryCreateFitServer(fitArgs))
                        Thread.sleep(10);
                } catch (InterruptedException e) {
                    // ok
                }
            }
        };
        fastFitServer = new Thread(fastFitServerRunnable);
        fastFitServer.start();
    }

    private boolean tryCreateFitServer(String args) {
        try {
            FitServer.runFitServer(args.trim().split(" "));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public CommandRunner initialize(String fitArguments, String command, Map<String, String> environmentVariables) {
        createFitServer(fitArguments);
        return new MockCommandRunner();
    }

    @Override
    public void join(CommandRunningFitClient commandRunningFitClient) throws InterruptedException {
        fastFitServer.join();
    }

    @Override
    public void start(CommandRunningFitClient commandRunningFitClient) {
    }

    @Override
    public void killVigilantThreads() {
    }
}
