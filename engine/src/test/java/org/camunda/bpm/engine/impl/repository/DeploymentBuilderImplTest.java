package org.camunda.bpm.engine.impl.repository;

import org.assertj.core.api.Assertions;
import org.junit.Test;

public class DeploymentBuilderImplTest {


    @Test
    public void shouldLoadWildcardResourcesInJar() {
        DeploymentBuilderImpl deploymentBuilder = new DeploymentBuilderImpl(null);
        deploymentBuilder.addClasspathResource("META-INF/spring*");
        Assertions.assertThat(deploymentBuilder.getResourceNames()).containsExactly("/META-INF/spring.tooling", "/META-INF/spring.schemas", "/META-INF/spring.handlers");
    }

    @Test
    public void shouldLoadWildcardResourcesInTargetDir() {
        DeploymentBuilderImpl deploymentBuilder = new DeploymentBuilderImpl(null);
        deploymentBuilder.addClasspathResource("test/*");
        Assertions.assertThat(deploymentBuilder.getResourceNames()).containsExactly("test/test1.txt", "test/test2.txt");
    }
}
