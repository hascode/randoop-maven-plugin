package net.sourceforge.javydreamercsw.randoop.mojo;

import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.descriptor.PluginDescriptor;

/**
 * Class to wrap Plugin information.
 */
public class PluginInfo {

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
