package com.github.skazzyy.gradle.plugins.psp;

import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.groovy.runtime.InvokerHelper;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;

public class PropertySetterPlugin implements Plugin<Project> {

    public final static String PROPERTY_PREFIX = "psp";
    
    protected final Map<String, String> propertiesToSet = new HashMap<String, String>();

    public void apply(Project targetProject) {
        Map<String, ?> properties = targetProject.getProperties();
        for (String key : properties.keySet()) {
            if (key.startsWith(PROPERTY_PREFIX)) {
                System.out.println("key=" + key);
                String value = properties.get(key).toString();
                System.out.println("value=" + value);
                propertiesToSet.put(key, value);
            }
        }
        setProperties(targetProject);
    }

    protected void setProperties(Project targetProject) {
        Set<Entry<String, String>> entrySet = propertiesToSet.entrySet();
        for (Iterator<Entry<String, String>> iterator = entrySet.iterator(); iterator.hasNext();) {
            Entry<String, String> entry = iterator.next();
            if (setProperty(targetProject, entry.getKey(), entry.getValue())) {
                iterator.remove();
            }
        }
    }

    protected boolean setProperty(ExtensionAware parent, String key, String value) {
        String[] expandedProperties = key.split("\\.");
        for (String propertyName : expandedProperties) {
            // needs to remove the psp.
            // needs to remove the current property and pass only the rest
            // Arrays.copyOfRange(expandedProperties, 1, 1);
            System.out.println(propertyName);
            if (PROPERTY_PREFIX.equals(propertyName)) {
                continue;
            }

            Object property = null;
            try {
                property = InvokerHelper.getProperty(parent, propertyName);
            }
            catch (MissingPropertyException e) {
                continue;
            }

            System.out.println("Property exists at " + propertyName + " with current value " + property);
            if (property instanceof ExtensionAware) {
                // This is a nested property container
                System.out.println("Extension aware!");
                return setProperty((ExtensionAware) property, key, value);
            }

            // let's set the property!
            System.out.println("Setting the property to " + value);
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
        return false;
    }

}
