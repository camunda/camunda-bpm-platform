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

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.builder.EndEventBuilder;
import org.junit.Test;

public class MigrationAddBoundaryEventsTest extends AbstractMigrationTest {

  @Test
  public void testAddMessageBoundaryEventToUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToScopeUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addMessageBoundaryEventWithUserTask("userTask1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addMessageBoundaryEventWithUserTask("userTask1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToConcurrentScopeUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addMessageBoundaryEventWithUserTask("userTask1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcessAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToSubProcessWithScopeUserTaskAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

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
    assertEventSubscriptionCreated("boundary", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMessageBoundaryEventToParallelSubProcessAndCorrelateMessage() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addMessageBoundaryEventWithUserTask("subProcess1", "boundary", MESSAGE_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToScopeUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addSignalBoundaryEventWithUserTask("userTask1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addSignalBoundaryEventWithUserTask("userTask1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToConcurrentScopeUserTaskAndSendSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addSignalBoundaryEventWithUserTask("userTask1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcessAndCorrelateSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToSubProcessWithScopeUserTaskAndCorrelateSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

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
    assertEventSubscriptionCreated("boundary", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddSignalBoundaryEventToParallelSubProcessAndCorrelateSignal() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addSignalBoundaryEventWithUserTask("subProcess1", "boundary", SIGNAL_NAME, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToScopeUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("userTask1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addTimerDateBoundaryEventWithUserTask("userTask1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToConcurrentScopeUserTaskAndSendTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SCOPE_TASKS)
      .addTimerDateBoundaryEventWithUserTask("userTask1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcessAndCorrelateTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToSubProcessWithScopeUserTaskAndCorrelateTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToParallelSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

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
    assertTimerJobCreated("boundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask1", "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddTimerBoundaryEventToParallelSubProcessAndCorrelateTimerWithDate() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEventWithUserTask("subProcess1", "boundary", TIMER_DATE, AFTER_BOUNDARY_TASK)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(AFTER_BOUNDARY_TASK, "userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddMultipleBoundaryEvents() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addTimerDateBoundaryEvent("subProcess", "timerBoundary", TIMER_DATE)
      .addMessageBoundaryEvent("userTask", "messageBoundary", MESSAGE_NAME)
      .addSignalBoundaryEvent("userTask", "signalBoundary", SIGNAL_NAME)
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    assertEventSubscriptionCreated("messageBoundary", MESSAGE_NAME);
    assertEventSubscriptionCreated("signalBoundary", SIGNAL_NAME);
    assertTimerJobCreated("timerBoundary");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddErrorBoundaryEventToSubProcessAndThrowError() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addErrorBoundaryEventWithUserTask("subProcess", ERROR_CODE, AFTER_BOUNDARY_TASK) // catch error with boundary event
      .getBuilderForElementById("subProcessEnd", EndEventBuilder.class)
      .error(ERROR_CODE) // let the end event of the subprocess throw an error
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    completeTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testAddEscalationBoundaryEventToSubProcessAndThrowEscalation() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .addEscalationBoundaryEventWithUserTask("subProcess", ESCALATION_CODE, AFTER_BOUNDARY_TASK) // catch escalation with boundary event
      .getBuilderForElementById("subProcessEnd", EndEventBuilder.class)
      .escalation(ESCALATION_CODE) // let the end event of the subprocess escalate
      .done()
    );

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    completeTasks(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

}
