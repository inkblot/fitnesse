// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesseMain.ant;

import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Reference;

/**
 * Task to start fitnesse.
 * <p/>
 * <pre>
 *    Usage:
 *    &lt;taskdef name=&quot;start-fitnesse&quot; classname=&quot;fitnesse.ant.StartFitnesseTask&quot; classpathref=&quot;classpath&quot; /&gt;
 *    OR
 *    &lt;taskdef classpathref=&quot;classpath&quot; resource=&quot;tasks.properties&quot; /&gt;
 *
 *    &lt;start-fitnesse wikidirectoryrootpath=&quot;.&quot; fitnesseport=&quot;8082&quot; /&gt;
 * </pre>
 */
public class StartFitnesseTask extends Task {
    private String wikiDirectoryRootPath;
    private int fitnessePort = 8082;
    private Reference classpathRef;

    @Override
    public void execute() throws BuildException {
        Java java = new Java();
        java.setProject(getProject());
        java.setOwningTarget(getOwningTarget());

        java.setFailonerror(true);
        java.setClassname("fitnesseMain.FitNesseMain");
        java.setClasspathRef(getClasspathRef());

        String[] argv = {"-p", String.valueOf(getFitnessePort()), "-d", getWikiDirectoryRootPath(), "-e", "0", "-o"};
        java.setArgs(StringUtils.join(argv, " "));

        super.execute();
    }

    /**
     * Port on which fitnesse would run. Defaults to <b>8082</b>.
     *
     * @return fitnessePort
     */
    public int getFitnessePort() {
        return fitnessePort;
    }

    public void setFitnessePort(int fitnessePort) {
        this.fitnessePort = fitnessePort;
    }

    /**
     * Path to the FitnesseRoot filder which contains all the wiki pages. <b>MUST SET</b>.
     *
     * @return wikiDirectoryRootPath
     */
    public String getWikiDirectoryRootPath() {
        return wikiDirectoryRootPath;
    }

    public void setWikiDirectoryRootPath(String wikiDirectoryRootPath) {
        this.wikiDirectoryRootPath = wikiDirectoryRootPath;
    }

    /**
     * FitNesse's classpath.
     *
     * @return a Reference representing the complete classpath for FitNesse
     */
    public Reference getClasspathRef() {
        return classpathRef;
    }

    public void setClasspathRef(Reference classpathRef) {
        this.classpathRef = classpathRef;
    }
}
