package net.sourceforge.javydreamercsw.randoop.mojo;

import com.thoughtworks.qdox.JavaDocBuilder;
import com.thoughtworks.qdox.model.JavaClass;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.WordUtils;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
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
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.plugin.version.PluginVersionNotFoundException;
import org.apache.maven.plugin.version.PluginVersionResolutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.tools.plugin.util.PluginUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 *
 * @author Javier A. Ortiz Bultron <javier.ortiz.78@gmail.com>
 */
public abstract class AbstractRandoopMojo extends AbstractMojo {

    /**
     * The Plugin manager instance used to resolve Plugin descriptors.
     *
     */
    @Component
    protected PluginManager pluginManager;
    /**
     * The project whose project files to create.
     */
    // Must configure pom.xml as per
    // http://maven.apache.org/plugin-tools/maven-plugin-plugin/examples/using-annotations.html
    @Component
    protected MavenProject project;
    /**
     * The current build session instance. This is used for plugin manager API
     * calls.
     */
    @Parameter(required = true, readonly = true, property = "session")
    protected MavenSession session;
    /**
     * Use smart generation of tests.
     * 
     * If configured, the following behavior will be executed:
     * 1) Tests will be placed on src/test/java/randoop/<ClassName> folder
     * 2) Randoop test cases file name will be <ClassName> instead of the default.
     *
     */
    @Parameter(required = true, readonly = true, defaultValue = "false")
    private boolean smartGeneration;
    protected String version = "1.0-SNAPSHOT";
    protected String groupId = "net.sourceforge.javydreamercsw";
    protected String artifactId = "randoop-maven-plugin";

    /**
     * Makes test generation smarter. 1)
     *
     * @return the smartGeneration
     */
    public boolean isSmartGeneration() {
        return smartGeneration;
    }

    protected Method getGetterMethod(String variable)
            throws InstantiationException, IllegalAccessException {
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
     * Gets parameters for the plugin
     *
     * @param md contains the description of the Plugin Mojo
     * @return Mojo parameters and their value
     * @throws MojoFailureException if any reflection exceptions occur.
     * @throws MojoExecutionException if any
     */
    protected Map<String, Object> getMojoParameters(MojoDescriptor md)
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
                    org.apache.maven.plugin.descriptor.Parameter parameter1 =
                            (org.apache.maven.plugin.descriptor.Parameter) o1;
                    org.apache.maven.plugin.descriptor.Parameter parameter2 =
                            (org.apache.maven.plugin.descriptor.Parameter) o2;
                    return parameter1.getName().compareToIgnoreCase(parameter2.getName());
                }
            });
            for (Iterator it = params.iterator(); it.hasNext();) {
                try {
                    org.apache.maven.plugin.descriptor.Parameter parameter =
                            (org.apache.maven.plugin.descriptor.Parameter) it.next();
                    if (!parameter.isEditable()) {
                        continue;
                    }
                    getLog().debug("Getting value for: " + parameter.getName());
                    Method method =
                            getGetterMethod(WordUtils.capitalize(
                            parameter.getName().replaceAll("_", " "))
                            .replaceAll(" ", ""));
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

    protected List<File> getSources(File dir) {
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
     * Method for retrieving the description of the plugin
     *
     * @param pi holds information of the plugin whose description is to be
     * retrieved
     * @return a PluginDescriptor where the plugin description is to be
     * retrieved
     * @throws MojoExecutionException if the plugin could not be verify
     * @throws MojoFailureException if groupId or artifactId is empty
     */
    protected PluginDescriptor lookupPluginDescriptor(PluginInfo pi)
            throws MojoExecutionException, MojoFailureException {
        PluginDescriptor descriptor = null;
        Plugin forLookup = null;
        if (StringUtils.isNotEmpty(pi.getPrefix())) {
            descriptor =
                    pluginManager.getPluginDescriptorForPrefix(pi.getPrefix());
            if (descriptor == null) {
                forLookup =
                        pluginManager.getPluginDefinitionForPrefix(
                        pi.getPrefix(), session, project);
            }
        } else if (StringUtils.isNotEmpty(pi.getGroupId())
                && StringUtils.isNotEmpty(pi.getArtifactId())) {
            forLookup = new Plugin();
            forLookup.setGroupId(pi.getGroupId());
            forLookup.setArtifactId(pi.getArtifactId());
            if (StringUtils.isNotEmpty(pi.getVersion())) {
                forLookup.setVersion(pi.getVersion());
            }
        }
        if (descriptor == null && forLookup != null) {
            try {
                descriptor = pluginManager.loadPluginDescriptor(forLookup,
                        project, session);
            } catch (ArtifactResolutionException e) {
                throw new MojoExecutionException(
                        "Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId
                        + "'\nversion: '" + version + "'\n\n", e);
            } catch (PluginManagerException e) {
                throw new MojoExecutionException(
                        "Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId
                        + "'\nversion: '" + version + "'\n\n", e);
            } catch (PluginVersionResolutionException e) {
                throw new MojoExecutionException(
                        "Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId
                        + "'\nversion: '" + version + "'\n\n", e);
            } catch (ArtifactNotFoundException e) {
                throw new MojoExecutionException(
                        "Plugin dependency does not exist: "
                        + e.getMessage(), e);
            } catch (InvalidVersionSpecificationException e) {
                throw new MojoExecutionException(
                        "Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId
                        + "'\nversion: '" + version + "'\n\n", e);
            } catch (InvalidPluginException e) {
                throw new MojoExecutionException(
                        "Error retrieving plugin descriptor for:\n\ngroupId: '"
                        + groupId + "'\nartifactId: '" + artifactId
                        + "'\nversion: '" + version + "'\n\n", e);
            } catch (PluginNotFoundException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Unable to find plugin", e);
                }
                throw new MojoFailureException("Plugin does not exist: "
                        + e.getMessage());
            } catch (PluginVersionNotFoundException e) {
                if (getLog().isDebugEnabled()) {
                    getLog().debug("Unable to find plugin version", e);
                }
                throw new MojoFailureException(e.getMessage());
            }
        }
        if (descriptor == null) {
            throw new MojoFailureException(
                    "Plugin could not be found. If you believe it is correct,"
                    + " check your pluginGroups setting, and run with "
                    + "-U to update the remote configuration");
        }
        return descriptor;
    }

    public ArrayList<ArrayList<String>> generateCommands() throws MojoExecutionException,
            MojoFailureException, FileNotFoundException {
        ArrayList<String> basicCommand = new ArrayList<String>();
        ArrayList<ArrayList<String>> commands = new ArrayList<ArrayList<String>>();
        basicCommand.add("java");
        basicCommand.add("-ea");
        basicCommand.add("-classpath");
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
        basicCommand.add("\"" + classpath.toString() + "\"");
        basicCommand.add("randoop.main.Main");
        basicCommand.add("gentests");
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
            for (Iterator<Map.Entry<String, Object>> it2 =
                    getMojoParameters(md).entrySet().iterator();
                    it2.hasNext();) {
                Map.Entry<String, Object> entry = it2.next();
                getLog().debug(entry.getKey() + ": "
                        + entry.getValue());
                if (entry.getKey().equals("mem_megabytes")) {
                    basicCommand.add(1, "-Xmx" + entry.getValue()
                            + "m");
                } else if ((entry.getKey().equals("junit_output_dir")
                        || entry.getKey().equals("junit_classname")
                        || entry.getKey().equals("junit_package_name"))
                        && isSmartGeneration()) {
                    //Skip it, is going to be set later
                    getLog().debug("Skipping setting parameter: "
                            + entry.getKey());
                    continue;
                } else {
                    basicCommand.add("--" + entry.getKey() + "="
                            + entry.getValue());
                }
            }
        }
        //At this point the command is the common basic script
        ArrayList<String> sourceList = new ArrayList<String>();
        for (Iterator<File> it = sources.iterator(); it.hasNext();) {
            File source = it.next();
            for (Iterator<File> it2 =
                    getSources(source).iterator(); it2.hasNext();) {
                File te = it2.next();
                //Calculate the full class name
                JavaDocBuilder builder = new JavaDocBuilder();
                builder.addSource(new FileReader(te));
                JavaClass cls = builder.getClasses()[0];
                String pkg = cls.getPackage().getName();
                String name = cls.getName();
                if (isSmartGeneration()) {
                    //Add it to the sourceList for individual processing.
                    sourceList.add((pkg.isEmpty() ? "" : pkg + ".") + name);
                } else {
                    //Normal generation, just add it to the command
                    basicCommand.add("--testclass="
                            + (pkg.isEmpty() ? "" : pkg + ".") + name);
                }
            }
        }
        getLog().debug("Basic command: " + basicCommand.toString());
        if (isSmartGeneration()) {
            for (Iterator<String> it = sourceList.iterator(); it.hasNext();) {
                String c = it.next();
                ArrayList<String> copy = new ArrayList<String>();
                copy.addAll(basicCommand);
                String className = c.substring(c.lastIndexOf('.') + 1);
                String targetPackage = c.substring(0, c.lastIndexOf('.'));
                copy.add("--junit-output-dir=src/test/java/");
                copy.add("--junit-package-name=randoop." + targetPackage + "." + className);
                copy.add("--junit-classname=" + className + "RandoopTest");
                copy.add("--testclass=" + c);
                commands.add(copy);
            }
        } else {
            //Just add the command normally
            commands.add(basicCommand);
        }
        getLog().debug("Amount of commands: " + commands.size());
        return commands;
    }
}
