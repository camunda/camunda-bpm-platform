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

import java.util.Arrays;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricTaskInstance;
import org.camunda.bpm.engine.history.HistoricTaskInstanceQuery;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationHistoricTaskInstanceTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected TaskService taskService;

  @Before
  public void initServices() {
    historyService = rule.getHistoryService();
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateHistoryUserTaskInstance() {
    //given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS)
          .changeElementId("Process", "Process2")
          .changeElementId("userTask", "userTask2"));

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask2")
        .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    HistoricTaskInstanceQuery sourceHistoryTaskInstanceQuery =
        historyService.createHistoricTaskInstanceQuery()
          .processDefinitionId(sourceProcessDefinition.getId());
    HistoricTaskInstanceQuery targetHistoryTaskInstanceQuery =
        historyService.createHistoricTaskInstanceQuery()
          .processDefinitionId(targetProcessDefinition.getId());

    ActivityInstance activityInstance = runtimeService.getActivityInstance(processInstance.getId());

    //when
    assertEquals(1, sourceHistoryTaskInstanceQuery.count());
    assertEquals(0, targetHistoryTaskInstanceQuery.count());
    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    runtimeService.newMigration(migrationPlan)
      .processInstanceQuery(sourceProcessInstanceQuery)
      .execute();

    //then
    assertEquals(0, sourceHistoryTaskInstanceQuery.count());
    assertEquals(1, targetHistoryTaskInstanceQuery.count());

    HistoricTaskInstance instance = targetHistoryTaskInstanceQuery.singleResult();
    assertEquals(targetProcessDefinition.getKey(), instance.getProcessDefinitionKey());
    assertEquals(targetProcessDefinition.getId(), instance.getProcessDefinitionId());
    assertEquals("userTask2", instance.getTaskDefinitionKey());
    assertEquals(activityInstance.getActivityInstances("userTask")[0].getId(), instance.getActivityInstanceId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateWithSubTask() {
    //given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    Task task = taskService.createTaskQuery().singleResult();
    Task subTask = taskService.newTask();
    subTask.setParentTaskId(task.getId());
    taskService.saveTask(subTask);

    // when
    runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then the historic sub task instance is still the same
    HistoricTaskInstance historicSubTaskAfterMigration = historyService
        .createHistoricTaskInstanceQuery().taskId(subTask.getId()).singleResult();

    Assert.assertNotNull(historicSubTaskAfterMigration);
    Assert.assertNull(historicSubTaskAfterMigration.getProcessDefinitionId());
    Assert.assertNull(historicSubTaskAfterMigration.getProcessDefinitionKey());
    Assert.assertNull(historicSubTaskAfterMigration.getExecutionId());
    Assert.assertNull(historicSubTaskAfterMigration.getActivityInstanceId());
  }
}
