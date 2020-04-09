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
package org.camunda.bpm.engine.test.history.useroperationlog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;

import org.camunda.bpm.engine.EntityTypes;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Roman Smirnov
 *
 */
public class LegacyUserOperationLogTest {

  public static final String USER_ID = "demo";

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(
      "org/camunda/bpm/engine/test/history/useroperationlog/enable.legacy.user.operation.log.camunda.cfg.xml");
  public ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule(bootstrapRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(processEngineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(processEngineRule).around(testHelper);

  protected IdentityService identityService;
  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ManagementService managementService;

  protected Batch batch;

  @Before
  public void initServices() {
    identityService = processEngineRule.getIdentityService();
    runtimeService = processEngineRule.getRuntimeService();
    taskService = processEngineRule.getTaskService();
    historyService = processEngineRule.getHistoryService();
    managementService = processEngineRule.getManagementService();
  }

  @After
  public void removeBatch() {
    Batch batch = managementService.createBatchQuery().singleResult();
    if (batch != null) {
      managementService.deleteBatch(batch.getId(), true);
    }

    HistoricBatch historicBatch = historyService.createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      historyService.deleteHistoricBatch(historicBatch.getId());
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/history/useroperationlog/UserOperationLogTaskTest.testOnlyTaskCompletionIsLogged.bpmn20.xml")
  public void testLogAllOperationWithAuthentication() {
    try {
      // given
      identityService.setAuthenticatedUserId(USER_ID);
      String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

      String taskId = taskService.createTaskQuery().singleResult().getId();

      // when
      taskService.complete(taskId);

      // then
      assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "taskListenerCalled"));
      assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "serviceTaskCalled"));

      UserOperationLogQuery query = userOperationLogQuery().userId(USER_ID);
      assertEquals(4, query.count());
      assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE).count());
      assertEquals(1, query.operationType(UserOperationLogEntry.OPERATION_TYPE_COMPLETE).count());
      assertEquals(2, query.operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE).count());

    } finally {
      identityService.clearAuthentication();
    }
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/history/useroperationlog/UserOperationLogTaskTest.testOnlyTaskCompletionIsLogged.bpmn20.xml")
  public void testLogOperationWithoutAuthentication() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    String taskId = taskService.createTaskQuery().singleResult().getId();

    // when
    taskService.complete(taskId);

    // then
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "taskListenerCalled"));
    assertTrue((Boolean) runtimeService.getVariable(processInstanceId, "serviceTaskCalled"));

    assertEquals(5, userOperationLogQuery().count());
    assertEquals(1, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_COMPLETE).count());
    assertEquals(2, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE).count());
    assertEquals(1, userOperationLogQuery()
            .entityType(EntityTypes.DEPLOYMENT)
            .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
            .count());
    assertEquals(1, userOperationLogQuery()
            .entityType(EntityTypes.PROCESS_INSTANCE)
            .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
            .count());
  }

  @Test
  @Deployment(resources = "org/camunda/bpm/engine/test/history/useroperationlog/UserOperationLogTaskTest.testOnlyTaskCompletionIsLogged.bpmn20.xml")
  public void testLogSetVariableWithoutAuthentication() {
    // given
    String processInstanceId = runtimeService.startProcessInstanceByKey("process").getId();

    // when
    runtimeService.setVariable(processInstanceId, "aVariable", "aValue");

    // then
    assertEquals(3, userOperationLogQuery().count());
    assertEquals(1, userOperationLogQuery().operationType(UserOperationLogEntry.OPERATION_TYPE_SET_VARIABLE).count());
    assertEquals(1, userOperationLogQuery()
            .entityType(EntityTypes.DEPLOYMENT)
            .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
            .count());
    assertEquals(1, userOperationLogQuery()
            .entityType(EntityTypes.PROCESS_INSTANCE)
            .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
            .count());
  }

  @Test
  public void testDontWriteDuplicateLogOnBatchDeletionJobExecution() {
    ProcessDefinition definition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessInstance processInstance = runtimeService.startProcessInstanceById(definition.getId());
    batch = runtimeService.deleteProcessInstancesAsync(
        Arrays.asList(processInstance.getId()), null, "test reason");

    Job seedJob = managementService
        .createJobQuery()
        .singleResult();
    managementService.executeJob(seedJob.getId());

    for (Job pending : managementService.createJobQuery().list()) {
      managementService.executeJob(pending.getId());
    }

    assertEquals(5, userOperationLogQuery().entityTypeIn(EntityTypes.PROCESS_INSTANCE, EntityTypes.DEPLOYMENT).count());
  }

  @Test
  public void testDontWriteDuplicateLogOnBatchMigrationJobExecution() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceDefinition.getId());

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapEqualActivities()
      .build();

    batch = runtimeService.newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();
    Job seedJob = managementService
        .createJobQuery()
        .singleResult();
    managementService.executeJob(seedJob.getId());

    Job migrationJob = managementService.createJobQuery()
        .jobDefinitionId(batch.getBatchJobDefinitionId())
        .singleResult();

    // when
    managementService.executeJob(migrationJob.getId());

    // then
    assertEquals(9, userOperationLogQuery().count());
    assertEquals(2, userOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
        .entityType(EntityTypes.DEPLOYMENT)
        .count());
    assertEquals(1, userOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_CREATE)
        .entityType(EntityTypes.PROCESS_INSTANCE)
        .count());
    assertEquals(3, userOperationLogQuery()
        .operationType(UserOperationLogEntry.OPERATION_TYPE_MIGRATE)
        .entityType(EntityTypes.PROCESS_INSTANCE)
        .count());
  }

  protected UserOperationLogQuery userOperationLogQuery() {
    return historyService.createUserOperationLogQuery();
  }

}
