package org.camunda.bpm.spring.boot.starter.actuator;

import org.camunda.bpm.engine.ProcessEngine;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health.Builder;
import org.springframework.util.Assert;

public class ProcessEngineHealthIndicator extends AbstractHealthIndicator {

  private final ProcessEngine processEngine;

  public ProcessEngineHealthIndicator(ProcessEngine processEngine) {
    Assert.notNull(processEngine);
    this.processEngine = processEngine;
  }

  @Override
  protected void doHealthCheck(Builder builder) throws Exception {
    builder.up().withDetail("name", processEngine.getName());
  }

}
