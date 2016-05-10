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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationRemoveBoundaryEventsTest {

  public static final String AFTER_BOUNDARY_TASK = "afterBoundary";
  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testRemoveMessageBoundaryEventFromUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .activityBuilder("subProcess1")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromUserTaskInSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .activityBuilder("subProcess1")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromUserTaskInSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .activityBuilder("subProcess1")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromUserTaskInSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertBoundaryTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMultipleBoundaryEvents() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("timerBoundary").timerWithDate(TIMER_DATE)
      .moveToActivity("userTask")
        .boundaryEvent("messageBoundary").message(MESSAGE_NAME)
      .moveToActivity("userTask")
        .boundaryEvent("signalBoundary").signal(SIGNAL_NAME)
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("messageBoundary", MESSAGE_NAME);
    testHelper.assertEventSubscriptionRemoved("signalBoundary", SIGNAL_NAME);
    testHelper.assertBoundaryTimerJobRemoved("timerBoundary");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveErrorBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .boundaryEvent().error(ERROR_CODE)
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveEscalationBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .boundaryEvent().escalation(ESCALATION_CODE)
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveIncidentForJob() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .userTaskBuilder("userTask")
      .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
      .serviceTask("failingTask").camundaClass("org.camunda.bpm.engine.test.api.runtime.FailingDelegate")
      .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId("userTask", "newUserTask")
      .changeElementId("boundary", "newBoundary");

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // a timer job exists
    Job jobBeforeMigration = rule.getManagementService().createJobQuery().singleResult();
    assertNotNull(jobBeforeMigration);

    // if the timer job is triggered the failing delegate fails and an incident is created
    executeJob(jobBeforeMigration);
    Incident incidentBeforeMigration = rule.getRuntimeService().createIncidentQuery().singleResult();
    assertEquals("boundary", incidentBeforeMigration.getActivityId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "newUserTask")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then the incident was removed
    Job jobAfterMigration = rule.getManagementService().createJobQuery().jobId(jobBeforeMigration.getId()).singleResult();
    assertNull(jobAfterMigration);

    assertEquals(0, rule.getRuntimeService().createIncidentQuery().count());
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
