package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.Resource;

public class DefaultDeploymentConfigurationTest {

  private final DefaultDeploymentConfiguration defaultDeploymentConfiguration = new DefaultDeploymentConfiguration();
  private final CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
  private final SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();

  @Before
  public void before() {
    defaultDeploymentConfiguration.camundaBpmProperties = camundaBpmProperties;
  }

  @Test
  public void noDeploymentTest() {
    camundaBpmProperties.setAutoDeploymentEnabled(false);
    defaultDeploymentConfiguration.preInit(configuration);

    assertThat(configuration.getDeploymentResources()).isEmpty();
  }

  @Test
  public void deploymentTest() throws IOException {
    camundaBpmProperties.setAutoDeploymentEnabled(true);
    defaultDeploymentConfiguration.preInit(configuration);

    final Resource[] resources = configuration.getDeploymentResources();
    assertThat(resources).hasSize(6);

    assertThat(filenames(resources)).containsOnly("async-service-task.bpmn", "test.cmmn10.xml", "test.bpmn", "test.cmmn", "test.bpmn20.xml", "check-order.dmn");
  }

  private Set<String> filenames(Resource[] resources) {
    Set<String> filenames = new HashSet<String>();
    for (Resource resource : resources) {
      filenames.add(resource.getFilename());
    }
    return filenames;
  }
}
