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
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.RestartProcessInstanceSyncTest.SetVariableExecutionListenerImpl;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.util.IncrementCounterListener;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 *
 * @author Anna Pazola
 *
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class RestartProcessInstanceAsyncTest {

  protected static final Date TEST_DATE = new Date(1457326800000L);

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchRestartHelper helper = new BatchRestartHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected TenantIdProvider defaultTenantIdProvider;
  protected boolean defaultEnsureJobDueDateSet;

  @Before
  public void init() {
    runtimeService = engineRule.getRuntimeService();
    taskService = engineRule.getTaskService();
    historyService = engineRule.getHistoryService();
    managementService = engineRule.getManagementService();

    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    defaultTenantIdProvider = processEngineConfiguration.getTenantIdProvider();
    defaultEnsureJobDueDateSet = processEngineConfiguration.isEnsureJobDueDateNotNull();
  }

  @After
  public void reset() {
    helper.removeAllRunningAndHistoricBatches();
    processEngineConfiguration.setTenantIdProvider(defaultTenantIdProvider);
    processEngineConfiguration.setEnsureJobDueDateNotNull(defaultEnsureJobDueDateSet);
  }

  @After
  public void resetClock() {
    ClockUtil.reset();
  }

  @Test
  public void createBatchRestart() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    List<String> processInstanceIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startAfterActivity("userTask2")
        .processInstanceIds(processInstanceIds)
        .executeAsync();

    // then
    assertBatchCreated(batch, 2);
  }

  @Test
  public void restartProcessInstanceWithNullProcessDefinitionId() {
    try {
      runtimeService.restartProcessInstances(null)
      .executeAsync();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("processDefinitionId is null");
    }
  }

  @Test
  public void restartProcessInstanceWithoutInstructions() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");

    try {
      Batch batch = runtimeService.restartProcessInstances(processDefinition.getId()).processInstanceIds(processInstance.getId()).executeAsync();
      helper.completeBatch(batch);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("instructions is empty");
    }
  }

  @Test
  public void restartProcessInstanceWithoutProcessInstanceIds() {
    try {
      runtimeService.restartProcessInstances("foo").startAfterActivity("bar").executeAsync();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("processInstanceIds is empty");
    }
  }

  @Test
  public void restartProcessInstanceWithNullProcessInstanceId() {
    try {
      runtimeService.restartProcessInstances("foo")
      .startAfterActivity("bar")
      .processInstanceIds((String) null)
      .executeAsync();
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("processInstanceIds contains null value");
    }
  }

  @Test
  public void restartNotExistingProcessInstance() {
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("bar")
        .processInstanceIds("aaa")
        .executeAsync();
    helper.completeSeedJobs(batch);
    try {
      helper.executeJobs(batch);
      fail("exception expected");
    } catch (BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Historic process instance cannot be found");
    }
  }

  @Test
  public void shouldRestartProcessInstance() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).active().singleResult();
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).active().singleResult();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .processInstanceIds(processInstance1.getId(),processInstance2.getId())
        .executeAsync();

    helper.completeSeedJobs(batch);

    // then
    helper.getExecutionJobs(batch).forEach(j -> assertEquals(processDefinition.getDeploymentId(), j.getDeploymentId()));

    // when
    helper.completeExecutionJobs(batch);
    helper.completeMonitorJobs(batch);

    // and
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    ProcessInstance restartedProcessInstance = restartedProcessInstances.get(0);
    Task restartedTask = engineRule.getTaskService().createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task1.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());

    restartedProcessInstance = restartedProcessInstances.get(1);
    restartedTask = engineRule.getTaskService().createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task2.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
  }

  @Test
  public void shouldRestartProcessInstanceWithParallelGateway() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .startBeforeActivity("userTask2")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    for (ProcessInstance restartedProcessInstance : restartedProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
      assertNotNull(updatedTree);
      assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .activity("userTask1")
          .activity("userTask2")
          .done());
    }
  }

  @Test
  public void shouldRestartProcessInstanceWithSubProcess() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("subProcess")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    for (ProcessInstance restartedProcessInstance : restartedProcessInstances) {
      ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
      assertNotNull(updatedTree);
      assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
      assertThat(updatedTree).hasStructure(
          describeActivityInstanceTree(
              processDefinition.getId())
          .beginScope("subProcess")
          .activity("userTask")
          .done());
    }
  }

  @Test
  public void shouldRestartProcessInstanceWithInitialVariables() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("Process")
        .startEvent()
        .userTask("userTask1")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, SetVariableExecutionListenerImpl.class.getName())
        .userTask("userTask2")
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);

     // initial variables
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValue("var", "bar"));
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process", Variables.createVariables().putValue("var", "bar"));

    // variables update
    List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processDefinition.getId()).active().list();
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // delete process instances
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .initialSetOfVariables()
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).active().list();
    VariableInstance variableInstance1 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstances.get(0).getId()).singleResult();
    VariableInstance variableInstance2 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstances.get(1).getId()).singleResult();

    assertEquals(variableInstance1.getExecutionId(), restartedProcessInstances.get(0).getId());
    assertEquals(variableInstance2.getExecutionId(), restartedProcessInstances.get(1).getId());
    assertEquals("var", variableInstance1.getName());
    assertEquals("bar", variableInstance1.getValue());
    assertEquals("var", variableInstance2.getName());
    assertEquals("bar", variableInstance2.getValue());
  }

  @Test
  public void shouldRestartProcessInstanceWithVariables() {
    // given
    BpmnModelInstance instance = Bpmn.createExecutableProcess("Process")
        .startEvent()
        .userTask("userTask1")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, SetVariableExecutionListenerImpl.class.getName())
        .userTask("userTask2")
        .endEvent()
        .done();

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(instance);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    // variables are set at the beginning
    runtimeService.setVariable(processInstance1.getId(), "var", "bar");
    runtimeService.setVariable(processInstance2.getId(), "var", "bb");

    // variables are changed
    List<Task> tasks = taskService.createTaskQuery().processDefinitionId(processDefinition.getId()).active().list();
    taskService.complete(tasks.get(0).getId());
    taskService.complete(tasks.get(1).getId());

    // process instances are deleted
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    ProcessInstance restartedProcessInstance = restartedProcessInstances.get(0);
    VariableInstance variableInstance1 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstance.getId()).singleResult();
    assertEquals(variableInstance1.getExecutionId(), restartedProcessInstance.getId());

    restartedProcessInstance = restartedProcessInstances.get(1);
    VariableInstance variableInstance2 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstance.getId()).singleResult();
    assertEquals(variableInstance2.getExecutionId(), restartedProcessInstance.getId());
    assertTrue(variableInstance1.getName().equals(variableInstance2.getName()));
    assertEquals("var", variableInstance1.getName());
    assertTrue(variableInstance1.getValue().equals(variableInstance2.getValue()));
    assertEquals("foo", variableInstance2.getValue());
  }

  @Test
  public void shouldNotSetLocalVariables() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    Execution subProcess1 = runtimeService.createExecutionQuery().processInstanceId(processInstance1.getId()).activityId("userTask").singleResult();
    Execution subProcess2 = runtimeService.createExecutionQuery().processInstanceId(processInstance2.getId()).activityId("userTask").singleResult();
    runtimeService.setVariableLocal(subProcess1.getId(), "local", "foo");
    runtimeService.setVariableLocal(subProcess2.getId(), "local", "foo");

    runtimeService.setVariable(processInstance1.getId(), "var", "bar");
    runtimeService.setVariable(processInstance2.getId(), "var", "bar");


    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");


    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask")
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);
    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).active().list();
    List<VariableInstance> variables1 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstances.get(0).getId()).list();
    assertEquals(1, variables1.size());
    assertEquals("var", variables1.get(0).getName());
    assertEquals("bar", variables1.get(0).getValue());
    List<VariableInstance> variables2 = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstances.get(1).getId()).list();
    assertEquals(1, variables1.size());
    assertEquals("var", variables2.get(0).getName());
    assertEquals("bar", variables2.get(0).getValue());
  }

  @Test
  public void shouldRestartProcessInstanceUsingHistoricProcessInstanceQuery() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    Task task1 = taskService.createTaskQuery().processInstanceId(processInstance1.getId()).active().singleResult();

    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");
    Task task2 = taskService.createTaskQuery().processInstanceId(processInstance2.getId()).active().singleResult();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    HistoricProcessInstanceQuery historicProcessInstanceQuery = engineRule.getHistoryService().createHistoricProcessInstanceQuery().processDefinitionId(processDefinition.getId());

    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .historicProcessInstanceQuery(historicProcessInstanceQuery)
        .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().active().list();
    ProcessInstance restartedProcessInstance = restartedProcessInstances.get(0);
    Task restartedTask = taskService.createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task1.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());

    restartedProcessInstance = restartedProcessInstances.get(1);
    restartedTask = taskService.createTaskQuery().processInstanceId(restartedProcessInstance.getId()).active().singleResult();
    Assert.assertEquals(task2.getTaskDefinitionKey(), restartedTask.getTaskDefinitionKey());
  }

  @Test
  public void testBatchCreationWithOverlappingProcessInstanceIdsAndQuery() {
    // given
    int processInstanceCount = 2;
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    HistoricProcessInstanceQuery processInstanceQuery = engineRule.getHistoryService()
        .createHistoricProcessInstanceQuery()
        .processDefinitionId(processDefinition.getId());

    Batch batch = runtimeService
      .restartProcessInstances(processDefinition.getId())
      .startTransition("flow1")
      .processInstanceIds(processInstance1.getId(), processInstance2.getId())
      .historicProcessInstanceQuery(processInstanceQuery)
      .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = engineRule.getRuntimeService().createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).list();
    assertEquals(restartedProcessInstances.size(), processInstanceCount);
  }

  @Test
  public void testMonitorJobPollingForCompletion() {
    processEngineConfiguration.setEnsureJobDueDateNotNull(false);

    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    // when the seed job creates the monitor job
    Date createDate = ClockTestUtil.setClockToDateWithoutMilliseconds();
    helper.completeSeedJobs(batch);

    // then the monitor job has a no due date set
    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
    assertNull(monitorJob.getDuedate());

    // when the monitor job is executed
    helper.executeMonitorJob(batch);

    // then the monitor job has a due date of the default batch poll time
    monitorJob = helper.getMonitorJob(batch);
    Date dueDate = helper.addSeconds(createDate, 30);
    assertEquals(dueDate, monitorJob.getDuedate());
  }

  @Test
  public void testMonitorJobPollingForCompletionDueDateSet() {
    ClockUtil.setCurrentTime(TEST_DATE);
    processEngineConfiguration.setEnsureJobDueDateNotNull(true);

    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
      .restartProcessInstances(processDefinition.getId())
      .startTransition("flow1")
      .processInstanceIds(processInstance1.getId(), processInstance2.getId())
      .executeAsync();

    // when the seed job creates the monitor job
    helper.completeSeedJobs(batch);

    // then the monitor job has the create date as due date set
    Job monitorJob = helper.getMonitorJob(batch);
    assertNotNull(monitorJob);
    assertEquals(TEST_DATE, monitorJob.getDuedate());

    // when the monitor job is executed
    helper.executeMonitorJob(batch);

    // then the monitor job has a due date of the default batch poll time
    monitorJob = helper.getMonitorJob(batch);
    Date dueDate = helper.addSeconds(TEST_DATE, 30);
    assertEquals(dueDate, monitorJob.getDuedate());
  }

  @Test
  public void testMonitorJobRemovesBatchAfterCompletion() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeSeedJobs(batch);
    helper.executeJobs(batch);

    helper.executeMonitorJob(batch);

    // then the batch was completed and removed
    assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

    // and the seed jobs was removed
    assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithCascade() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeSeedJobs(batch);

    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the batch was deleted
    assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

    // and the seed and execution job definition were deleted
    assertEquals(0, engineRule.getManagementService().createJobDefinitionQuery().count());

    // and the seed job and execution jobs were deleted
    assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchDeletionWithoutCascade() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeSeedJobs(batch);

    engineRule.getManagementService().deleteBatch(batch.getId(), false);

    // then the batch was deleted
    assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

    // and the seed and execution job definition were deleted
    assertEquals(0, engineRule.getManagementService().createJobDefinitionQuery().count());

    // and the seed job and execution jobs were deleted
    assertEquals(0, engineRule.getManagementService().createJobQuery().count());
  }

  @Test
  public void testBatchWithFailedSeedJobDeletionWithCascade() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    // create incident
    Job seedJob = helper.getSeedJob(batch);
    engineRule.getManagementService().setJobRetries(seedJob.getId(), 0);

    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedExecutionJobDeletionWithCascade() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeSeedJobs(batch);

    // create incidents
    List<Job> executionJobs = helper.getExecutionJobs(batch);
    for (Job executionJob : executionJobs) {
      engineRule.getManagementService().setJobRetries(executionJob.getId(), 0);
    }

    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testBatchWithFailedMonitorJobDeletionWithCascade() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService
        .restartProcessInstances(processDefinition.getId())
        .startTransition("flow1")
        .processInstanceIds(processInstance1.getId(), processInstance2.getId())
        .executeAsync();

    helper.completeSeedJobs(batch);

    // create incident
    Job monitorJob = helper.getMonitorJob(batch);
    engineRule.getManagementService().setJobRetries(monitorJob.getId(), 0);

    engineRule.getManagementService().deleteBatch(batch.getId(), true);

    // then the no historic incidents exists
    long historicIncidents = engineRule.getHistoryService().createHistoricIncidentQuery().count();
    assertEquals(0, historicIncidents);
  }

  @Test
  public void testJobsExecutionByJobExecutorWithAuthorizationEnabledAndTenant() {
    // given
    ProcessEngineConfigurationImpl processEngineConfiguration = engineRule.getProcessEngineConfiguration();

    processEngineConfiguration.setAuthorizationEnabled(true);
    ProcessDefinition processDefinition = testRule.deployForTenantAndGetDefinition("tenantId", ProcessModels.TWO_TASKS_PROCESS);

    try {
      ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
      ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

      List<String> list = Arrays.asList(processInstance1.getId(), processInstance2.getId());

      runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
      runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

      // when
      Batch batch = runtimeService
          .restartProcessInstances(processDefinition.getId())
          .startTransition("flow1")
          .processInstanceIds(list)
          .executeAsync();
      helper.completeSeedJobs(batch);

      testRule.waitForJobExecutorToProcessAllJobs();

      List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).list();
      // then all process instances were restarted
      for (ProcessInstance restartedProcessInstance : restartedProcessInstances) {
        ActivityInstance updatedTree = runtimeService.getActivityInstance(restartedProcessInstance.getId());
        assertNotNull(updatedTree);
        assertEquals(restartedProcessInstance.getId(), updatedTree.getProcessInstanceId());
        assertEquals("tenantId", restartedProcessInstance.getTenantId());

        assertThat(updatedTree).hasStructure(
            describeActivityInstanceTree(
                processDefinition.getId())
            .activity("userTask2")
            .done());
      }

    } finally {
      processEngineConfiguration.setAuthorizationEnabled(false);
    }

  }

  @Test
  public void restartProcessInstanceWithNotMatchingProcessDefinition() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("Process");
    runtimeService.deleteProcessInstance(processInstance.getId(), null);
    BpmnModelInstance instance2 = Bpmn.createExecutableProcess().startEvent().done();
    ProcessDefinition processDefinition2 = testRule.deployAndGetDefinition(instance2);

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition2.getId())
        .startBeforeActivity("userTask1")
        .processInstanceIds(processInstance.getId())
        .executeAsync();

    try {
      helper.completeBatch(batch);
      fail("exception expected");
    } catch (ProcessEngineException e) {
      // then
      assertThat(e.getMessage()).contains("Its process definition '" + processDefinition.getId() + "' does not match given process definition '" + processDefinition2.getId() +"'" );
    }
  }

  @Test
  public void shouldRestartProcessInstanceWithoutBusinessKey() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process", "businessKey1", (String) null);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process", "businessKey2", (String) null);

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .withoutBusinessKey()
    .executeAsync();

    helper.completeBatch(batch);
    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).active().list();
    ProcessInstance restartedProcessInstance1 = restartedProcessInstances.get(0);
    ProcessInstance restartedProcessInstance2 = restartedProcessInstances.get(1);
    assertNull(restartedProcessInstance1.getBusinessKey());
    assertNull(restartedProcessInstance2.getBusinessKey());
  }

  @Test
  public void shouldRestartProcessInstanceWithBusinessKey() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process", "businessKey1", (String) null);
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process", "businessKey2", (String) null);

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);
    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).active().list();
    ProcessInstance restartedProcessInstance1 = restartedProcessInstances.get(0);
    ProcessInstance restartedProcessInstance2 = restartedProcessInstances.get(1);
    assertNotNull(restartedProcessInstance1.getBusinessKey());
    assertNotNull(restartedProcessInstance2.getBusinessKey());
  }

  @Test
  public void shouldRestartProcessInstanceWithoutCaseInstanceId() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process", null, "caseInstanceId1");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process", null, "caseInstanceId2");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);
    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).active().list();
    ProcessInstance restartedProcessInstance1 = restartedProcessInstances.get(0);
    ProcessInstance restartedProcessInstance2 = restartedProcessInstances.get(1);
    assertNull(restartedProcessInstance1.getCaseInstanceId());
    assertNull(restartedProcessInstance2.getCaseInstanceId());
  }

  @Test
  public void shouldRestartProcessInstanceWithTenant() {
    // given
    ProcessDefinition processDefinition = testRule.deployForTenantAndGetDefinition("tenantId", ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");


    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);
    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).active().list();
    assertNotNull(restartedProcessInstances.get(0).getTenantId());
    assertNotNull(restartedProcessInstances.get(1).getTenantId());
    assertEquals("tenantId", restartedProcessInstances.get(0).getTenantId());
    assertEquals("tenantId", restartedProcessInstances.get(1).getTenantId());
  }

  @Test
  public void shouldSkipCustomListeners() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(modify(ProcessModels.TWO_TASKS_PROCESS).activityBuilder("userTask1")
        .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_START, IncrementCounterListener.class.getName()).done());
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    IncrementCounterListener.counter = 0;
    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .skipCustomListeners()
    .executeAsync();

    helper.completeBatch(batch);
    // then
    assertEquals(0, IncrementCounterListener.counter);
  }

  @Test
  public void shouldSkipIoMappings() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(
        modify(ProcessModels.TWO_TASKS_PROCESS).activityBuilder("userTask1").camundaInputParameter("foo", "bar").done());
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .skipIoMappings()
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).list();
    Execution task1Execution = runtimeService.createExecutionQuery().processInstanceId(restartedProcessInstances.get(0).getId()).activityId("userTask1").singleResult();
    assertNotNull(task1Execution);
    assertNull(runtimeService.getVariable(task1Execution.getId(), "foo"));

    task1Execution = runtimeService.createExecutionQuery().processInstanceId(restartedProcessInstances.get(1).getId()).activityId("userTask1").singleResult();
    assertNotNull(task1Execution);
    assertNull(runtimeService.getVariable(task1Execution.getId(), "foo"));
  }

  @Test
  public void shouldRetainTenantIdOfSharedProcessDefinition() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setTenantIdProvider(new TestTenantIdProvider());

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(processInstance.getTenantId(), TestTenantIdProvider.TENANT_ID);
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
      .startBeforeActivity(ProcessModels.USER_TASK_ID)
      .processInstanceIds(processInstance.getId())
      .executeAsync();

    helper.completeBatch(batch);

    // then
    ProcessInstance restartedInstance = runtimeService.createProcessInstanceQuery().active()
      .processDefinitionId(processDefinition.getId()).singleResult();

    assertNotNull(restartedInstance);
    assertEquals(restartedInstance.getTenantId(), TestTenantIdProvider.TENANT_ID);
  }

  @Test
  public void shouldSkipTenantIdProviderOnRestart() {
    // given
    engineRule.getProcessEngineConfiguration()
      .setTenantIdProvider(new TestTenantIdProvider());

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId());
    assertEquals(processInstance.getTenantId(), TestTenantIdProvider.TENANT_ID);
    runtimeService.deleteProcessInstance(processInstance.getId(), "test");

    // set tenant id provider to fail to verify it is not called during instantiation
    engineRule.getProcessEngineConfiguration()
      .setTenantIdProvider(new FailingTenantIdProvider());

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
      .startBeforeActivity(ProcessModels.USER_TASK_ID)
      .processInstanceIds(processInstance.getId())
      .executeAsync();

    helper.completeBatch(batch);

    // then
    ProcessInstance restartedInstance = runtimeService.createProcessInstanceQuery().active()
      .processDefinitionId(processDefinition.getId()).singleResult();

    assertNotNull(restartedInstance);
    assertEquals(restartedInstance.getTenantId(), TestTenantIdProvider.TENANT_ID);
  }

  @Test
  public void shouldNotSetInitialVariablesIfThereIsNoUniqueStartActivity() {
    // given
    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.createProcessInstanceById(processDefinition.getId())
        .startBeforeActivity("userTask2")
        .startBeforeActivity("userTask1")
        .execute();

    ProcessInstance processInstance2 = runtimeService.createProcessInstanceById(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .startBeforeActivity("userTask2")
        .setVariable("foo", "bar")
        .execute();

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
    .startBeforeActivity("userTask1")
    .initialSetOfVariables()
    .processInstanceIds(processInstance1.getId(), processInstance2.getId())
    .executeAsync();

    helper.completeBatch(batch);

    // then
    List<ProcessInstance> restartedProcessInstances = runtimeService.createProcessInstanceQuery().processDefinitionId(processDefinition.getId()).list();
    List<VariableInstance> variables = runtimeService.createVariableInstanceQuery().processInstanceIdIn(restartedProcessInstances.get(0).getId(), restartedProcessInstances.get(1).getId()).list();
    assertEquals(0, variables.size());
  }

  @Test
  public void shouldSetInvocationsPerBatchType() {
    // given
    engineRule.getProcessEngineConfiguration()
        .getInvocationsPerBatchJobByBatchType()
        .put(Batch.TYPE_PROCESS_INSTANCE_RESTART, 42);

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.startProcessInstanceByKey("Process");
    ProcessInstance processInstance2 = runtimeService.startProcessInstanceByKey("Process");

    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    runtimeService.deleteProcessInstance(processInstance2.getId(), "test");

    List<String> processInstanceIds = Arrays.asList(processInstance1.getId(), processInstance2.getId());

    // when
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startAfterActivity("userTask2")
        .processInstanceIds(processInstanceIds)
        .executeAsync();

    // then
    assertThat(batch.getInvocationsPerBatchJob()).isEqualTo(42);

    // clear
    engineRule.getProcessEngineConfiguration()
        .setInvocationsPerBatchJobByBatchType(new HashMap<>());
  }

  @Test
  public void shouldSetExecutionStartTimeInBatchAndHistory() {
    // given
    ClockUtil.setCurrentTime(TEST_DATE);

    ProcessDefinition processDefinition = testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);
    ProcessInstance processInstance1 = runtimeService.createProcessInstanceById(processDefinition.getId())
        .startBeforeActivity("userTask1")
        .execute();
    runtimeService.deleteProcessInstance(processInstance1.getId(), "test");
    Batch batch = runtimeService.restartProcessInstances(processDefinition.getId())
        .startAfterActivity("userTask2")
        .processInstanceIds(processInstance1.getId())
        .executeAsync();
    helper.executeSeedJob(batch);
    List<Job> executionJobs = helper.getExecutionJobs(batch);

    // when
    helper.executeJob(executionJobs.get(0));

    // then
    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    batch = managementService.createBatchQuery().singleResult();

    Assertions.assertThat(batch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
    Assertions.assertThat(historicBatch.getExecutionStartTime()).isEqualToIgnoringMillis(TEST_DATE);
  }

  protected void assertBatchCreated(Batch batch, int processInstanceCount) {
    assertNotNull(batch);
    assertNotNull(batch.getId());
    assertEquals("instance-restart", batch.getType());
    assertEquals(processInstanceCount, batch.getTotalJobs());
  }

  public static class TestTenantIdProvider extends FailingTenantIdProvider {

    static final String TENANT_ID = "testTenantId";

    @Override
    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return TENANT_ID;
    }

  }

  public static class FailingTenantIdProvider implements TenantIdProvider {

    @Override
    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      throw new UnsupportedOperationException();
    }

    @Override
    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      throw new UnsupportedOperationException();
    }
  }

}
