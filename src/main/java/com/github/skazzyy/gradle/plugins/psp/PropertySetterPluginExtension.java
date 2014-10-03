package com.github.skazzyy.gradle.plugins.psp;

public class PropertySetterPluginExtension {

    protected PropertySetterPlugin plugin;

    public PropertySetterPluginExtension(PropertySetterPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Attempts to set any properties that are needed
     */
    public void setProperties() {
        plugin.setProperties();
    }
}
