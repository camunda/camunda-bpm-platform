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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.camunda.bpm.engine.test.util.MigratingProcessInstanceValidationReportAssert.assertThat;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.camunda.bpm.engine.delegate.TaskListener;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
import org.camunda.bpm.engine.impl.jobexecutor.TimerTaskListenerJobHandler;
import org.camunda.bpm.engine.impl.persistence.entity.JobEntity;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.management.ActivityStatistics;
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.migration.MigratingProcessInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.util.AccessModelInstanceTaskListener;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.camunda.bpm.model.bpmn.instance.camunda.CamundaTaskListener;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationUserTaskTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testUserTaskMigrationInProcessDefinitionScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then

    // the entities were migrated
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .done());

    Task task = testHelper.snapshotBeforeMigration.getTaskForKey("userTask");
    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertEquals(task.getId(), migratedTask.getId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());

  }

  @Test
  public void testUserTaskMigrationInSubProcessScope() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then

    // the entities were migrated
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("userTask"))
          .done());

    Task task = testHelper.snapshotBeforeMigration.getTaskForKey("userTask");
    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertEquals(task.getId(), migratedTask.getId());
    Assert.assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testConcurrentUserTaskMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then

    // the entities were migrated
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask1").concurrent().noScope().up()
          .child("userTask2").concurrent().noScope()
          .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(2, migratedTasks.size());

    for (Task migratedTask : migratedTasks) {
      Assert.assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testCannotMigrateWhenNotAllActivityInstancesAreMapped() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .build();


    // when
    try {
      testHelper.createProcessInstanceAndMigrate(migrationPlan);
      Assert.fail("should not succeed because the userTask2 instance is not mapped");
    } catch (MigratingProcessInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasActivityInstanceFailures("userTask2", "There is no migration instruction for this instance's activity");
    }
  }

  @Test
  public void testCannotMigrateWhenNotAllTransitionInstancesAreMapped() {
    // given
    BpmnModelInstance model = ModifiableBpmnModelInstance.modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
        .activityBuilder("userTask1")
        .camundaAsyncBefore()
        .moveToActivity("userTask2")
        .camundaAsyncBefore()
        .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(model);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(model);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .build();


    // when
    try {
      testHelper.createProcessInstanceAndMigrate(migrationPlan);
      Assert.fail("should not succeed because the userTask2 instance is not mapped");
    } catch (MigratingProcessInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasTransitionInstanceFailures("userTask2", "There is no migration instruction for this instance's activity");
    }
  }

  @Test
  public void testChangeActivityId() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .createProcessInstanceById(sourceProcessDefinition.getId())
        .startBeforeActivity("userTask1")
        .execute();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask2").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask2", testHelper.getSingleActivityInstanceBeforeMigration("userTask1").getId())
        .done());

    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask2");
    Assert.assertNotNull(migratedTask);
    Assert.assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    Assert.assertEquals("userTask2", migratedTask.getTaskDefinitionKey());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateWithSubTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Task task = rule.getTaskService().createTaskQuery().singleResult();
    Task subTask = rule.getTaskService().newTask();
    subTask.setParentTaskId(task.getId());
    rule.getTaskService().saveTask(subTask);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then the sub task properties have not been updated (i.e. subtask should not reference the process instance/definition now)
    Task subTaskAfterMigration = rule.getTaskService().createTaskQuery().taskId(subTask.getId()).singleResult();
    Assert.assertNull(subTaskAfterMigration.getProcessDefinitionId());
    Assert.assertNull(subTaskAfterMigration.getProcessInstanceId());
    Assert.assertNull(subTaskAfterMigration.getTaskDefinitionKey());

    // the tasks can be completed and the process can be ended
    rule.getTaskService().complete(subTask.getId());
    rule.getTaskService().complete(task.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());

    if (!rule.getProcessEngineConfiguration().getHistoryLevel().equals(HistoryLevel.HISTORY_LEVEL_NONE)) {
      rule.getHistoryService().deleteHistoricTaskInstance(subTaskAfterMigration.getId());
    }
  }

  @Test
  public void testAccessModelInNewAssignmentTaskListenerAfterMigration() {
    BpmnModelInstance targetModel = modify(ProcessModels.ONE_TASK_PROCESS).changeElementId("userTask", "newUserTask");
    addTaskListener(targetModel, "newUserTask", TaskListener.EVENTNAME_ASSIGNMENT, AccessModelInstanceTaskListener.class.getName());

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetModel);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "newUserTask")
      .build();

    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // when
    Task task = rule.getTaskService().createTaskQuery().singleResult();

    rule.getTaskService().setAssignee(task.getId(), "foo");

    // then the task listener was able to access the bpmn model instance and set a variable
    String variableValue =
        (String) rule.getRuntimeService().getVariable(processInstance.getId(), AccessModelInstanceTaskListener.VARIABLE_NAME);
    Assert.assertEquals("newUserTask", variableValue);

  }

  @Test
  public void testAccessModelInNewTimeoutTaskListenerAfterMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobCreated("userTask");

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testTimeoutTaskListenerRemovedAfterMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobRemoved("userTask");
  }

  @Test
  public void testTimeoutTaskListenerMigratedAfterMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobMigrated("userTask", "userTask");

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testTimeoutTaskListenerMigratedAndUpdatedAfterMigration() {
    // given
    ClockTestUtil.setClockToDateWithoutMilliseconds();

    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities().updateEventTriggers()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Date newDueDate = new DateTime(ClockUtil.getCurrentTime()).plusHours(3).toDate();
    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "userTask",
        newDueDate);

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testAccessModelInNewTimeoutTaskListenerAfterMigrationToDifferentUserTask() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerDifferentTask.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapActivities("userTask", "userTask2")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobCreated("userTask2");
    assertThat(testHelper.snapshotBeforeMigration.getJobs().size()).isEqualTo(0);
    assertThat(testHelper.snapshotAfterMigration.getJobs().size()).isEqualTo(1);

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask2");
  }

  @Test
  public void testTimeoutTaskListenerRemovedAfterMigrationToDifferentUserTask() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerDifferentTask.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapActivities("userTask2", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobRemoved("userTask2");
    assertThat(testHelper.snapshotBeforeMigration.getJobs().size()).isEqualTo(1);
    assertThat(testHelper.snapshotAfterMigration.getJobs().size()).isEqualTo(0);
  }

  @Test
  public void testTimeoutTaskListenerMigratedAfterMigrationToDifferentUserTask() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerDifferentTask.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapActivities("userTask2", "userTask")
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobMigrated("userTask2", "userTask");
    assertThat(testHelper.snapshotBeforeMigration.getJobs().size()).isEqualTo(1);
    assertThat(testHelper.snapshotAfterMigration.getJobs().size()).isEqualTo(1);

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testTimeoutTaskListenerMigratedAndUpdatedAfterMigrationToDifferentUserTask() {
    // given
    ClockTestUtil.setClockToDateWithoutMilliseconds();

    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerDifferentTask.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapActivities("userTask2", "userTask").updateEventTrigger()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Date newDueDate = new DateTime(ClockUtil.getCurrentTime()).plusHours(3).toDate();
    testHelper.assertTaskListenerTimerJobMigrated("userTask2", "userTask", newDueDate);
    assertThat(testHelper.snapshotBeforeMigration.getJobs().size()).isEqualTo(1);
    assertThat(testHelper.snapshotAfterMigration.getJobs().size()).isEqualTo(1);

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testAccessModelInNewTimeoutTaskListenerAfterMultipleListenerMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.twoTimeoutTaskListeners.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobsCreated("userTask", 2);
    ClockUtil.offset(TimeUnit.MINUTES.toMillis(70L));
    testHelper.waitForJobExecutorToProcessAllJobs(5000L);
    String variableValue =
        (String) rule.getRuntimeService().getVariable(processInstance.getId(), AccessModelInstanceTaskListener.VARIABLE_NAME);
    Assert.assertEquals("userTask", variableValue);
  }

  @Test
  public void testOneTimeoutTaskListenerRemovedAfterMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.twoTimeoutTaskListeners.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getJobs().size()).isEqualTo(2);
    assertThat(testHelper.snapshotAfterMigration.getJobs().size()).isEqualTo(1);
    JobEntity job = (JobEntity) testHelper.snapshotAfterMigration.getJobs().get(0);
    String jobHandlerConfiguration = job.getJobHandlerConfigurationRaw();
    assertThat(jobHandlerConfiguration).contains("timeout-friendly");
  }

  @Test
  public void testOneTimeoutTaskListenerAddedAfterMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.twoTimeoutTaskListeners.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(testHelper.snapshotBeforeMigration.getJobs().size()).isEqualTo(1);
    JobEntity job = (JobEntity) testHelper.snapshotBeforeMigration.getJobs().get(0);
    String jobHandlerConfiguration = job.getJobHandlerConfigurationRaw();
    assertThat(jobHandlerConfiguration).contains("timeout-friendly");

    assertThat(testHelper.snapshotAfterMigration.getJobs().size()).isEqualTo(2);
    job = (JobEntity) testHelper.snapshotAfterMigration.getJobs().get(1);
    jobHandlerConfiguration = job.getJobHandlerConfigurationRaw();
    assertThat(jobHandlerConfiguration).contains("timeout-hard");
  }

  @Test
  public void testTriggeredTimeoutTaskListenerNotStartedAgainAfterMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.twoTimeoutTaskListenersPastDate.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.twoTimeoutTaskListenersPastDate.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinitionId);
    assertThat(rule.getManagementService().createJobQuery().count()).isEqualTo(2L);
    assertThat(rule.getManagementService().createJobQuery().executable().count()).isEqualTo(1L);
    testHelper.waitForJobExecutorToProcessAllJobs(5000L);
    assertThat(rule.getManagementService().createJobQuery().count()).isEqualTo(1L);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    assertThat(rule.getManagementService().createJobQuery().count()).isEqualTo(1L);
  }

  @Test
  public void testTimeoutTaskListenerMigratedAfterMultipleListenerMigration() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobMigrated("userTask", "userTask");

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testTimeoutTaskListenerMigratedAndUpdatedAfterMultipleListenerMigration() {
    // given
    ClockTestUtil.setClockToDateWithoutMilliseconds();

    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities().updateEventTriggers()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Date newDueDate = new DateTime(ClockUtil.getCurrentTime()).plusHours(3).toDate();
    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
        "userTask",
        newDueDate);

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testAccessModelInMigratedTimeoutTaskListenerAfterMigrationToDifferentUserTask() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListener.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListener.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobCreated("userTask");

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }


  @Test
  public void testAccessModelInNewTimeoutTaskListenerAfterMigrationWithBoundaryEvent() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobCreated("userTask");

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testTimeoutTaskListenerRemovedAfterMigrationWithBoundaryEvent() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.noTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobRemoved("userTask");
  }

  @Test
  public void testTimeoutTaskListenerMigratedAfterMigrationWithBoundaryEvent() {
    // given
    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertTaskListenerTimerJobMigrated("userTask", "userTask");

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void testTimeoutTaskListenerMigratedAndUpdatedAfterMigrationWithBoundaryEvent() {
    // given
    ClockTestUtil.setClockToDateWithoutMilliseconds();

    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.oneTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();
    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition("org/camunda/bpm/engine/test/api/runtime/migration/MigrationUserTaskTest.changedTimeoutTaskListenerWithBoundaryEvent.bpmn20.xml")
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
      .mapEqualActivities().updateEventTriggers()
      .build();

    // when
    ProcessInstance processInstance = testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Date newDueDate = new DateTime(ClockUtil.getCurrentTime()).plusHours(3).toDate();
    Job taskListenerJob = testHelper.snapshotBeforeMigration.getJobForDefinitionId(
        testHelper.snapshotBeforeMigration.getJobDefinitionForActivityIdAndType(
            "userTask", TimerTaskListenerJobHandler.TYPE).getId());
    testHelper.assertJobMigrated(taskListenerJob, "userTask", newDueDate);

    // and the task listener was able to access the bpmn model instance and set a variable
    testTimeoutListenerCanBeTriggered(processInstance, "userTask");
  }

  @Test
  public void shouldRemainActiveAfterBecomingNoneScope() {
    // given
    BpmnModelInstance sourceProcess = Bpmn.createExecutableProcess("source")
        .startEvent()
        .userTask("user-task")
          .boundaryEvent()
            .condition("${true == false}")
        .endEvent()
        .done();

    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition(sourceProcess)
        .getId();

    BpmnModelInstance targetProcess = Bpmn.createExecutableProcess("target")
        .startEvent()
        .userTask("user-task")
        .endEvent()
        .done();

    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition(targetProcess)
        .getId();

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceByKey("source");

    String definitionId = rule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("source")
        .singleResult()
        .getId();

    ActivityStatistics activityStatistics = rule.getManagementService()
        .createActivityStatisticsQuery(definitionId)
        .singleResult();

    // assume
    assertThat(activityStatistics.getId()).isEqualTo("user-task");
    assertThat(activityStatistics.getInstances()).isEqualTo(1);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    definitionId = rule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("target")
        .singleResult()
        .getId();

    activityStatistics = rule.getManagementService()
        .createActivityStatisticsQuery(definitionId)
        .singleResult();

    assertThat(activityStatistics.getId()).isEqualTo("user-task");
    assertThat(activityStatistics.getInstances()).isEqualTo(1);
  }

  @Test
  public void shouldRemainActiveAfterBecomingScope() {
    // given
    BpmnModelInstance sourceProcess = Bpmn.createExecutableProcess("source")
        .startEvent()
        .userTask("user-task")
        .endEvent()
        .done();

    String sourceProcessDefinitionId = testHelper
        .deployAndGetDefinition(sourceProcess)
        .getId();

    BpmnModelInstance targetProcess = Bpmn.createExecutableProcess("target")
        .startEvent()
        .userTask("user-task")
          .boundaryEvent()
            .condition("${true == false}")
        .endEvent()
        .done();

    String targetProcessDefinitionId = testHelper
        .deployAndGetDefinition(targetProcess)
        .getId();

    String definitionId = rule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("source")
        .singleResult()
        .getId();

    // assume
    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceByKey("source");

    ActivityStatistics activityStatistics = rule.getManagementService()
        .createActivityStatisticsQuery(definitionId)
        .singleResult();

    assertThat(activityStatistics.getId()).isEqualTo("user-task");
    assertThat(activityStatistics.getInstances()).isEqualTo(1);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinitionId, targetProcessDefinitionId)
        .mapEqualActivities()
        .updateEventTriggers()
        .build();

    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    definitionId = rule.getRepositoryService().createProcessDefinitionQuery()
        .processDefinitionKey("target")
        .singleResult()
        .getId();

    activityStatistics = rule.getManagementService()
        .createActivityStatisticsQuery(definitionId)
        .singleResult();

    assertThat(activityStatistics.getId()).isEqualTo("user-task");
    assertThat(activityStatistics.getInstances()).isEqualTo(1);
  }

  protected static void addTaskListener(BpmnModelInstance targetModel, String activityId, String event, String className) {
    CamundaTaskListener taskListener = targetModel.newInstance(CamundaTaskListener.class);
    taskListener.setCamundaClass(className);
    taskListener.setCamundaEvent(event);

    UserTask task = targetModel.getModelElementById(activityId);
    task.builder().addExtensionElement(taskListener);
  }

  protected void testTimeoutListenerCanBeTriggered(ProcessInstance processInstance, String activityId) {
    JobDefinition jobDefinition = testHelper.snapshotAfterMigration.getJobDefinitionForActivityIdAndType(activityId, TimerTaskListenerJobHandler.TYPE);
    rule.getManagementService().executeJob(testHelper.snapshotAfterMigration.getJobForDefinitionId(jobDefinition.getId()).getId());
    String variableValue =
        (String) rule.getRuntimeService().getVariable(processInstance.getId(), AccessModelInstanceTaskListener.VARIABLE_NAME);
    Assert.assertEquals(activityId, variableValue);
  }

}
