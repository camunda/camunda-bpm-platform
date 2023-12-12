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
package org.camunda.bpm.quarkus.engine.test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.impl.test.TestHelper.waitForJobExecutorToProcessAllJobs;

import io.quarkus.test.QuarkusUnitTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.quarkus.engine.extension.QuarkusProcessEngineConfiguration;
import org.camunda.bpm.quarkus.engine.extension.impl.ManagedJobExecutor;
import org.camunda.bpm.quarkus.engine.test.helper.ProcessEngineAwareExtension;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ManagedJobExecutorTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new ProcessEngineAwareExtension()
      .withConfigurationResource("org/camunda/bpm/quarkus/engine/test/config/" +
                                     "job-executor-application.properties")
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class));

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  protected ProcessEngine processEngine;

  @Inject
  protected RuntimeService runtimeService;

  @Inject
  protected ManagementService managementService;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @BeforeEach
  protected void setUp() {
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine
        .getProcessEngineConfiguration();
  }

  @ApplicationScoped
  static class EngineConfigurer {

    @Produces
    public QuarkusProcessEngineConfiguration engineConfiguration() {
      return new QuarkusProcessEngineConfiguration();
    }

  }

  @Test
  public void shouldActivateJobExecutorByDefault() {
    // then
    assertThat(processEngineConfiguration.isJobExecutorActivate()).isTrue();
  }

  @Test
  public void shouldCreateManagedExecutor() {
    // given a process engine configuration

    // then
    // an quarkus managed executor is created
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    assertThat(jobExecutor).isNotNull();
    assertThat(jobExecutor).isInstanceOf(ManagedJobExecutor.class);
  }

  @Test
  public void shouldUseCustomJobAcquisitionProperties() {
    // given a custom application.properties file
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();

    // then
    assertThat(jobExecutor.getMaxJobsPerAcquisition()).isEqualTo(5);
    assertThat(jobExecutor.getLockTimeInMillis()).isEqualTo(500000);
    assertThat(jobExecutor.getWaitTimeInMillis()).isEqualTo(7000);
    assertThat(jobExecutor.getMaxWait()).isEqualTo(65000);
    assertThat(jobExecutor.getBackoffTimeInMillis()).isEqualTo(5);
    assertThat(jobExecutor.getMaxBackoff()).isEqualTo(5);
    assertThat(jobExecutor.getBackoffDecreaseThreshold()).isEqualTo(120);
    assertThat(jobExecutor.getWaitIncreaseFactor()).isEqualTo(3);
  }

  @Test
  public void shouldNotReuseManagedExecutor() {
    // given a process engine configuration
    ProcessEngineConfigurationImpl configuration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    // then
    // a quarkus managed executor is created,
    // and a ManagedExecutor bean is not used.
    ManagedJobExecutor jobExecutor = (ManagedJobExecutor) configuration.getJobExecutor();
    assertThat(jobExecutor).hasFieldOrProperty("taskExecutor").isNotSameAs(managedExecutor);
  }

  @Test
  @Deployment
  public void shouldExecuteJob() {
    // given
    processEngineConfiguration.getJobExecutor().shutdown();
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTaskProcess");

    // when
    long jobCount = managementService
        .createJobQuery()
        .processInstanceId(processInstance.getId())
        .count();

    // then
    assertThat(jobCount).isOne();

    waitForJobExecutorToProcessAllJobs(processEngineConfiguration, 5000L, 25L);

    jobCount = managementService
        .createJobQuery()
        .processInstanceId(processInstance.getId())
        .count();

    assertThat(jobCount).isZero();
  }

}