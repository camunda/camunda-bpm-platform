package org.camunda.bpm.spring.boot.starter;

import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.spring.boot.starter.actuator.JobExecutorHealthIndicator;
import org.camunda.bpm.spring.boot.starter.actuator.ProcessEngineHealthIndicator;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@ConditionalOnProperty(prefix = "management.health.camunda", name = "enabled", matchIfMissing = true)
@ConditionalOnClass(HealthIndicator.class)
@DependsOn("runtimeService")
public class CamundaBpmActuatorConfiguration {

  @Bean
  @ConditionalOnBean(name = "jobExecutor")
  @ConditionalOnMissingBean(name = "jobExecutorHealthIndicator")
  public HealthIndicator jobExecutorHealthIndicator(JobExecutor jobExecutor) {
    return new JobExecutorHealthIndicator(jobExecutor);
  }

  @Bean
  @ConditionalOnMissingBean(name = "processEngineHealthIndicator")
  public HealthIndicator processEngineHealthIndicator(ProcessEngine processEngine) {
    return new ProcessEngineHealthIndicator(processEngine);
  }
}
