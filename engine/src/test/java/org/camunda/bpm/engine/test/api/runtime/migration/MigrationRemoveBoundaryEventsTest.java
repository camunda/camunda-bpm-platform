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
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class MigrationRemoveBoundaryEventsTest extends AbstractMigrationTest {

  @Test
  public void testRemoveMessageBoundaryEventFromUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addMessageBoundaryEventWithUserTask("userTask1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMessageBoundaryEventFromUserTaskInSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addSignalBoundaryEventWithUserTask("userTask1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveSignalBoundaryEventFromUserTaskInSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addTimerDateBoundaryEventWithUserTask("userTask1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask1");
    completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveTimerBoundaryEventFromUserTaskInSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobRemoved("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveMultipleBoundaryEvents() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEvent("subProcess", "timerBoundary", TIMER_DATE)
      .addMessageBoundaryEvent("userTask", "messageBoundary", MESSAGE_NAME)
      .addSignalBoundaryEvent("userTask", "signalBoundary", SIGNAL_NAME)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionRemoved("messageBoundary", MESSAGE_NAME);
    assertEventSubscriptionRemoved("signalBoundary", SIGNAL_NAME);
    assertTimerJobRemoved("timerBoundary");

    // and it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveErrorBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addErrorBoundaryEvent("subProcess", ERROR_CODE)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveEscalationBoundaryEventFromSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addEscalationBoundaryEvent("subProcess", ESCALATION_CODE)
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to successfully complete the migrated instance
    completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateIncidentForJob() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .builderForUserTask("userTask")
      .boundaryEvent("boundary").timerWithDate(TIMER_DATE)
      .serviceTask("failingTask").camundaClass("org.camunda.bpm.engine.test.api.runtime.FailingDelegate")
      .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId("userTask", "newUserTask")
      .changeElementId("boundary", "newBoundary");

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    // a timer job exists
    Job jobBeforeMigration = rule.getManagementService().createJobQuery().singleResult();
    assertNotNull(jobBeforeMigration);

    // if the timer job is triggered the failing delegate fails and an incident is created
    executeJob(jobBeforeMigration);
    Incident incidentBeforeMigration = runtimeService.createIncidentQuery().singleResult();
    assertEquals("userTask", incidentBeforeMigration.getActivityId());

    MigrationPlan migrationPlan = runtimeService
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
