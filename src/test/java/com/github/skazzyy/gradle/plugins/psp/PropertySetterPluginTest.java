package com.github.skazzyy.gradle.plugins.psp;

import java.io.File;
import java.util.Arrays;

import org.gradle.api.Task;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.api.tasks.Exec;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
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
        project.getExtensions().add("psp.basic.property", VALUE);
        project.getExtensions().add("psp.some.other.property", VALUE);
        project.getExtensions().create("basic", Extension.class);
        project.getPlugins().apply(PropertySetterPlugin.class);
        Assert.assertEquals(VALUE, project.getExtensions().getByType(Extension.class).property);
    }

    @Test
    public void shouldRunAgainWhenEvaluated() {
        final String VALUE = "value";
        // given the property is passed and the plugin is applied
        project.getExtensions().add("psp.basic.property", VALUE);
        project.getPlugins().apply(PropertySetterPlugin.class);
        // when the extension is created after applying the plugin
        project.getExtensions().create("basic", Extension.class);
        project.evaluate();
        // then when the project is evaluated, the property will get set
        Assert.assertEquals(VALUE, project.getExtensions().getByType(Extension.class).property);
    }


    @Test
    public void shouldSetComplexProperty() {
        Task task = project.getTasks().create("customTask");
        task.getExtensions().create("basic", Extension.class);
        final String VALUE = "someValue";
        project.getExtensions().add("psp.customTask.basic.property", VALUE);
        project.getPlugins().apply(PropertySetterPlugin.class);
        Assert.assertEquals(VALUE, task.getExtensions().getByType(Extension.class).property);
    }

    @Ignore
    @Test
    public void shouldSetComplexExtensionProperty() {
        final String VALUE = "value";
        project.getExtensions().add("psp.parent.childExtension.property", VALUE);
        project.getExtensions().create("parent", NestedExtension.class);
        project.getPlugins().apply(PropertySetterPlugin.class);
        Assert.assertEquals(VALUE, project.getExtensions().getByType(NestedExtension.class).childExtension.property);
    }

    @Test
    public void shouldSetTaskProperty() {
        Exec echoTask = project.getTasks().create("echoTask", Exec.class);
        final String VALUE = "echo hello";
        project.getExtensions().add("psp.echoTask.commandLine", VALUE);
        project.getPlugins().apply(PropertySetterPlugin.class);
        Assert.assertEquals(Arrays.asList(VALUE), echoTask.getCommandLine());
    }

}

