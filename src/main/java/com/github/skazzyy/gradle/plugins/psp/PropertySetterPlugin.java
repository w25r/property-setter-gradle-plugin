package com.github.skazzyy.gradle.plugins.psp;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.plugins.ExtensionAware;

public class PropertySetterPlugin implements Plugin<Project> {

    public final static String PROPERTY_PREFIX = "psp";
    public final static String EXTENSION_NAME = "propertySetter";
    
    protected final Map<String, String> propertiesToSet = new HashMap<String, String>();

    protected Project targetProject;
    protected Logger logger;

    public void apply(Project target) {

        this.targetProject = target;
        this.logger = target.getLogger();
        target.getExtensions().create(EXTENSION_NAME, PropertySetterPluginExtension.class, this);

        Map<String, ?> properties = targetProject.getProperties();
        for (String key : properties.keySet()) {
            if (key.startsWith(PROPERTY_PREFIX)) {
                String value = properties.get(key).toString();
                logger.debug("Adding key-value pair: key={} value={}", key, value);
                String scrubbedKey = key.substring(PROPERTY_PREFIX.length() + 1);
                propertiesToSet.put(scrubbedKey, value);
            }
        }

        //set now for any extensions that already exist
        setProperties();
        
        //also set after project evaluation
        target.afterEvaluate(new Action<Project>() {
            public void execute(Project project) {
                // TODO: add an extension property that disables this feature?
                setProperties();
            }
        });
    }

    protected void setProperties() {
        Set<Entry<String, String>> entrySet = propertiesToSet.entrySet();
        logger.debug("Attempting to set the following properties {}", entrySet);

        for (Iterator<Entry<String, String>> iterator = entrySet.iterator(); iterator.hasNext();) {
            Entry<String, String> entry = iterator.next();
            if (setProperty(targetProject, entry.getKey().replace(PROPERTY_PREFIX + ".", ""), entry.getValue())) {
                iterator.remove();
            }
        }
    }

    protected boolean setProperty(ExtensionAware parent, String key, String value) {

        String[] expandedProperties = key.split("\\.", 2);
        String propertyName = expandedProperties[0];

        Object property = null;
        try {
            property = InvokerHelper.getProperty(parent, propertyName);
        }
        catch (MissingPropertyException e) {
            logger.debug("The property {} was not found on {}", propertyName, parent);
            return false;
        }

        logger.debug("Found property {} with current value {}", propertyName, property);
        if (property instanceof ExtensionAware) {
            // This is a nested property container
            return setProperty((ExtensionAware) property, expandedProperties[1], value);
        }

        // let's set the property!
        logger.info("Setting property {} to {}", propertyName, value);
        try {
            InvokerHelper.invokeMethod(parent, propertyName, value);
            return true;
        }
        catch (MissingMethodException e) {
            // worth a try, right?
        }
        InvokerHelper.setProperty(parent, propertyName, value);

        return true;

    }


}
