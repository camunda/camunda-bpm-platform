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

import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

import java.util.Date;
import java.util.HashMap;

import static org.camunda.bpm.engine.impl.migration.validation.instruction.ConditionalEventUpdateEventTriggerValidator.MIGRATION_CONDITIONAL_VALIDATION_ERROR_MSG;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.CONDITIONAL_EVENT_SUBPROCESS_PROCESS;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MigrationEventSubProcessTest {

  public static final String SIGNAL_NAME = "Signal";
  protected static final String EVENT_SUB_PROCESS_START_ID = "eventSubProcessStart";
  protected static final String EVENT_SUB_PROCESS_TASK_ID = "eventSubProcessTask";
  protected static final String USER_TASK_ID = "userTask";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public ExpectedException exceptionRule = ExpectedException.none();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateActiveEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity(EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcess", "eventSubProcess")
        .mapActivities(EVENT_SUB_PROCESS_TASK_ID, EVENT_SUB_PROCESS_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
    .hasProcessDefinitionId(targetProcessDefinition.getId())
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(EVENT_SUB_PROCESS_TASK_ID).scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("eventSubProcess"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("eventSubProcess", testHelper.getSingleActivityInstanceBeforeMigration("eventSubProcess").getId())
          .activity(EVENT_SUB_PROCESS_TASK_ID, testHelper.getSingleActivityInstanceBeforeMigration(EVENT_SUB_PROCESS_TASK_ID).getId())
        .done());

    testHelper.assertEventSubscriptionRemoved(EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);
    testHelper.assertEventSubscriptionCreated(EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);

    // and it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateActiveEventSubProcessToEmbeddedSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity(EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcess", "subProcess")
        .mapActivities(EVENT_SUB_PROCESS_TASK_ID, USER_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
    .hasProcessDefinitionId(targetProcessDefinition.getId())
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(USER_TASK_ID).scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("eventSubProcess"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("subProcess", testHelper.getSingleActivityInstanceBeforeMigration("eventSubProcess").getId())
          .activity(USER_TASK_ID, testHelper.getSingleActivityInstanceBeforeMigration(EVENT_SUB_PROCESS_TASK_ID).getId())
        .done());

    testHelper.assertEventSubscriptionRemoved(EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);
    Assert.assertEquals(0, testHelper.snapshotAfterMigration.getEventSubscriptions().size());

    // and it is possible to complete the process instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateActiveEmbeddedSubProcessToEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "eventSubProcess")
        .mapActivities(USER_TASK_ID, EVENT_SUB_PROCESS_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
    .hasProcessDefinitionId(targetProcessDefinition.getId())
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(EVENT_SUB_PROCESS_TASK_ID).scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("subProcess"))
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("eventSubProcess", testHelper.getSingleActivityInstanceBeforeMigration("subProcess").getId())
          .activity(EVENT_SUB_PROCESS_TASK_ID, testHelper.getSingleActivityInstanceBeforeMigration(USER_TASK_ID).getId())
        .done());

    testHelper.assertEventSubscriptionCreated(EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);

    // and it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateActiveErrorEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.ERROR_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.ERROR_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity(EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcess", "eventSubProcess")
        .mapActivities(EVENT_SUB_PROCESS_TASK_ID, EVENT_SUB_PROCESS_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateActiveCompensationEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.COMPENSATE_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.COMPENSATE_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity(EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcess", "eventSubProcess")
        .mapActivities(EVENT_SUB_PROCESS_TASK_ID, EVENT_SUB_PROCESS_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateActiveEscalationEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.ESCALATION_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.ESCALATION_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity(EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcess", "eventSubProcess")
        .mapActivities(EVENT_SUB_PROCESS_TASK_ID, EVENT_SUB_PROCESS_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateTaskAddEventSubProcess() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, EVENT_SUB_PROCESS_TASK_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
    .hasProcessDefinitionId(targetProcessDefinition.getId())
    .matches(
      describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
        .child(EVENT_SUB_PROCESS_TASK_ID).scope()
        .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope("eventSubProcess")
          .activity(EVENT_SUB_PROCESS_TASK_ID, testHelper.getSingleActivityInstanceBeforeMigration(USER_TASK_ID).getId())
        .done());

    testHelper.assertEventSubscriptionCreated(EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);

    // and it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubprocessMessageKeepTrigger() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);

    // and it is possible to trigger the event subprocess
    rule.getRuntimeService().correlateMessage(EventSubProcessModels.MESSAGE_NAME);
    Assert.assertEquals(1, rule.getTaskService().createTaskQuery().count());

    // and complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubprocessTimerKeepTrigger() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertJobMigrated(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID, TimerStartEventSubprocessJobHandler.TYPE);

    // and it is possible to trigger the event subprocess
    Job timerJob = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(timerJob.getId());
    Assert.assertEquals(1, rule.getTaskService().createTaskQuery().count());

    // and complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateEventSubprocessSignalKeepTrigger() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.SIGNAL_NAME);

    // and it is possible to trigger the event subprocess
    rule.getRuntimeService().signalEventReceived(EventSubProcessModels.SIGNAL_NAME);
    Assert.assertEquals(1, rule.getTaskService().createTaskQuery().count());

    // and complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateConditionalBoundaryEventKeepTrigger() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CONDITIONAL_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CONDITIONAL_EVENT_SUBPROCESS_PROCESS);

    // expected migration validation exception
    exceptionRule.expect(MigrationPlanValidationException.class);
    exceptionRule.expectMessage(MIGRATION_CONDITIONAL_VALIDATION_ERROR_MSG);

    // when conditional event sub process is migrated without update event trigger
    rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
      .build();
  }


  @Test
  public void testMigrateEventSubprocessChangeStartEventType() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);

    try {
      // when
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
        .build();
      Assert.fail("exception expected");
    } catch (MigrationPlanValidationException e) {
      // then
      assertThat(e.getValidationReport())
      .hasInstructionFailures(EVENT_SUB_PROCESS_START_ID,
        "Events are not of the same type (signalStartEvent != startTimerEvent)"
      );
    }
  }

  @Test
  public void testMigrateEventSubprocessTimerIncident() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Job timerTriggerJob = rule.getManagementService().createJobQuery().singleResult();
    // create an incident
    rule.getManagementService().setJobRetries(timerTriggerJob.getId(), 0);
    Incident incidentBeforeMigration = rule.getRuntimeService().createIncidentQuery().singleResult();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    Incident incidentAfterMigration = rule.getRuntimeService().createIncidentQuery().singleResult();
    assertNotNull(incidentAfterMigration);

    assertEquals(incidentBeforeMigration.getId(), incidentAfterMigration.getId());
    assertEquals(timerTriggerJob.getId(), incidentAfterMigration.getConfiguration());

    assertEquals(EVENT_SUB_PROCESS_START_ID, incidentAfterMigration.getActivityId());
    assertEquals(targetProcessDefinition.getId(), incidentAfterMigration.getProcessDefinitionId());

    // and it is possible to complete the process
    rule.getManagementService().executeJob(timerTriggerJob.getId());
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testMigrateNonInterruptingEventSubprocessMessageTrigger() {
    BpmnModelInstance nonInterruptingModel = modify(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS)
      .startEventBuilder(EVENT_SUB_PROCESS_START_ID)
      .interrupting(false)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(nonInterruptingModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(nonInterruptingModel);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID)
        .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME);

    // and it is possible to trigger the event subprocess
    rule.getRuntimeService().correlateMessage(EventSubProcessModels.MESSAGE_NAME);
    Assert.assertEquals(2, rule.getTaskService().createTaskQuery().count());

    // and complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testUpdateEventMessage() {
    // given
    BpmnModelInstance sourceProcess = EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS)
      .renameMessage(EventSubProcessModels.MESSAGE_NAME, "new" + EventSubProcessModels.MESSAGE_NAME);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID).updateEventTrigger()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated(
      EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.MESSAGE_NAME,
      EVENT_SUB_PROCESS_START_ID, "new" + EventSubProcessModels.MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().correlateMessage("new" + EventSubProcessModels.MESSAGE_NAME);
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventSignal() {
    // given
    BpmnModelInstance sourceProcess = EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS)
      .renameSignal(EventSubProcessModels.SIGNAL_NAME, "new" + EventSubProcessModels.SIGNAL_NAME);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID).updateEventTrigger()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated(
      EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.SIGNAL_NAME,
      EVENT_SUB_PROCESS_START_ID, "new" + EventSubProcessModels.SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().signalEventReceived("new" + EventSubProcessModels.SIGNAL_NAME);
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventTimer() {
    // given
    ClockTestUtil.setClockToDateWithoutMilliseconds();

    BpmnModelInstance sourceProcess = EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS)
      .removeChildren(EVENT_SUB_PROCESS_START_ID)
      .startEventBuilder(EVENT_SUB_PROCESS_START_ID)
        .timerWithDuration("PT50M")
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID).updateEventTrigger()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Date newDueDate = new DateTime(ClockUtil.getCurrentTime()).plusMinutes(50).toDate();
    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
      EVENT_SUB_PROCESS_START_ID,
        newDueDate);

    // and it is possible to successfully complete the migrated instance
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventMessageWithExpression() {

    // given
    String newMessageNameWithExpression = "new" + EventSubProcessModels.MESSAGE_NAME + "-${var}";
    BpmnModelInstance sourceProcess = EventSubProcessModels.MESSAGE_INTERMEDIATE_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(EventSubProcessModels.MESSAGE_INTERMEDIATE_EVENT_SUBPROCESS_PROCESS)
        .renameMessage(EventSubProcessModels.MESSAGE_NAME, newMessageNameWithExpression);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities("eventSubProcess", "eventSubProcess")
        .mapActivities("catchMessage", "catchMessage").updateEventTrigger()
        .build();
    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "foo");

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan, variables);

    // then
    String resolvedMessageName = "new" + EventSubProcessModels.MESSAGE_NAME + "-foo";
    testHelper.assertEventSubscriptionMigrated(
        "catchMessage", EventSubProcessModels.MESSAGE_NAME,
        "catchMessage", resolvedMessageName);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().correlateMessage(resolvedMessageName);
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventSignalWithExpression() {
    // given
    String newSignalNameWithExpression = "new" + EventSubProcessModels.MESSAGE_NAME + "-${var}";
    BpmnModelInstance sourceProcess = EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS)
        .renameSignal(EventSubProcessModels.SIGNAL_NAME, newSignalNameWithExpression);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID).updateEventTrigger()
        .build();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "foo");

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan, variables);

    // then
    String resolvedsignalName = "new" + EventSubProcessModels.MESSAGE_NAME + "-foo";
    testHelper.assertEventSubscriptionMigrated(
      EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.SIGNAL_NAME,
      EVENT_SUB_PROCESS_START_ID, resolvedsignalName);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().signalEventReceived(resolvedsignalName);
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateConditionalEventExpression() {
    // given
    BpmnModelInstance sourceProcess = EventSubProcessModels.FALSE_CONDITIONAL_EVENT_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(CONDITIONAL_EVENT_SUBPROCESS_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);


    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID).updateEventTrigger()
      .build();

    // when process is migrated without update event trigger
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then condition is migrated and has new condition expr
    testHelper.assertEventSubscriptionMigrated(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID, null);

    // and it is possible to successfully complete the migrated instance
    testHelper.setAnyVariable(testHelper.snapshotAfterMigration.getProcessInstanceId());
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }
}
