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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import io.quarkus.test.QuarkusUnitTest;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.cdi.CdiStandaloneProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.jobexecutor.JobExecutor;
import org.camunda.bpm.engine.impl.test.TestHelper;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.quarkus.engine.extension.impl.ManagedJobExecutor;
import org.eclipse.microprofile.context.ManagedExecutor;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

public class ManagedJobExecutorTest {

  @RegisterExtension
  static final QuarkusUnitTest unitTest = new QuarkusUnitTest()
      .setArchiveProducer(() -> ShrinkWrap.create(JavaArchive.class)
          .addClass(ExecutorConfig.class)
          .addAsResource("org/camunda/bpm/quarkus/engine/test/asyncServiceTask.bpmn20.xml"));

  @ApplicationScoped
  static class ExecutorConfig {

    @Produces
    public CdiStandaloneProcessEngineConfiguration getCustomProcessEngineConfig() {

      CdiStandaloneProcessEngineConfiguration executorConfiguration = new CdiStandaloneProcessEngineConfiguration();
      executorConfiguration.setJobExecutorActivate(true);

      return executorConfiguration;
    }

  }

  @Inject
  ManagedExecutor managedExecutor;

  @Inject
  protected ProcessEngine processEngine;

  @Inject
  protected RuntimeService runtimeService;

  @Inject
  protected ManagementService managementService;

  @Inject
  protected RepositoryService repositoryService;

  protected ProcessEngineConfigurationImpl processEngineConfiguration;

  @BeforeEach
  protected void setUp() {
    repositoryService.createDeployment()
        .addClasspathResource("org/camunda/bpm/quarkus/engine/test/asyncServiceTask.bpmn20.xml")
        .deploy();
    processEngineConfiguration = (ProcessEngineConfigurationImpl) processEngine
        .getProcessEngineConfiguration();
  }

  @Test
  public void shouldCreateManagedExecutor() {
    // given a process engine configuration
    ProcessEngineConfigurationImpl configuration =
        (ProcessEngineConfigurationImpl) processEngine.getProcessEngineConfiguration();

    // then
    // an quarkus managed executor is created
    JobExecutor jobExecutor = configuration.getJobExecutor();
    assertThat(jobExecutor).isNotNull();
    assertThat(jobExecutor).isInstanceOf(ManagedJobExecutor.class);
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
  public void shouldExecuteJob() throws InterruptedException {
    // given
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("asyncTaskProcess");

    // when
    long jobCount = managementService
        .createJobQuery()
        .processInstanceId(processInstance.getId())
        .count();

    TestHelper.waitForJobExecutorToProcessAllJobs(processEngineConfiguration, 5000l, 25l);

    // then
    assertThat(jobCount).isOne();
    jobCount = managementService
        .createJobQuery()
        .processInstanceId(processInstance.getId())
        .count();
    assertThat(jobCount).isZero();
  }

}