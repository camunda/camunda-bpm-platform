package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.invokeMethod;
import static org.springframework.test.util.ReflectionTestUtils.setField;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.junit.Before;
import org.junit.Test;

public class DefaultMetricsConfigurationTest {
  private DefaultMetricsConfiguration defaultMetricsConfiguration = new DefaultMetricsConfiguration();
  private CamundaBpmProperties camundaBpmProperties = new CamundaBpmProperties();
  private SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();

  @Before
  public void setUp() {
    setField(defaultMetricsConfiguration, "camundaBpmProperties", camundaBpmProperties);
    defaultMetricsConfiguration.init();

    invokeMethod(configuration, "initMetrics");
  }

  @Test
  public void enabled() {
    assertThat(configuration.isMetricsEnabled()).isTrue();
    assertThat(camundaBpmProperties.getMetrics().isEnabled()).isTrue();

    camundaBpmProperties.getMetrics().setEnabled(false);
    defaultMetricsConfiguration.preInit(configuration);
    assertThat(configuration.isMetricsEnabled()).isFalse();

    camundaBpmProperties.getMetrics().setEnabled(true);
    defaultMetricsConfiguration.preInit(configuration);
    assertThat(configuration.isMetricsEnabled()).isTrue();
  }

  @Test
  public void dbMetricsReporterActivate() {
    assertThat(configuration.isDbMetricsReporterActivate()).isTrue();
    assertThat(camundaBpmProperties.getMetrics().isDbReporterActivate()).isTrue();

    camundaBpmProperties.getMetrics().setDbReporterActivate(false);
    defaultMetricsConfiguration.preInit(configuration);
    assertThat(configuration.isDbMetricsReporterActivate()).isFalse();

    camundaBpmProperties.getMetrics().setDbReporterActivate(true);
    defaultMetricsConfiguration.preInit(configuration);
    assertThat(configuration.isDbMetricsReporterActivate()).isTrue();
  }
}
