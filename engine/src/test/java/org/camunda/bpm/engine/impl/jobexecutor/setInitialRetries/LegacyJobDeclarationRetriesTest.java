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
package org.camunda.bpm.engine.impl.jobexecutor.setInitialRetries;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.impl.cfg.StandaloneInMemProcessEngineConfiguration;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.jobexecutor.FailingDelegate;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

public class LegacyJobDeclarationRetriesTest {

  @Rule
  public ProcessEngineRule processEngineRule = new ProcessEngineRule(
      new StandaloneInMemProcessEngineConfiguration()
          .setLegacyJobRetryBehaviorEnabled(true)
          .buildProcessEngine()
  );

  @After
  public void tearDown() {
    processEngineRule.getProcessEngine().close();
  }

  @Test
  public void testRetryTimeCycle() {
    // given
    ProcessEngine processEngine = processEngineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R5/PT5M";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(retryInterval);
    deployProcess(processEngine, bpmnModelInstance);

    // when
    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(3, job.getRetries());
  }

  @Test
  public void testRetryTimeCycleWithZeroRetries() {
    // given
    ProcessEngine processEngine = processEngineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R0/PT5M";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(retryInterval);
    deployProcess(processEngine, bpmnModelInstance);

    // when
    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(3, job.getRetries());
  }

  @Test
  public void testRetryTimeCycleWithZeroRetriesAndFailure() {
    // given
    ProcessEngine processEngine = processEngineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R0/PT5M";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(retryInterval);
    deployProcess(processEngine, bpmnModelInstance);

    // when
    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(3, job.getRetries());

    // when
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }
    job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(0, job.getRetries());
  }

  @Test
  public void testRetryTimeCycleWithFailure() {
    // given
    ProcessEngine processEngine = processEngineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R5/PT5M";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(retryInterval);
    deployProcess(processEngine, bpmnModelInstance);

    // when
    runtimeService.startProcessInstanceByKey("process");
    // then
    Job job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(3, job.getRetries());

    // when
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }
    job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(4, job.getRetries());
  }

  @Test
  public void testRetryIntervals() {
    // given
    ProcessEngine processEngine = processEngineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "PT10M,PT17M,PT20M";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(retryInterval);
    deployProcess(processEngine, bpmnModelInstance);

    // when
    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // when
    Assert.assertEquals(3, job.getRetries());
  }

  @Test
  public void testRetryIntervalsWithFailure() {
    // given
    ProcessEngine processEngine = processEngineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "PT10M,PT17M,PT20M";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(retryInterval);
    deployProcess(processEngine, bpmnModelInstance);

    // when
    runtimeService.startProcessInstanceByKey("process");
    Job job = managementService.createJobQuery().singleResult();

    // then
    Assert.assertEquals(3, job.getRetries());

    // when
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }

    // then
    job = managementService.createJobQuery().singleResult();
    Assert.assertEquals(3, job.getRetries());
  }

  private static BpmnModelInstance getBpmnModelInstance(String retryStrategy) {
    return Bpmn.createExecutableProcess("process")
        .camundaHistoryTimeToLive(180)
        .startEvent()
        .serviceTask()
        .camundaAsyncBefore()
        .camundaFailedJobRetryTimeCycle(retryStrategy)
        .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }

  private void deployProcess(ProcessEngine processEngine ,BpmnModelInstance bpmnModelInstance) {
    processEngine.getRepositoryService()
        .createDeployment()
        .addModelInstance("process.bpmn", bpmnModelInstance)
        .deploy();
  }
}
