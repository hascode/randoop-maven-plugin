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
import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.WordUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
import org.apache.maven.artifact.resolver.ArtifactResolutionException;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.InvalidPluginException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugin.PluginManagerException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.descriptor.MojoDescriptor;
import org.apache.maven.plugin.descriptor.Parameter;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.version.PluginVersionNotFoundException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.settings.Settings;
import org.apache.maven.tools.plugin.util.PluginUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Generate tests for a Maven project
 *
 * @goal generate-all-tests
 * @requiresDependencyResolution compile
 * @requiresDirectInvocation true
 * @requiresProject true
 * @aggregator
 * @phase generate-resources
 * @see <a href="http://maven.apache.org/general.html#What_is_a_Mojo">What is a
 * Mojo?</a>
 */
public class RandoopMojo extends AbstractMojo {

    /**
     * The project whose project files to create.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    private MavenProject project;
    /**
     * The Plugin manager instance used to resolve Plugin descriptors.
     *
     * @component role="org.apache.maven.plugin.PluginManager"
     */
    private PluginManager pluginManager;
    /**
     * The current user system settings for use in Maven. This is used for
     * plugin manager API calls.
     *
     * @parameter expression="${settings}"
     * @required
     * @readonly
     */
    private Settings settings;
    /**
     * The current build session instance. This is used for plugin manager API
     * calls.
     *
     * @parameter expression="${session}"
     * @required
     * @readonly
     */
    private MavenSession session;
    /**
     * The local repository ArtifactRepository instance. This is used for plugin
     * manager API calls.
     *
     * @parameter expression="${localRepository}"
     * @required
     * @readonly
     */
    private ArtifactRepository localRepository;
//    @Parameter(required = true, readonly = true, defaultValue = "false")
    /**
     * @parameter expression="${silently_ignore_bad_classNames}"
     * default-value="false"
     * @required
     * @readonly
     */
    private boolean silently_ignore_bad_classNames;
//    @Parameter(required = true, readonly = true, defaultValue = "NONE")
    /**
     * @parameter expression="${literals_level}" default-value="NONE"
     * @required
     *
     */
    private String literals_level;
//    @Parameter(required = true, readonly = true, defaultValue = "0")
    /**
     * @parameter expression="${randomseed}" default-value="0"
     * @required
     *
     */
    private int randomseed;
//    @Parameter(required = true, readonly = true, defaultValue = "100")
    /**
     * @parameter expression="${timelimit}" default-value="100"
     * @required
     *
     */
    private int timelimit;
//    @Parameter(required = true, readonly = true, defaultValue = "100000000")
    /**
     * @parameter expression="${inputlimit}" default-value="100000000"
     * @required
     *
     */
    private int inputlimit;
//    @Parameter(required = true, readonly = true, defaultValue = "100000000")
    /**
     * @parameter expression="${outputlimit}" default-value="100000000"
     * @required
     *
     */
    private int outputlimit;
//    @Parameter(required = true, readonly = true, defaultValue = "100")
    /**
     * @parameter expression="${maxsize}" default-value="100"
     * @required
     *
     */
    private int maxsize;
//    @Parameter(required = true, readonly = true, defaultValue = "true")
    /**
     * @parameter expression="${forbid_null}" default-value="true"
     * @required
     *
     */
    private boolean forbid_null;
//    @Parameter(required = true, readonly = true, defaultValue = "10000")
    /**
     * @parameter expression="${string_maxlen}" default-value="10000"
     * @required
     *
     */
    private int string_maxlen;
//    @Parameter(required = true, readonly = true, defaultValue = "0.0")
    /**
     * @parameter expression="${null_ratio}" default-value="0.0"
     * @required
     *
     */
    private Double null_ratio;
//    @Parameter(required = true, readonly = true, defaultValue = "0.0")
    /**
     * @parameter expression="${alias_ratio}" default-value="0.0"
     * @required
     *
     */
    private Double alias_ratio;
//    @Parameter(required = true, readonly = true, defaultValue = "false")
    /**
     * @parameter expression="${small_tests}" default-value="false"
     * @required
     *
     */
    private boolean small_tests;
//    @Parameter(required = true, readonly = true, defaultValue = "100000000")
    /**
     * @parameter expression="${clear}" default-value="100000000"
     * @required
     *
     */
    private int clear;
//    @Parameter(required = true, readonly = true, defaultValue = "true")
    /**
     * @parameter expression="${check_object_contracts}" default-value="true"
     * @required
     *
     */
    private boolean check_object_contracts;
//    @Parameter(required = true, readonly = true, defaultValue = "all")
    /**
     * @parameter expression="${output_tests}" default-value="all"
     * @required
     *
     */
    private String output_tests;
//    @Parameter(required = true, readonly = true, defaultValue = "500")
    /**
     * @parameter expression="${testsperfile}" default-value="500"
     * @required
     *
     */
    private int testsperfile;
//    @Parameter(required = true, readonly = true, defaultValue = "RandoopTest")
    /**
     * @parameter expression="${junit_classname}" default-value="RandoopTest"
     * @required
     *
     */
    private String junit_classname;
//    @Parameter(required = true, readonly = true, defaultValue = "")
    /**
     * @parameter expression="${junit_package_name}" default-value=""
     * @required
     *
     */
    private String junit_package_name;
//    @Parameter(required = true, readonly = true, defaultValue = "test")
    /**
     * @parameter expression="${junit_output_dir}" default-value=""
     * @required
     *
     */
    private String junit_output_dir;
//    @Parameter(required = true, readonly = true, defaultValue = "false")
    /**
     * @parameter expression="${dont_output_tests}" default-value="false"
     * @required
     *
     */
    private boolean dont_output_tests;
//    @Parameter(required = true, readonly = true, defaultValue = "false")
    /**
     * @parameter expression="${output_nonexec}" default-value="false"
     * @required
     *
     */
    private boolean output_nonexec;
//    @Parameter(required = true, readonly = true, defaultValue = "")
    /**
     * @parameter expression="${agent}" default-value=""
     * @required
     *
     */
    private String agent;
//    @Parameter(required = true, readonly = true, defaultValue = "1000")
    /**
     * @parameter expression="${mem_megabytes}" default-value="1000"
     * @required
     *
     */
    private int mem_megabytes;
//    @Parameter(required = true, readonly = true, defaultValue = "false")
    /**
     * @parameter expression="${capture_output}" default-value="false"
     * @required
     *
     */
    private boolean capture_output;
    private String version = "1.0-SNAPSHOT";
    private String groupId = "net.sourceforge.javydreamercsw";
    private String artifactId = "randoop-maven-plugin";

    @Override
    public void execute() throws MojoExecutionException {
        final Runnable randoopProcess = new Runnable() {
            @Override
            public void run() {
                try {
                    getLog().info("Running Randoop Mojo on project: "
                            + project.getName());
                    ArrayList<String> commands = new ArrayList<String>();
                    commands.add("java");
                    commands.add("-ea");
                    commands.add("-classpath");
                    StringBuilder classpath = new StringBuilder();
                    ArrayList<File> sources = new ArrayList<File>();
                    getLog().debug("Sources:");
                    for (Object obj : project.getCompileSourceRoots()) {
                        getLog().debug(obj.toString());
                        File source = new File(obj.toString());
                        if (source.exists()) {
                            sources.add(source);
                        }
                    }
                    try {
                        for (Object obj : project.getCompileClasspathElements()) {
                            getLog().debug(obj == null ? "null" : obj.toString());
                            if (obj != null) {
                                if (classpath.length() > 0) {
                                    classpath.append(";");
                                }
                                classpath.append(obj.toString());
                            }
                        }
                    } catch (DependencyResolutionRequiredException ex) {
                        getLog().error(ex);
                    }
                    getLog().debug("Classpath: " + classpath.toString());
                    getLog().debug("Test directory: "
                            + project.getModel().getBuild().getTestSourceDirectory());
                    commands.add("\"" + classpath.toString() + "\"");
                    commands.add("randoop.main.Main");
                    commands.add("gentests");
                    for (Iterator<File> it = sources.iterator(); it.hasNext();) {
                        File source = it.next();
                        for (Iterator<File> it2 = getSources(source).iterator(); it2.hasNext();) {
                            File te = it2.next();
                            //Calculate the full class name
                            JavaDocBuilder builder = new JavaDocBuilder();
                            builder.addSource(new FileReader(te));
                            JavaClass cls=builder.getClasses()[0];
                            String pkg = cls.getPackage().getName();   // "com.blah.foo"
                            String name = cls.getName();               // "MyClass"
                            commands.add("--testclass=" + pkg+"."+name);
                        }
                    }
                    //Pass parameters to the command
                    PluginInfo pi = new PluginInfo();
                    pi.setGroupId(groupId);
                    pi.setArtifactId(artifactId);
                    pi.setVersion(version);
                    PluginDescriptor pd = lookupPluginDescriptor(pi);
                    List mojos = pd.getMojos();
                    PluginUtils.sortMojos(mojos);
                    getLog().debug("Amount of mojos: " + mojos.size());
                    for (Iterator it = mojos.iterator(); it.hasNext();) {
                        MojoDescriptor md = (MojoDescriptor) it.next();
                        for (Iterator<Entry<String, Object>> it2 =
                                getMojoParameters(md).entrySet().iterator(); it2.hasNext();) {
                            Entry<String, Object> entry = it2.next();
                            getLog().debug(entry.getKey() + ": " + entry.getValue());
                            if (entry.getKey().equals("mem_megabytes")) {
                                commands.add(1, "-Xmx" + entry.getValue() + "m");
                            } else {
                                commands.add("--" + entry.getKey() + "=" + entry.getValue());
                            }
                        }
                    }
                    getLog().info("Running command: " + commands.toString().replaceAll(",", ""));
                    try {
                        ProcessBuilder pb = new ProcessBuilder(commands);
                        pb.directory(project.getBasedir());
                        getLog().info("Executing command on path: " + pb.directory().getPath());
                        pb.redirectErrorStream(true);
                        String line;
                        Process process = pb.start();
                        InputStream stderr = process.getErrorStream();
                        InputStream stdout = process.getInputStream();
                        // clean up any output in stdout
                        BufferedReader brCleanUp = new BufferedReader(
                                new InputStreamReader(stdout));
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
                        brCleanUp = new BufferedReader(new InputStreamReader(stderr));
                        while ((line = brCleanUp.readLine()) != null) {
                            mess += "\n" + line;
                        }
                        if (!mess.isEmpty() || mess.startsWith("Error")) {
                            getLog().error("[Randoop Error] " + mess);
                        }
                    } catch (IOException e) {
                        getLog().error(e);
                    }
                } catch (FileNotFoundException ex) {
                    getLog().debug(ex);
                } catch (MojoExecutionException ex) {
                    getLog().debug(ex);
                } catch (MojoFailureException ex) {
                    getLog().debug(ex);
                }catch (IOException ex) {
                    getLog().debug(ex);
                } 
            }
        };
        Thread thread = new Thread(randoopProcess, "Randoop");
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            throw new MojoExecutionException("Mojo failed", e);
        }
    }

    private List<File> getSources(File dir) {
        ArrayList<File> sources = new ArrayList<File>();
        if (dir.isDirectory()) {
            for (File file : dir.listFiles()) {
                getLog().debug("Checking file: " + file);
                if (file.isDirectory()) {
                    sources.addAll(getSources(file));
                } else if (file.getName().endsWith(".java")) {
                    getLog().debug("Adding file: " + file.getAbsolutePath());
                    sources.add(file);
                }
            }
        }
        return sources;
    }

    /**
     * Gets parameters for the plugin
     *
     * @param md contains the description of the Plugin Mojo
     * @throws MojoFailureException if any reflection exceptions occur.
     * @throws MojoExecutionException if any
     */
    private Map<String, Object> getMojoParameters(MojoDescriptor md)
            throws MojoFailureException, MojoExecutionException {
        HashMap<String, Object> parameters = new HashMap<String, Object>();
        List params = md.getParameters();
        if (params != null && !params.isEmpty()) {
            getLog().debug("Amount of parameters: " + params.size());
            // TODO remove when maven-plugin-tools-api:2.4.4 is out see PluginUtils.sortMojoParameters()
            Collections.sort(params, new Comparator() {
                /**
                 * {@inheritDoc}
                 */
                @Override
                public int compare(Object o1, Object o2) {
                    Parameter parameter1 = (Parameter) o1;
                    Parameter parameter2 = (Parameter) o2;

                    return parameter1.getName().compareToIgnoreCase(parameter2.getName());
                }
            });
            for (Iterator it = params.iterator(); it.hasNext();) {
                try {
                    Parameter parameter = (Parameter) it.next();
                    if (!parameter.isEditable()) {
                        continue;
                    }
                    getLog().debug("Getting value for: " + parameter.getName());
                    Method method = getGetterMethod(WordUtils.capitalize(parameter.getName().replaceAll("_", " ")).replaceAll(" ", ""));
                    if (method != null) {
                        Object result = method.invoke(this);
                        if (result != null) {
                            parameters.put(parameter.getName(), result);
                        }
                    }
                } catch (InstantiationException ex) {
                    getLog().debug(ex);
                } catch (IllegalAccessException ex) {
                    getLog().debug(ex);
                } catch (IllegalArgumentException ex) {
                    getLog().debug(ex);
                } catch (InvocationTargetException ex) {
                    getLog().debug(ex);
                }
            }
        }
        return parameters;
    }

    private Method getGetterMethod(String variable) throws InstantiationException,
            IllegalAccessException {
        Method m = null;
        try {
            m = getClass().getDeclaredMethod("get" + variable);
        } catch (NoSuchMethodException ex) {
            getLog().debug(ex);
        } catch (SecurityException ex) {
            getLog().debug(ex);
        }
        return m;
    }

    /**
     * Method for retrieving the description of the plugin
     *
     * @param pi holds information of the plugin whose description is to be
     * retrieved
     * @return a PluginDescriptor where the plugin description is to be
     * retrieved
     * @throws MojoExecutionException if the plugin could not be verify
     * @throws MojoFailureException if groupId or artifactId is empty
     */
    private PluginDescriptor lookupPluginDescriptor(PluginInfo pi)
            throws MojoExecutionException, MojoFailureException {
        PluginDescriptor descriptor = null;

        Plugin forLookup = null;

        if (StringUtils.isNotEmpty(pi.getPrefix())) {
            descriptor = pluginManager.getPluginDescriptorForPrefix(pi.getPrefix());
            if (descriptor == null) {
                forLookup = pluginManager.getPluginDefinitionForPrefix(pi.getPrefix(), session, project);
            }
        } else if (StringUtils.isNotEmpty(pi.getGroupId()) && StringUtils.isNotEmpty(pi.getArtifactId())) {
            forLookup = new Plugin();

            forLookup.setGroupId(pi.getGroupId());
            forLookup.setArtifactId(pi.getArtifactId());

            if (StringUtils.isNotEmpty(pi.getVersion())) {
                forLookup.setVersion(pi.getVersion());
            }
        }

        if (descriptor == null && forLookup != null) {
            try {
                descriptor = pluginManager.verifyPlugin(forLookup, project, settings, localRepository);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException("Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId + "'\nversion: '" + version + "'\n\n", e);
            } catch (PluginManagerException e) {
                throw new MojoExecutionException("Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId + "'\nversion: '" + version + "'\n\n", e);
            } catch (PluginVersionResolutionException e) {
                throw new MojoExecutionException("Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId + "'\nversion: '" + version + "'\n\n", e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException("Plugin dependency does not exist: " + e.getMessage(), e);
            } catch (InvalidVersionSpecificationException e) {
                throw new MojoExecutionException("Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId + "'\nversion: '" + version + "'\n\n", e);
            } catch (InvalidPluginException e) {
                throw new MojoExecutionException("Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId + "'\nversion: '" + version + "'\n\n", e);
            } catch (PluginNotFoundException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Unable to find plugin", e);
                }
                throw new MojoFailureException("Plugin does not exist: " + e.getMessage());
            } catch (PluginVersionNotFoundException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Unable to find plugin version", e);
                }
                throw new MojoFailureException(e.getMessage());
            }
        }

        if (descriptor == null) {
            throw new MojoFailureException("Plugin could not be found. If you believe it is correct,"
                    + " check your pluginGroups setting, and run with -U to update the remote configuration");
        }

        return descriptor;
    }

    /**
     * @return the silentlyIgnoreBadClassNames
     */
    public boolean getSilentlyIgnoreBadClassNames() {
        return silently_ignore_bad_classNames;
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

    /**
     * Class to wrap Plugin information.
     */
    static class PluginInfo {

        private String prefix;
        private String groupId;
        private String artifactId;
        private String version;
        private String mojo;
        private Plugin plugin;
        private PluginDescriptor pluginDescriptor;

        /**
         * @return the prefix
         */
        public String getPrefix() {
            return prefix;
        }

        /**
         * @param prefix the prefix to set
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        /**
         * @return the groupId
         */
        public String getGroupId() {
            return groupId;
        }

        /**
         * @param groupId the groupId to set
         */
        public void setGroupId(String groupId) {
            this.groupId = groupId;
        }

        /**
         * @return the artifactId
         */
        public String getArtifactId() {
            return artifactId;
        }

        /**
         * @param artifactId the artifactId to set
         */
        public void setArtifactId(String artifactId) {
            this.artifactId = artifactId;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @param version the version to set
         */
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * @return the mojo
         */
        public String getMojo() {
            return mojo;
        }

        /**
         * @param mojo the mojo to set
         */
        public void setMojo(String mojo) {
            this.mojo = mojo;
        }

        /**
         * @return the plugin
         */
        public Plugin getPlugin() {
            return plugin;
        }

        /**
         * @param plugin the plugin to set
         */
        public void setPlugin(Plugin plugin) {
            this.plugin = plugin;
        }

        /**
         * @return the pluginDescriptor
         */
        public PluginDescriptor getPluginDescriptor() {
            return pluginDescriptor;
        }

        /**
         * @param pluginDescriptor the pluginDescriptor to set
         */
        public void setPluginDescriptor(PluginDescriptor pluginDescriptor) {
            this.pluginDescriptor = pluginDescriptor;
        }
    }
}