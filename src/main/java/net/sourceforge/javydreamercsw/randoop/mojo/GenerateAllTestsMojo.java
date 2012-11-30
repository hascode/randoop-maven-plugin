package net.sourceforge.javydreamercsw.randoop.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * Generate tests for a Maven project
 *
 * @see <a href="http://maven.apache.org/general.html#What_is_a_Mojo">What is a
 * Mojo?</a>
 */
@Mojo( name = "generate-all-tests",
requiresDependencyResolution = ResolutionScope.COMPILE,
requiresDirectInvocation = true,
defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class GenerateAllTestsMojo extends AbstractRandoopMojo {

    @Parameter(required = true, defaultValue = "false")
    private boolean silently_ignore_bad_class_names;
    @Parameter(required = true, defaultValue = "NONE")
    private String literals_level;
    @Parameter(required = true, defaultValue = "0")
    private int randomseed;
    @Parameter(required = true, defaultValue = "100")
    private int timelimit;
    @Parameter(required = true, defaultValue = "100000000")
    private int inputlimit;
    @Parameter(required = true, defaultValue = "100000000")
    private int outputlimit;
    @Parameter(required = true, defaultValue = "100")
    private int maxsize;
    @Parameter(required = true, defaultValue = "true")
    private boolean forbid_null;
    @Parameter(required = true, defaultValue = "10000")
    private int string_maxlen;
    @Parameter(required = true, defaultValue = "0.0")
    private Double null_ratio;
    @Parameter(required = true, defaultValue = "0.0")
    private Double alias_ratio;
    @Parameter(required = true, defaultValue = "false")
    private boolean small_tests;
    @Parameter(required = true, defaultValue = "100000000")
    private int clear;
    @Parameter(required = true, defaultValue = "true")
    private boolean check_object_contracts;
    @Parameter(required = true, defaultValue = "all")
    private String output_tests;
    @Parameter(required = true, defaultValue = "500")
    private int testsperfile;
    @Parameter(required = true, defaultValue = "RandoopTest")
    private String junit_classname;
    //TODO: No way to set defaultValue ="" on the annotation. 
    //Had to workaround it like this.
    @Parameter
    private String junit_package_name = "";
    //TODO: No way to set defaultValue ="" on the annotation. 
    //Had to workaround it like this.
    @Parameter
    private String junit_output_dir = "";
    @Parameter(required = true, defaultValue = "false")
    private boolean dont_output_tests;
    @Parameter(required = true, defaultValue = "false")
    private boolean output_nonexec;
    //TODO: No way to set defaultValue ="" on the annotation. 
    //Had to workaround it like this.
    @Parameter(readonly = true)
    private String agent = "";
    @Parameter(required = true, defaultValue = "1000")
    private int mem_megabytes;
    @Parameter(required = true, defaultValue = "false")
    private boolean capture_output;

    @Override
    public void execute() throws MojoExecutionException {
        final Runnable randoopProcess = new Runnable() {
            @Override
            public void run() {
                try {
                    getLog().info("Running Randoop Mojo on project: "
                            + project.getName());
                    if (isSmartGeneration()) {
                        getLog().info("Smart generation enabled!");
                    }
                    for (ArrayList<String> commands : generateCommands()) {
                        getLog().debug("Running command(s): "
                                + commands.toString().replaceAll(",", ""));
                        try {
                            ProcessBuilder pb = new ProcessBuilder(commands);
                            pb.directory(project.getBasedir());
                            getLog().info("Executing command on path: "
                                    + pb.directory().getPath());
                            pb.redirectErrorStream(true);
                            String line;
                            getLog().info("Generating tests, please wait...");
                            Process process = pb.start();
                            InputStream stderr = process.getErrorStream();
                            InputStream stdout = process.getInputStream();
                            // clean up any output in stdout
                            BufferedReader brCleanUp =
                                    new BufferedReader(new InputStreamReader(stdout));
                            String mess = "";
                            while ((line = brCleanUp.readLine()) != null) {
                                mess += "\n" + line;
                            }
                            if (!mess.isEmpty() && !mess.startsWith("Error")) {
                                getLog().info("[Randoop] " + mess);
                            }
                            brCleanUp.close();
                            mess = "";
                            // clean up any output in stderr
                            brCleanUp =
                                    new BufferedReader(new InputStreamReader(stderr));
                            while ((line = brCleanUp.readLine()) != null) {
                                mess += "\n" + line;
                            }
                            if (!mess.isEmpty() || mess.startsWith("Error")) {
                                getLog().error("[Randoop Error] " + mess);
                            }
                            getLog().info("Done generating tests!");
                        } catch (IOException e) {
                            getLog().error(e);
                        }
                    }
                } catch (FileNotFoundException ex) {
                    getLog().debug(ex);
                } catch (MojoExecutionException ex) {
                    getLog().debug(ex);
                } catch (MojoFailureException ex) {
                    getLog().debug(ex);
                } catch (IOException ex) {
                    getLog().debug(ex);
                }
            }
        };
        Thread thread = new Thread(randoopProcess, "Randoop Maven Plugin");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Mojo failed", e);
        }
    }

    /**
     * @return the silentlyIgnoreBadClassNames
     */
    public boolean getSilentlyIgnoreBadClassNames() {
        return silently_ignore_bad_class_names;
    }

    /**
     * @return the literalsLevel
     */
    public String getLiteralsLevel() {
        return literals_level;
    }

    /**
     * @return the randomseed
     */
    public int getRandomseed() {
        return randomseed;
    }

    /**
     * @return the timelimit
     */
    public int getTimelimit() {
        return timelimit;
    }

    /**
     * @return the inputlimit
     */
    public int getInputlimit() {
        return inputlimit;
    }

    /**
     * @return the outputlimit
     */
    public int getOutputlimit() {
        return outputlimit;
    }

    /**
     * @return the maxsize
     */
    public int getMaxsize() {
        return maxsize;
    }

    /**
     * @return the forbidNull
     */
    public boolean getForbidNull() {
        return forbid_null;
    }

    /**
     * @return the stringMaxlen
     */
    public int getStringMaxlen() {
        return string_maxlen;
    }

    /**
     * @return the nullRatio
     */
    public Double getNullRatio() {
        return null_ratio;
    }

    /**
     * @return the aliasRatio
     */
    public Double getAliasRatio() {
        return alias_ratio;
    }

    /**
     * @return the smallTests
     */
    public boolean getSmallTests() {
        return small_tests;
    }

    /**
     * @return the clear
     */
    public int getClear() {
        return clear;
    }

    /**
     * @return the checkObjectContracts
     */
    public boolean getCheckObjectContracts() {
        return check_object_contracts;
    }

    /**
     * @return the outputTests
     */
    public String getOutputTests() {
        return output_tests;
    }

    /**
     * @return the testsperfile
     */
    public int getTestsperfile() {
        return testsperfile;
    }

    /**
     * @return the junitClassname
     */
    public String getJunitClassname() {
        return junit_classname;
    }

    /**
     * @return the junitPackageName
     */
    public String getJunitPackageName() {
        return junit_package_name;
    }

    /**
     * @return the junitOutputDir
     */
    public String getJunitOutputDir() {
        return junit_output_dir;
    }

    /**
     * @return the dontOutputTests
     */
    public boolean getDontOutputTests() {
        return dont_output_tests;
    }

    /**
     * @return the outputNonexec
     */
    public boolean getOutputNonexec() {
        return output_nonexec;
    }

    /**
     * @return the agent
     */
    public String getAgent() {
        return agent;
    }

    /**
     * @return the memMegabytes
     */
    public int getMemMegabytes() {
        return mem_megabytes;
    }

    /**
     * @return the captureOutput
     */
    public boolean getCaptureOutput() {
        return capture_output;
    }
}