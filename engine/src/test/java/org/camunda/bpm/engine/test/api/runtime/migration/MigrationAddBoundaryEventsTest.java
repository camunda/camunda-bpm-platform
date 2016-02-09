/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.impl.persistence.entity.MessageEventSubscriptionEntity;
import org.camunda.bpm.engine.impl.persistence.entity.SignalEventSubscriptionEntity;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationAddBoundaryEventsTest {

  public static final String AFTER_BOUNDARY_TASK = "afterBoundary";
  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "3016-02-11T12:13:14Z";

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  public ProcessEngine processEngine;
  public RuntimeService runtimeService;
  public TaskService taskService;

  public ProcessInstance processInstance;
  public ActivityInstance originalActivityTree;
  public ActivityInstance updatedActivityTree;
  public ExecutionTree migratedExecutionTree;
  public ManagementService managementService;

  @Before
  public void initServices() {
    processEngine = rule.getProcessEngine();
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    managementService = rule.getManagementService();
  }

  @Test
  public void testAddMessageBoundaryEventToUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToScopeUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope()
          .up().up()
          .child("userTask2").concurrent().noScope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask1"))
          .up().up()
          .child(null).concurrent().noScope()
          .child("userTask2").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask2"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentScopeUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcessAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcessWithScopeUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess1").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess1"))
          .up().up()
          .child(null).concurrent().noScope()
          .child("userTask2").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess2"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess1", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess1").getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .endScope()
        .beginScope("subProcess2", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess2").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMessageBoundaryEventToParallelSubProcessAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess1").builder()
      .boundaryEvent().message(MESSAGE_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToScopeUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope()
          .up().up()
          .child("userTask2").concurrent().noScope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask1"))
          .up().up()
          .child(null).concurrent().noScope()
          .child("userTask2").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask2"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentScopeUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcessAndCorrelateSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcessWithScopeUserTaskAndCorrelateSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess1").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess1"))
          .up().up()
          .child(null).concurrent().noScope()
          .child("userTask2").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess2"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess1", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess1").getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .endScope()
        .beginScope("subProcess2", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess2").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertSignalEventSubscriptionExists(SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddSignalBoundaryEventToParallelSubProcessAndCorrelateSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess1").builder()
      .boundaryEvent().signal(SIGNAL_NAME)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }
  
  @Test
  public void testAddTimerBoundaryEventToUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToScopeUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope()
          .up().up()
          .child("userTask2").concurrent().noScope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask1"))
          .up().up()
          .child(null).concurrent().noScope()
          .child("userTask2").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask2"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentScopeUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcessAndCorrelateTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "userTask"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcessWithScopeUserTaskAndCorrelateTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess1").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).concurrent().noScope()
          .child("userTask1").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess1"))
          .up().up()
          .child(null).concurrent().noScope()
          .child("userTask2").scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess2"))
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess1", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess1").getId())
        .activity("userTask1", testHelper.getSingleActivityInstance(originalActivityTree, "userTask1").getId())
        .endScope()
        .beginScope("subProcess2", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess2").getId())
        .activity("userTask2", testHelper.getSingleActivityInstance(originalActivityTree, "userTask2").getId())
        .done());

    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddTimerBoundaryEventToParallelSubProcessAndCorrelateTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess1").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testAddMultipleBoundaryEvents() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("subProcess").builder()
      .boundaryEvent().timerWithDate(TIMER_DATE)
      .endEvent()
      .moveToActivity("userTask")
      .boundaryEvent().message(MESSAGE_NAME)
      .endEvent()
      .moveToActivity("userTask")
      .boundaryEvent().signal(SIGNAL_NAME)
      .endEvent()
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertThat(migratedExecutionTree)
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(processInstance.getId())
          .child(null).scope().id(testHelper.getSingleExecutionIdForActivity(originalActivityTree, "subProcess"))
          .child("userTask").scope()
          .done());

    assertThat(updatedActivityTree).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstance(originalActivityTree, "subProcess").getId())
        .activity("userTask", testHelper.getSingleActivityInstance(originalActivityTree, "userTask").getId())
        .done());

    assertMessageEventSubscriptionExists(MESSAGE_NAME);
    assertSignalEventSubscriptionExists(SIGNAL_NAME);
    assertTimerJobExsits();

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }

  // helper

  protected void createProcessInstanceAndMigrate(MigrationPlan migrationPlan) {
    processInstance = runtimeService.startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    originalActivityTree = runtimeService.getActivityInstance(processInstance.getId());
    runtimeService.executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
    updatedActivityTree = runtimeService.getActivityInstance(processInstance.getId());
    migratedExecutionTree = ExecutionTree.forExecution(processInstance.getId(), processEngine);
  }

  protected void completeTasks(String... taskKeys) {
    List<Task> tasks = taskService.createTaskQuery().taskDefinitionKeyIn(taskKeys).list();
    assertEquals(taskKeys.length, tasks.size());
    for (Task task : tasks) {
      assertNotNull(task);
      taskService.complete(task.getId());
    }
  }

  protected void correlateMessageAndCompleteTasks(String messageName, String... taskKeys) {
    runtimeService.createMessageCorrelation(messageName).correlate();
    completeTasks(taskKeys);
  }

  protected void sendSignalAndCompleteTasks(String signalName, String... taskKeys) {
    runtimeService.signalEventReceived(signalName);
    completeTasks(taskKeys);
  }

  protected void assertMessageEventSubscriptionExists(String messageName) {
    EventSubscription eventSubscription = assertAndGetEventSubscription(messageName);
    assertEquals(MessageEventSubscriptionEntity.EVENT_TYPE, eventSubscription.getEventType());
  }

  protected void assertSignalEventSubscriptionExists(String signalName) {
    EventSubscription eventSubscription = assertAndGetEventSubscription(signalName);
    assertEquals(SignalEventSubscriptionEntity.EVENT_TYPE, eventSubscription.getEventType());
  }

  protected EventSubscription assertAndGetEventSubscription(String eventName) {
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().eventName(eventName).singleResult();
    assertNotNull(eventSubscription);
    return eventSubscription;
  }

  protected void assertTimerJobExsits() {
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).timers().singleResult();
    assertNotNull(job);
  }

  protected void triggerTimerAndCompleteTasks(String... taskKeys) {
    Job job = managementService.createJobQuery().processInstanceId(processInstance.getId()).timers().singleResult();
    assertNotNull(job);
    managementService.executeJob(job.getId());
    completeTasks(taskKeys);
  }

}
