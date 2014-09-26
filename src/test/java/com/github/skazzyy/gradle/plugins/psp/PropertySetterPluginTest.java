package com.github.skazzyy.gradle.plugins.psp;

import java.io.File;
import java.util.Arrays;

import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.tasks.Exec;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PropertySetterPluginTest {

    DefaultProject project;

    @Before
    public void setup() {
        project = (DefaultProject) ProjectBuilder.builder().withProjectDir(new File("build/tmp/" + getClass().getSimpleName())).build();
    }

    @Test
    public void shouldApplyPlugin() {
        project.getPlugins().apply("property-setter");
    }

    @Test
    public void shouldSetBasicExtensionProperty() {

        final String VALUE = "value";

//        project.getExtensions().
        project.getExtensions().add("psp.basic.property", VALUE);
        project.getExtensions().create("basic", Basic.class);
//        (project.getProperties()).put("psp.basic.property", "value");
        // project.setProperty("psp.basic.property", "value");
        project.getPlugins().apply(PropertySetterPlugin.class);
        Assert.assertEquals(VALUE, project.getExtensions().getByType(Basic.class).property);
    }


    public static class Basic {
        public String property;
    }


    @Test
    public void shouldSetTaskProperty() {

        Exec echoTask = project.getTasks().create("echoTask", Exec.class);

        final String VALUE = "echo hello";

        // project.getExtensions().
        project.getExtensions().add("psp.echoTask.commandLine", VALUE);
        // (project.getProperties()).put("psp.basic.property", "value");
        // project.setProperty("psp.basic.property", "value");
        project.getPlugins().apply(PropertySetterPlugin.class);
        Assert.assertEquals(Arrays.asList(VALUE), echoTask.getCommandLine());
    }

}

