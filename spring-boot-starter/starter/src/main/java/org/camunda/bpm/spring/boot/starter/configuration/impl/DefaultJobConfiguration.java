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
package org.camunda.bpm.spring.boot.starter.configuration.impl;

import static org.camunda.bpm.spring.boot.starter.util.CamundaSpringBootUtil.join;

import java.util.List;
import java.util.Optional;

import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.jobexecutor.JobHandler;
import org.camunda.bpm.engine.impl.jobexecutor.NotifyAcquisitionRejectedJobsHandler;
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration;
import org.camunda.bpm.engine.spring.components.jobexecutor.SpringJobExecutor;
import org.camunda.bpm.spring.boot.starter.configuration.CamundaJobConfiguration;
import org.camunda.bpm.spring.boot.starter.event.JobExecutorStartingEventListener;
import org.camunda.bpm.spring.boot.starter.property.CamundaBpmProperties;
import org.camunda.bpm.spring.boot.starter.property.JobExecutionProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Prepares JobExecutor and registers all known custom JobHandlers.
 */
public class DefaultJobConfiguration extends AbstractCamundaConfiguration implements CamundaJobConfiguration {

  @Autowired
  protected JobExecutor jobExecutor;

  @Autowired(required = false)
  protected List<JobHandler<?>> customJobHandlers;

  @Override
  public void preInit(final SpringProcessEngineConfiguration configuration) {
    configureJobExecutor(configuration);
    registerCustomJobHandlers(configuration);
  }

  protected void registerCustomJobHandlers(SpringProcessEngineConfiguration configuration) {
    configuration.setCustomJobHandlers(join(configuration.getCustomJobHandlers(), customJobHandlers));
    for (JobHandler<?> jobHandler : configuration.getCustomJobHandlers()) {
      logger.info("Register Custom JobHandler: '{}'", jobHandler.getType());
    }
  }

  protected void configureJobExecutor(SpringProcessEngineConfiguration configuration) {
    // note: the job executor will be activated in
    // org.camunda.bpm.spring.boot.starter.runlistener.JobExecutorRunListener
    configuration.setJobExecutorActivate(false);
    configuration.setJobExecutorDeploymentAware(camundaBpmProperties.getJobExecution().isDeploymentAware());
    configuration.setJobExecutor(jobExecutor);

  }

  public static class JobConfiguration {

    public static final String CAMUNDA_TASK_EXECUTOR_QUALIFIER = "camundaTaskExecutor";

    @Bean(name = CAMUNDA_TASK_EXECUTOR_QUALIFIER)
    @ConditionalOnMissingBean(name = CAMUNDA_TASK_EXECUTOR_QUALIFIER)
    @ConditionalOnProperty(prefix = "camunda.bpm.job-execution", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static TaskExecutor camundaTaskExecutor(CamundaBpmProperties properties) {
      int corePoolSize = properties.getJobExecution().getCorePoolSize();
      int maxPoolSize = properties.getJobExecution().getMaxPoolSize();
      int queueCapacity = properties.getJobExecution().getQueueCapacity();

      final ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

      threadPoolTaskExecutor.setCorePoolSize(corePoolSize);
      threadPoolTaskExecutor.setMaxPoolSize(maxPoolSize);
      threadPoolTaskExecutor.setQueueCapacity(queueCapacity);

      Optional.ofNullable(properties.getJobExecution().getKeepAliveSeconds())
        .ifPresent(threadPoolTaskExecutor::setKeepAliveSeconds);

      LOG.configureJobExecutorPool(corePoolSize, maxPoolSize);
      return threadPoolTaskExecutor;
    }

    @Bean
    @ConditionalOnMissingBean(JobExecutor.class)
    @ConditionalOnProperty(prefix = "camunda.bpm.job-execution", name = "enabled", havingValue = "true", matchIfMissing = true)
    public static JobExecutor jobExecutor(@Qualifier(CAMUNDA_TASK_EXECUTOR_QUALIFIER) final TaskExecutor taskExecutor, CamundaBpmProperties properties) {
      final SpringJobExecutor springJobExecutor = new SpringJobExecutor();
      springJobExecutor.setTaskExecutor(taskExecutor);
      springJobExecutor.setRejectedJobsHandler(new NotifyAcquisitionRejectedJobsHandler());

      JobExecutionProperty jobExecution = properties.getJobExecution();
      Optional.ofNullable(jobExecution.getLockTimeInMillis()).ifPresent(springJobExecutor::setLockTimeInMillis);
      Optional.ofNullable(jobExecution.getMaxJobsPerAcquisition()).ifPresent(springJobExecutor::setMaxJobsPerAcquisition);
      Optional.ofNullable(jobExecution.getWaitTimeInMillis()).ifPresent(springJobExecutor::setWaitTimeInMillis);
      Optional.ofNullable(jobExecution.getMaxWait()).ifPresent(springJobExecutor::setMaxWait);
      Optional.ofNullable(jobExecution.getBackoffTimeInMillis()).ifPresent(springJobExecutor::setBackoffTimeInMillis);
      Optional.ofNullable(jobExecution.getMaxBackoff()).ifPresent(springJobExecutor::setMaxBackoff);
      Optional.ofNullable(jobExecution.getBackoffDecreaseThreshold()).ifPresent(springJobExecutor::setBackoffDecreaseThreshold);
      Optional.ofNullable(jobExecution.getWaitIncreaseFactor()).ifPresent(springJobExecutor::setWaitIncreaseFactor);

      return springJobExecutor;
    }

    @Bean
    @ConditionalOnProperty(prefix = "camunda.bpm.job-execution", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnBean(JobExecutor.class)
    public static JobExecutorStartingEventListener jobExecutorStartingEventListener() {
      return new JobExecutorStartingEventListener();
    }
  }
}
