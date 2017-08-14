package org.camunda.bpm.spring.boot.starter.configuration.impl;

import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaMetricsConfiguration;
import org.camunda.bpm.spring.boot.starter.property.MetricsProperty;

import javax.annotation.PostConstruct;

public class DefaultMetricsConfiguration extends AbstractCamundaConfiguration implements CamundaMetricsConfiguration {

  private MetricsProperty metrics;

  @PostConstruct
  void init() {
    metrics = camundaBpmProperties.getMetrics();
  }

  @Override
  public void preInit(final SpringProcessEngineConfiguration configuration) {
    configuration.setMetricsEnabled(metrics.isEnabled());
    configuration.setDbMetricsReporterActivate(metrics.isDbReporterActivate());
  }
}
