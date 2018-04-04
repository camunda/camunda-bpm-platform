package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.util.SpringBootStarterException;
import org.junit.Before;
import org.junit.Test;

public class GenericPropertiesConfigurationTest {

  private SpringProcessEngineConfiguration processEngineConfiguration;
  private GenericPropertiesConfiguration genericPropertiesConfiguration;
  private CamundaBpmProperties camundaBpmProperties;

  @Before
  public void init() {
    processEngineConfiguration = new SpringProcessEngineConfiguration();
    genericPropertiesConfiguration = new GenericPropertiesConfiguration();
    camundaBpmProperties = new CamundaBpmProperties();
    genericPropertiesConfiguration.camundaBpmProperties = camundaBpmProperties;
  }

  @Test
  public void genericBindingTestWithType() {
    final int batchPollTimeValue = Integer.MAX_VALUE;
    camundaBpmProperties.getGenericProperties().getProperties().put("batch-poll-time", batchPollTimeValue);
    genericPropertiesConfiguration.preInit(processEngineConfiguration);
    assertEquals(batchPollTimeValue, processEngineConfiguration.getBatchPollTime());
  }

  @Test
  public void genericBindingTestAsString() {
    final int batchPollTimeValue = Integer.MAX_VALUE;
    camundaBpmProperties.getGenericProperties().getProperties().put("batch-poll-time", Integer.valueOf(batchPollTimeValue).toString());
    genericPropertiesConfiguration.preInit(processEngineConfiguration);
    assertEquals(batchPollTimeValue, processEngineConfiguration.getBatchPollTime());
  }

  @Test(expected = SpringBootStarterException.class)
  public void genericBindingTestWithNotExistingProperty() {
    final int dontExistValue = Integer.MAX_VALUE;
    camundaBpmProperties.getGenericProperties().getProperties().put("dont-exist", dontExistValue);
    genericPropertiesConfiguration.preInit(processEngineConfiguration);
  }
}
