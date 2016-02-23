/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Test;

public class MigrationEventSubProcessTest extends AbstractMigrationTest {

  public static final String IN_EVENT_SUB_PROCESS_TASK = "inEventSubProcessTask";

  @Test
  public void testMapUserTaskSiblingOfMessageEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventStart").message(MESSAGE_NAME)
      .userTask(IN_EVENT_SUB_PROCESS_TASK)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess")
          .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    assertEventSubscriptionRemoved("eventStart", MESSAGE_NAME);
    assertEventSubscriptionCreated("eventStart", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMapUserTaskSiblingOfMessageEventSubProcessAndTriggerMessage() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventStart").message(MESSAGE_NAME)
      .userTask(IN_EVENT_SUB_PROCESS_TASK)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to correlate the message and successfully complete the migrated instance
    correlateMessageAndCompleteTasks(MESSAGE_NAME, IN_EVENT_SUB_PROCESS_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMapUserTaskSiblingOfSignalEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventStart").signal(SIGNAL_NAME)
      .userTask(IN_EVENT_SUB_PROCESS_TASK)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess")
        .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    assertEventSubscriptionRemoved("eventStart", SIGNAL_NAME);
    assertEventSubscriptionCreated("eventStart", SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMapUserTaskSiblingOfSignalEventSubProcessAndSendSignal() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventStart").signal(SIGNAL_NAME)
      .userTask(IN_EVENT_SUB_PROCESS_TASK)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to send the signal and successfully complete the migrated instance
    sendSignalAndCompleteTasks(SIGNAL_NAME, IN_EVENT_SUB_PROCESS_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMapUserTaskSiblingOfTimerEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventStart").timerWithDate(TIMER_DATE)
      .userTask(IN_EVENT_SUB_PROCESS_TASK)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess")
        .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    assertTimerJobRemoved("eventStart");
    assertTimerJobCreated("eventStart");

    // and it is possible to successfully complete the migrated instance
    completeTasks("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMapUserTaskSiblingOfTimerEventSubProcessAndTriggerTimer() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent("eventStart").timerWithDate(TIMER_DATE)
      .userTask(IN_EVENT_SUB_PROCESS_TASK)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the timer and successfully complete the migrated instance
    triggerTimerAndCompleteTasks(IN_EVENT_SUB_PROCESS_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

}
