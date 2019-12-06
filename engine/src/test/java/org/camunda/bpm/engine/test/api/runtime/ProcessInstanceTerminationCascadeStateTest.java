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
package org.camunda.bpm.engine.test.api.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class ProcessInstanceTerminationCascadeStateTest {

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngine engine;
  protected RepositoryService repositoryService;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  protected boolean externallyTerminated;

  @Before
  public void init() {
    engine = engineRule.getProcessEngine();
    repositoryService = engine.getRepositoryService();
    runtimeService = engine.getRuntimeService();
    historyService = engine.getHistoryService();

    prepareDeployment();
  }

  protected void prepareDeployment() {
    BpmnModelInstance callee = Bpmn.createExecutableProcess("subProcess").startEvent().userTask("userTask").endEvent().done();
    BpmnModelInstance caller = Bpmn.createExecutableProcess("process").startEvent().callActivity("callActivity").calledElement("subProcess").endEvent().done();

    testRule.deploy(caller, callee);
  }

  @After
  public void teardown() {
    List<HistoricProcessInstance> processes = historyService.createHistoricProcessInstanceQuery().list();
    for (HistoricProcessInstance historicProcessInstance : processes) {
      historyService.deleteHistoricProcessInstance(historicProcessInstance.getId());
    }

    List<Deployment> deployments = repositoryService.createDeploymentQuery().list();
    for (Deployment deployment : deployments) {
      repositoryService.deleteDeployment(deployment.getId());
    }
  }

  @Test
  public void shouldCascadeStateFromSubprocessUpDeletion() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").singleResult();
    externallyTerminated = true;

    // when
    runtimeService.deleteProcessInstance(subProcess.getId(), "test", false, externallyTerminated);

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldNotCascadeStateFromSubprocessUpDeletion() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").singleResult();
    externallyTerminated = false;

    // when
    runtimeService.deleteProcessInstance(subProcess.getId(), "test", false, externallyTerminated);

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldCascadeStateFromProcessDownDeletion() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance process = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").singleResult();
    externallyTerminated = true;

    // when
    runtimeService.deleteProcessInstance(process.getId(), "test", false, externallyTerminated);

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldNotCascadeStateFromProcessDownDeletion() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance process = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").singleResult();
    externallyTerminated = false;

    // when
    runtimeService.deleteProcessInstance(process.getId(), "test", false, externallyTerminated);

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldNotCascadeStateFromSubprocessUpCancelation() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").singleResult();
    ActivityInstance activityInstance = runtimeService.getActivityInstance(subProcess.getId());
    externallyTerminated = false;

    // when
    runtimeService.createProcessInstanceModification(subProcess.getId()).cancellationSourceExternal(externallyTerminated).cancelActivityInstance(activityInstance.getId()).execute();

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldNotCascadeStateFromProcessDownCancelation() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance process = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").singleResult();
    ActivityInstance activityInstance = runtimeService.getActivityInstance(process.getId());
    externallyTerminated = false;

    // when
    runtimeService.createProcessInstanceModification(process.getId()).cancellationSourceExternal(externallyTerminated).cancelActivityInstance(activityInstance.getId()).execute();

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldCascadeStateFromSubprocessUpCancelation() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance subProcess = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess").singleResult();
    ActivityInstance activityInstance = runtimeService.getActivityInstance(subProcess.getId());
    externallyTerminated = true;

    // when
    runtimeService.createProcessInstanceModification(subProcess.getId()).cancellationSourceExternal(externallyTerminated).cancelActivityInstance(activityInstance.getId()).execute();

    // then
    assertHistoricProcessInstances();
  }

  @Test
  public void shouldCascadeStateFromProcessDownCancelation() {
    // given
    runtimeService.startProcessInstanceByKey("process");
    ProcessInstance process = runtimeService.createProcessInstanceQuery().processDefinitionKey("process").singleResult();
    ActivityInstance activityInstance = runtimeService.getActivityInstance(process.getId());
    externallyTerminated = true;

    // when
    runtimeService.createProcessInstanceModification(process.getId()).cancellationSourceExternal(externallyTerminated).cancelActivityInstance(activityInstance.getId()).execute();

    // then
    assertHistoricProcessInstances();
  }

  protected void assertHistoricProcessInstances() {
    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();
    assertThat(historicProcessInstances.size()).isEqualTo(2);
    for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
      assertThat(historicProcessInstance.getState())
          .isEqualTo(externallyTerminated ? HistoricProcessInstance.STATE_EXTERNALLY_TERMINATED : HistoricProcessInstance.STATE_INTERNALLY_TERMINATED);
    }
  }
}
