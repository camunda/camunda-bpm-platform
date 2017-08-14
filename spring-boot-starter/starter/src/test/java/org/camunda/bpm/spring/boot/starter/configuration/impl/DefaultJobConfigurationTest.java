package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import java.util.Arrays;

import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class DefaultJobConfigurationTest {

  private final SpringProcessEngineConfiguration processEngineConfiguration = new SpringProcessEngineConfiguration();
  private final DefaultJobConfiguration jobConfiguration = new DefaultJobConfiguration();
  private final CamundaBpmProperties properties = new CamundaBpmProperties();

  @Before
  public void setUp() {
    setField(jobConfiguration, "camundaBpmProperties", properties);
  }

  @Test
  public void delegate_to_specialized_configurations() {
    DefaultJobConfiguration configurationSpy = Mockito.spy(jobConfiguration);
    configurationSpy.preInit(processEngineConfiguration);
    verify(configurationSpy).configureJobExecutor(processEngineConfiguration);
    verify(configurationSpy).registerCustomJobHandlers(processEngineConfiguration);
  }

  @Test
  public void addJobHandler() {
    JobHandler<?> jobHandler = mock(JobHandler.class);
    when(jobHandler.getType()).thenReturn("MockHandler");
    setField(jobConfiguration, "customJobHandlers", Arrays.<JobHandler<?>> asList(jobHandler));

    assertThat(processEngineConfiguration.getCustomJobHandlers()).isNull();
    jobConfiguration.registerCustomJobHandlers(processEngineConfiguration);

    assertThat(processEngineConfiguration.getCustomJobHandlers()).containsOnly(jobHandler);
  }

}
