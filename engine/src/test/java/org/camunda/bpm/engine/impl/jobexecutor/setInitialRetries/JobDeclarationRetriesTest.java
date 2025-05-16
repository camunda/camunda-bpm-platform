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

import java.util.List;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.jobexecutor.FailingDelegate;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class JobDeclarationRetriesTest {

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(config -> config.setLegacyJobRetryBehaviorEnabled(false));

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @After
  public void cleanUpDeployment() {
    List<Deployment> deployments = engineRule.getRepositoryService()
        .createDeploymentQuery()
        .list();

    for (Deployment deployment : deployments) {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  @Test
  public void testRetryTimeCycle() {
    // given
    ProcessEngine processEngine = engineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R5/PT5M";
    String processDefinitionName = "testRetryTimeCycle";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionName, retryInterval);
    deployProcess(processEngine, bpmnModelInstance, processDefinitionName);

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionName).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // then
    Assert.assertEquals(5, job.getRetries());
  }

  @Test
  public void testRetryTimeCycleWithZeroRetries() {
    // given
    ProcessEngine processEngine = engineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R0/PT5M";
    String processDefinitionName = "testRetryTimeCycleWithZeroRetries";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionName, retryInterval);
    deployProcess(processEngine, bpmnModelInstance, processDefinitionName);

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionName).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // then
    Assert.assertEquals(1, job.getRetries());
  }

  @Test
  public void testRetryTimeCycleWithZeroRetriesAndFailure() {
    // given
    ProcessEngine processEngine = engineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R0/PT5M";
    String processDefinitionName = "testRetryTimeCycleWithZeroRetriesAndFailure";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionName, retryInterval);
    deployProcess(processEngine, bpmnModelInstance, processDefinitionName);

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionName).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // then
    Assert.assertEquals(1, job.getRetries());

    // when
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }
    job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // then
    Assert.assertEquals(0, job.getRetries());
  }

  @Test
  public void testRetryTimeCycleWithFailure() {
    // given
    ProcessEngine processEngine = engineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "R5/PT5M";
    String processDefinitionName = "testRetryTimeCycleWithFailure";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionName, retryInterval);
    deployProcess(processEngine, bpmnModelInstance, processDefinitionName);

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionName).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();
    Assert.assertEquals(5, job.getRetries());

    // when
    try {
      managementService.executeJob(job.getId());
    } catch (Exception e) {
      // ignore
    }
    job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // then
    Assert.assertEquals(4, job.getRetries());
  }

  @Test
  public void testRetryIntervals() {
    // given
    ProcessEngine processEngine = engineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "PT10M,PT17M,PT20M";
    String processDefinitionName = "testRetryIntervals";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionName, retryInterval);
    deployProcess(processEngine, bpmnModelInstance, processDefinitionName);

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionName).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // when
    Assert.assertEquals(4, job.getRetries());
  }

  @Test
  public void testRetryIntervalsWithFailure() {
    // given
    ProcessEngine processEngine = engineRule.getProcessEngine();
    RuntimeService runtimeService = processEngine.getRuntimeService();
    ManagementService managementService = processEngine.getManagementService();

    String retryInterval = "PT10M,PT17M,PT20M";
    String processDefinitionName = "testRetryIntervalsWithFailure";
    BpmnModelInstance bpmnModelInstance = getBpmnModelInstance(processDefinitionName, retryInterval);
    deployProcess(processEngine, bpmnModelInstance, processDefinitionName);

    // when
    String processInstanceId = runtimeService.startProcessInstanceByKey(processDefinitionName).getId();
    Job job = managementService.createJobQuery().processInstanceId(processInstanceId).singleResult();

    // then
    Assert.assertEquals(4, job.getRetries());

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

  private static BpmnModelInstance getBpmnModelInstance(String processDefinitionName, String retryStrategy) {
    return Bpmn.createExecutableProcess(processDefinitionName)
        .camundaHistoryTimeToLive(180)
        .startEvent()
        .serviceTask()
        .camundaAsyncBefore()
        .camundaFailedJobRetryTimeCycle(retryStrategy)
        .camundaClass(FailingDelegate.class.getName())
        .endEvent()
        .done();
  }

  private void deployProcess(ProcessEngine processEngine ,BpmnModelInstance bpmnModelInstance, String processDefinitionName) {
    processEngine.getRepositoryService()
        .createDeployment()
        .addModelInstance(processDefinitionName + ".bpmn", bpmnModelInstance)
        .deploy();
  }
}
