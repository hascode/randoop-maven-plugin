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
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * Goal which touches a timestamp file.
 *
 * @goal generate-all-tests
 * @requiresDependencyResolution compile
 * @requiresDirectInvocation true
 * @phase generate-resources
 * process-sources
 */
public class RandoopMojo extends AbstractMojo {

    @Parameter(required = true, readonly = true, defaultValue = "${project}")
    MavenProject project;
    @Parameter(required = true, readonly = true, defaultValue = "false")
    private boolean silently_ignore_bad_class_names;
    @Parameter(required = true, readonly = true, defaultValue = "NONE")
    private String literals_level;
    @Parameter(required = true, readonly = true, defaultValue = "0")
    private int randomseed;
    @Parameter(required = true, readonly = true, defaultValue = "100")
    private int timelimit;
    @Parameter(required = true, readonly = true, defaultValue = "100000000")
    private int inputlimit;
    @Parameter(required = true, readonly = true, defaultValue = "100000000")
    private int outputlimit;
    @Parameter(required = true, readonly = true, defaultValue = "100")
    private int maxsize;
    @Parameter(required = true, readonly = true, defaultValue = "true")
    private boolean forbid_null;
    @Parameter(required = true, readonly = true, defaultValue = "10000")
    private int string_maxlen;
    @Parameter(required = true, readonly = true, defaultValue = "0.0")
    private Double null_ratio;
    @Parameter(required = true, readonly = true, defaultValue = "0.0")
    private Double alias_ratio;
    @Parameter(required = true, readonly = true, defaultValue = "false")
    private boolean small_tests;
    @Parameter(required = true, readonly = true, defaultValue = "100000000")
    private int clear;
    @Parameter(required = true, readonly = true, defaultValue = "true")
    private boolean check_object_contracts;
    @Parameter(required = true, readonly = true, defaultValue = "all")
    private String output_tests;
    @Parameter(required = true, readonly = true, defaultValue = "500")
    private int testsperfile;
    @Parameter(required = true, readonly = true, defaultValue = "RandoopTest")
    private String junit_classname;
    @Parameter(required = true, readonly = true, defaultValue = "")
    private String junit_package_name;
    @Parameter(required = true, readonly = true, defaultValue = "")
    private String junit_output_dir;
    @Parameter(required = true, readonly = true, defaultValue = "false")
    private boolean dont_output_tests;
    @Parameter(required = true, readonly = true, defaultValue = "false")
    private boolean output_nonexec;
    @Parameter(required = true, readonly = true, defaultValue = "")
    private String agent;
    @Parameter(required = true, readonly = true, defaultValue = "1000")
    private int mem_megabytes;
    @Parameter(required = true, readonly = true, defaultValue = "false")
    private boolean capture_output;
    /**
     * Project classpath.
     */
    @Parameter( defaultValue = "${project.compileClasspathElements}",
    readonly = true, required = true)
    private List<String> classpathElements;

    @Override
    public void execute() throws MojoExecutionException {
        final Runnable randoopProcess = new Runnable() {
            @Override
            public void run() {
                getLog().info("Running Randoop Mojo on project: "
                        + project.getName());
                ArrayList<String> commands = new ArrayList<String>();
                commands.add("java");
                commands.add("-ea");
                commands.add("-classpath");
                StringBuilder classpath = new StringBuilder();
                getLog().info("Sources:");
                for (Object obj : project.getCompileSourceRoots()) {
                    getLog().info(obj.toString());
                }
                getLog().info("Artifacts:");
                for (Object obj : project.getCompileArtifacts()) {
                    getLog().info(obj.toString());
                }
                getLog().info("Dependencies:");
                for (Object obj : project.getCompileDependencies()) {
                    getLog().info(obj.toString());
                }
                try {
                    getLog().info("Classpath:");
                    for (Object obj : project.getCompileClasspathElements()) {
                        getLog().info(obj == null ? "null" : obj.toString());
                    }
                } catch (DependencyResolutionRequiredException ex) {
                    getLog().error(ex);
                }
                getLog().info("Test directory: " + project.getModel().getBuild().getTestSourceDirectory());
                getLog().info("Source directory: " + project.getModel().getBuild().getSourceDirectory());
//                                for (Iterator<FileObject> it = deps.iterator(); it.hasNext();) {
//                                    FileObject dep = it.next();
//                                    getLog().debug(dep.getPath());
//                                    classpath.append(";");
//                                    classpath.append(dep.getPath());
//                                }
                //                commands.add("\"" + classpath.toString() + "\"");
                //                commands.add("randoop.main.Main");
                //                commands.add("gentests");
                //                for (Iterator<String> it = elements.iterator(); it.hasNext();) {
                //                    String te = it.next();
                //                    commands.add("--testclass=" + te);
                //                }
                //                if (NbPreferences.forModule(RandoopTestGenerator.class).keys().length == 0) {
                //                    //Use defaults
                //                    for (Iterator<Map.Entry<String, Object>> it = getDefaults().entrySet().iterator(); it.hasNext();) {
                //                        Map.Entry<String, Object> entry = it.next();
                //                        commands.add("--" + entry.getKey() + "=" + entry.getValue());
                //                        if (entry.getKey().equals("mem-megabytes")) {
                //                            commands.add(1, "-Xmx" + entry.getValue() + "m");
                //                        }
                //                    }
                //                } else {
                //                    //Add commands from project's settings
                //                    try {
                //                        for (String s : NbPreferences.forModule(RandoopTestGenerator.class).keys()) {
                //                            commands.add("--" + s + "=" + NbPreferences.forModule(RandoopSettingsPanel.class).get(s, null));
                //                            if (s.equals("mem-megabytes")) {
                //                                commands.add(1, "-Xmx" + NbPreferences.forModule(RandoopSettingsPanel.class).get(s, null) + "m");
                //                            }
                //                        }
                //                    } catch (BackingStoreException ex) {
                //                        Exceptions.printStackTrace(ex);
                //                    }
                //                }
                //                getLog().info("Running command: " + commands.toString().replaceAll(",", ""));
                //                try {
                //                    ProcessBuilder pb = new ProcessBuilder(commands);
                //                    pb.directory(new File(project.getProjectDirectory().getPath()));
                //                    getLog().info("Executing command on path: " + pb.directory().getPath());
                //                    pb.redirectErrorStream(true);
                //                    String line;
                //                    process = pb.start();
                //                    InputStream stderr = process.getErrorStream();
                //                    InputStream stdout = process.getInputStream();
                //                    // clean up any output in stdout
                //                    BufferedReader brCleanUp = new BufferedReader(
                //                            new InputStreamReader(stdout));
                //                    String mess = "";
                //                    while ((line = brCleanUp.readLine()) != null) {
                //                        mess += "\n" + line;
                //                    }
                //                    if (!mess.isEmpty() && !mess.startsWith("Error")) {
                //                        output(RandoopTestGeneratorAction.RANDOOP,
                //                                "[Randoop] " + mess, Color.BLACK, null);
                //                        getLog().info("[Randoop] " + mess);
                //                    }
                //                    brCleanUp.close();
                //                    mess = "";
                //                    // clean up any output in stderr
                //                    brCleanUp = new BufferedReader(new InputStreamReader(stderr));
                //                    while ((line = brCleanUp.readLine()) != null) {
                //                        mess += "\n" + line;
                //                    }
                //                    if (!mess.isEmpty() || mess.startsWith("Error")) {
                //                        output(RandoopTestGeneratorAction.RANDOOP,
                //                                "[Randoop Error] " + mess, Color.RED, null);
                //                        getLog().error("[Randoop Error] " + mess);
                //                    }
                //                } catch (IOException e) {
                //                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                //                            "There was a problem executing the Randoop command. "
                //                            + "Make sure Java (JDK or JRE) is on the classpath",
                //                            NotifyDescriptor.ERROR_MESSAGE));
                //                    getLog().error(e);
                //                }
            }
        };
        Thread thread = new Thread(randoopProcess, "Randoop");
        thread.start();
    }
}
