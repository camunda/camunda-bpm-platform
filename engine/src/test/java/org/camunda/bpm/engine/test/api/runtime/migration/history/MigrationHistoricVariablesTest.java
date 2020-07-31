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
package org.camunda.bpm.engine.test.api.runtime.migration.history;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.impl.history.event.HistoricVariableUpdateEventEntity;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.AsyncProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CompensationModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MultiInstanceProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationHistoricVariablesTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  protected static final BpmnModelInstance ONE_BOUNDARY_TASK = ModifiableBpmnModelInstance.modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
      .boundaryEvent()
      .message("Message")
      .done();

  protected static final BpmnModelInstance CONCURRENT_BOUNDARY_TASKS = ModifiableBpmnModelInstance.modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
      .boundaryEvent()
      .message("Message")
      .moveToActivity("userTask2")
      .boundaryEvent()
      .message("Message")
      .done();

  protected static final BpmnModelInstance SUBPROCESS_CONCURRENT_BOUNDARY_TASKS = ModifiableBpmnModelInstance.modify(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS)
      .activityBuilder("userTask1")
      .boundaryEvent()
      .message("Message")
      .moveToActivity("userTask2")
      .boundaryEvent()
      .message("Message")
      .done();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    historyService = rule.getHistoryService();
    managementService = rule.getManagementService();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void noHistoryUpdateOnSameStructureMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ONE_BOUNDARY_TASK);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ONE_BOUNDARY_TASK);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = runtimeService
        .startProcessInstanceById(sourceProcessDefinition.getId());
    ExecutionTree executionTreeBeforeMigration =
        ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());

    ExecutionTree scopeExecution = executionTreeBeforeMigration.getExecutions().get(0);

    runtimeService.setVariableLocal(scopeExecution.getId(), "foo", 42);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then there is still one historic variable instance
    Assert.assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());

    // and no additional historic details
    Assert.assertEquals(1, historyService.createHistoricDetailQuery().count());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void noHistoryUpdateOnAddScopeMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CONCURRENT_BOUNDARY_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(SUBPROCESS_CONCURRENT_BOUNDARY_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = runtimeService
        .startProcessInstanceById(sourceProcessDefinition.getId());
    ExecutionTree executionTreeBeforeMigration =
        ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());

    ExecutionTree userTask1CCExecutionBefore  = executionTreeBeforeMigration
        .getLeafExecutions("userTask1")
        .get(0)
        .getParent();

    runtimeService.setVariableLocal(userTask1CCExecutionBefore.getId(), "foo", 42);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then there is still one historic variable instance
    Assert.assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());

    // and no additional historic details
    Assert.assertEquals(1, historyService.createHistoricDetailQuery().count());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testMigrateHistoryVariableInstance() {
    //given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    runtimeService.setVariable(processInstance.getId(), "test", 3537);
    HistoricVariableInstance instance = historyService.createHistoricVariableInstanceQuery().singleResult();

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    //when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    //then
    HistoricVariableInstance migratedInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertEquals(targetDefinition.getKey(), migratedInstance.getProcessDefinitionKey());
    assertEquals(targetDefinition.getId(), migratedInstance.getProcessDefinitionId());
    assertEquals(instance.getActivityInstanceId(), migratedInstance.getActivityInstanceId());
    assertEquals(instance.getExecutionId(), migratedInstance.getExecutionId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testMigrateHistoryVariableInstanceMultiInstance() {
    //given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_SUBPROCESS_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapEqualActivities()
      .build();

    //when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    //then
    List<HistoricVariableInstance> migratedVariables = historyService.createHistoricVariableInstanceQuery().list();
    Assert.assertEquals(6, migratedVariables.size()); // 3 loop counter + nrOfInstance + nrOfActiveInstances + nrOfCompletedInstances

    for (HistoricVariableInstance variable : migratedVariables) {
      assertEquals(targetDefinition.getKey(), variable.getProcessDefinitionKey());
      assertEquals(targetDefinition.getId(), variable.getProcessDefinitionId());

    }
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testMigrateEventScopeVariable() {
    //given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapActivities("userTask2", "userTask2")
      .mapActivities("subProcess", "subProcess")
      .mapActivities("compensationBoundary", "compensationBoundary")
      .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    Execution subProcessExecution = runtimeService.createExecutionQuery().activityId("userTask1").singleResult();

    runtimeService.setVariableLocal(subProcessExecution.getId(), "foo", "bar");

    testHelper.completeTask("userTask1");

    Execution eventScopeExecution = runtimeService.createExecutionQuery().activityId("subProcess").singleResult();
    HistoricVariableInstance eventScopeVariable = historyService
      .createHistoricVariableInstanceQuery()
      .executionIdIn(eventScopeExecution.getId())
      .singleResult();

    //when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(processInstance.getId())
      .execute();

    // then
    HistoricVariableInstance historicVariableInstance = historyService
      .createHistoricVariableInstanceQuery()
      .variableId(eventScopeVariable.getId())
      .singleResult();
    Assert.assertEquals(targetDefinition.getId(), historicVariableInstance.getProcessDefinitionId());
  }


  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
  public void testMigrateHistoricVariablesAsyncBeforeStartEvent() {
    //given
    String userTask = "task";
    BpmnModelInstance failing =
        Bpmn.createExecutableProcess("Process")
        .startEvent("startEvent")
        .camundaAsyncBefore(true)
        .serviceTask("failing")
        .camundaClass("foo")
        .userTask(userTask)
        .endEvent("endEvent")
        .done();
    BpmnModelInstance passing =
        Bpmn.createExecutableProcess("Process")
        .startEvent("startEvent")
        .camundaAsyncBefore(true)
        .userTask(userTask)
        .endEvent("endEvent")
        .done();

    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(failing);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(passing);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId(),
        Variables.createVariables().putValue("foo", "bar"));

    Job job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    executeJob(job);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("startEvent", "startEvent")
        .mapActivities(userTask, userTask)
        .build();

    // when migrate
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then the failed job is also migrated
    job = managementService.createJobQuery().singleResult();
    assertNotNull(job);
    assertEquals(0, job.getRetries());
    managementService.setJobRetries(job.getId(), 1);

    // when the failed job is executed again
    executeJob(managementService.createJobQuery().singleResult());

    // then job succeeds
    assertNull(managementService.createJobQuery().singleResult());
    assertNotNull(runtimeService.createProcessInstanceQuery().activityIdIn(userTask).singleResult());

    // and variable history was written
    HistoricVariableInstance migratedInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertEquals(targetDefinition.getKey(), migratedInstance.getProcessDefinitionKey());
    assertEquals(targetDefinition.getId(), migratedInstance.getProcessDefinitionId());

    // details
    HistoricVariableUpdateEventEntity historicDetail = (HistoricVariableUpdateEventEntity) historyService.createHistoricDetailQuery()
        .processInstanceId(processInstance.getId())
        .singleResult();

    assertNotNull(historicDetail);
    assertTrue(historicDetail.isInitial());
    assertEquals("foo", historicDetail.getVariableName());
    assertEquals("bar", historicDetail.getTextValue());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
  public void testMigrateHistoryVariableInstanceWithAsyncBefore() {
    //given
    BpmnModelInstance model = AsyncProcessModels.ASYNC_BEFORE_USER_TASK_PROCESS;

    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(model)
        .changeElementId(ProcessModels.PROCESS_KEY, "new" + ProcessModels.PROCESS_KEY));

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    runtimeService.setVariable(processInstance.getId(), "test", 3537);
    HistoricVariableInstance instance = historyService.createHistoricVariableInstanceQuery().singleResult();

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    //when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    //then
    HistoricVariableInstance migratedInstance = historyService.createHistoricVariableInstanceQuery().singleResult();
    assertEquals(targetDefinition.getKey(), migratedInstance.getProcessDefinitionKey());
    assertEquals(targetDefinition.getId(), migratedInstance.getProcessDefinitionId());
    assertEquals(instance.getActivityInstanceId(), migratedInstance.getActivityInstanceId());
    assertEquals(instance.getExecutionId(), migratedInstance.getExecutionId());
  }

  protected void executeJob(Job job) {
    ManagementService managementService = rule.getManagementService();

    while (job != null && job.getRetries() > 0) {
      try {
        managementService.executeJob(job.getId());
      }
      catch (Exception e) {
        // ignore
      }

      job = managementService.createJobQuery().jobId(job.getId()).singleResult();
    }
  }
}
