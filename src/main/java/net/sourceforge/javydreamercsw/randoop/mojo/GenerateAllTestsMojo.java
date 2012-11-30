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

    /**
     * Ignore class names specified by user that cannot be found. [default
     * false]
     */
    @Parameter(required = true, defaultValue = "false")
    private boolean silently_ignore_bad_class_names;
    /**
     * How to use literal values. See: ClassLiteralsMode.
     * [default NONE] 
     * NONE do not use literals specified in a literals file
     * 
     * CLASS a literal for a given class is used as input only to methods of
     * that class 
     * 
     * PACKAGE a literal is used as input to methods of any classes
     * in the same package 
     * 
     * ALL each literal is used as input to any method under
     * test
     */
    @Parameter(required = true, defaultValue = "NONE")
    private String literals_level;
    /**
     * The random seed to use in the generation process [default 0]
     */
    @Parameter(required = true, defaultValue = "0")
    private int randomseed;
    /**
     * Maximum number of seconds to spend generating tests [default 100]
     */
    @Parameter(required = true, defaultValue = "100")
    private int timelimit;
    /**
     * Maximum number of tests generated. 
     * Used to determine when to stop test 
     * generation. Generation stops when either the time limit (--timelimit=int) 
     * OR the input limit (--inputlimit=int) is reached. The number of tests 
     * output may be smaller than then number of inputs created, because 
     * redundant and illegal inputs may be discarded. Also see --outputlimit. 
     * [default 100000000]
     */
    @Parameter(required = true, defaultValue = "100000000")
    private int inputlimit;
    /**
     * Determines the maximum number of tests to output, 
     * no matter how many are generated. 
     * Contrast to --inputlimit. [default 100000000]
     */
    @Parameter(required = true, defaultValue = "100000000")
    private int outputlimit;
    /**
     * Do not generate tests with more than this many statements. [default 100]
     */
    @Parameter(required = true, defaultValue = "100")
    private int maxsize;
    /**
     * Never use null as input to methods or constructors. 
     * This option causes Randoop to abandon the method call rather than 
     * providing null as an input, when no non-null value of the appropriate 
     * type is available. To ask Randoop to calls methods with null with greater 
     * frequency, see option --null-ratio. [default true]
     */
    @Parameter(required = true, defaultValue = "true")
    private boolean forbid_null;
    /**
     * Maximum length of Strings in generated tests. [default 10000]
     */
    @Parameter(required = true, defaultValue = "10000")
    private int string_maxlen;
    /**
     * Use null with the given frequency. If a null ratio is given, 
     * it should be between 0 and 1. A ratio of X means that null will be 
     * used instead of a non-null value as a parameter to method calls, 
     * with X frequency (1 means always use null, 0 means never use null). 
     * For example, a ratio of 0.5 directs Randoop to use null inputs 50 
     * percent of the time. Randoop never uses null for receiver values. 
     * [default 0.0]
     */
    @Parameter(required = true, defaultValue = "0.0")
    private Double null_ratio;
    /**
     *  Try to reuse values from a sequence with the given frequency. 
     * If an alias ratio is given, it should be between 0 and 1. A ratio of 0 
     * results in tests where each value created within a test input is 
     * typically used at most once as an argument in a method call. A ratio 
     * of 1 tries to maximize the number of times values are used as inputs 
     * to parameters within a test. [default 0.0]
     */
    @Parameter(required = true, defaultValue = "0.0")
    private Double alias_ratio;
    /**
     * Favor shorter sequences when assembling new sequences out of old ones. 
     * Randoop generate new tests by combining old previously-generated tests. 
     * If this option is given, tests with fewer calls are given greater weight 
     * during its random selection. This has the overall effect of producing 
     * smaller JUnit tests. [default false]
     */
    @Parameter(required = true, defaultValue = "false")
    private boolean small_tests;
    /**
     * Clear the component set each time it contains the given number of inputs. 
     * Randoop stores previously-generated tests in a "component" set, and uses 
     * them to generate new tests. Setting this variable to a small number can 
     * sometimes result in a greater variety of tests generated during a single 
     * run. [default 100000000]
     */
    @Parameter(required = true, defaultValue = "100000000")
    private int clear;
    /**
     * Use Randoop's default set of object contracts. By default, Randoop 
     * checks a set of contracts, e.g. equals(Object) is reflexive, 
     * equals(null) returns false, no NullPointerExceptions, no AssertionErrors, 
     * etc. [default true]
     */
    @Parameter(required = true, defaultValue = "true")
    private boolean check_object_contracts;
    /**
     * What kinds of tests to output: pass, fail, or all. [default all]
     */
    @Parameter(required = true, defaultValue = "all")
    private String output_tests;
    /**
     * Maximum number of tests to write to each JUnit file. [default 500]
     */
    @Parameter(required = true, defaultValue = "500")
    private int testsperfile;
    /**
     * Base name (no ".java" suffix) of the JUnit file containing 
     * Randoop-generated tests. [default RandoopTest]
     */
    @Parameter(required = true, defaultValue = "RandoopTest")
    private String junit_classname;
    //TODO: No way to set defaultValue ="" on the annotation. 
    //Had to workaround it like this.
    /**
     * Name of the package for the generated JUnit files. [default ]
     */
    @Parameter
    private String junit_package_name = "";
    //TODO: No way to set defaultValue ="" on the annotation. 
    //Had to workaround it like this.
    /**
     * Name of the directory to which JUnit files should be written.
     */
    @Parameter
    private String junit_output_dir = "";
    /**
     * Run Randoop but do not create JUnit tests. [default false]
     */
    @Parameter(required = true, defaultValue = "false")
    private boolean dont_output_tests;
    /**
     * Output sequences even if they do not complete execution. Randoop's 
     * default behavior is to output only tests consisting of method call 
     * sequences that execute to the end, rather than throwing an exception 
     * or failing a contract check in the middle of execution. [default false]
     */
    @Parameter(required = true, defaultValue = "false")
    private boolean output_nonexec;
    //TODO: No way to set defaultValue ="" on the annotation. 
    //Had to workaround it like this.
    /**
     * Specify an extra command for recursive JVM calls that Randoop spawns. 
     * The argument to the --agent option is the entire extra JVM command. 
     * A typical invocation of Randoop might be:
     * java -javaagent:jarpath=args randoop.main.Main gentests --agent="-javaagent:jarpath=args"
     */
    @Parameter(readonly = true)
    private String agent = "";
    /**
     * Specify the memory size (in megabytes) for recursive JVM calls.
     * [default 1000]
     */
    @Parameter(required = true, defaultValue = "1000")
    private int mem_megabytes;
    /**
     * Capture all output to stdout and stderr. [default false]
     */
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