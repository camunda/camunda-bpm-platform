/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
