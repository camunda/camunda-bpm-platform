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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.camunda.bpm.engine.impl.migration.validation.instruction.ConditionalEventUpdateEventTriggerValidator.MIGRATION_CONDITIONAL_VALIDATION_ERROR_MSG;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.FailingDelegate;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ClockTestUtil;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

public class MigrationBoundaryEventsTest {

  public static final String AFTER_BOUNDARY_TASK = "afterBoundary";
  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  protected static final String FALSE_CONDITION = "${false}";
  protected static final String VAR_CONDITION = "${any=='any'}";
  protected static final String BOUNDARY_ID = "boundary";
  protected static final String USER_TASK_ID = "userTask";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateMultipleBoundaryEvents() {
    // given
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("timerBoundary1").timerWithDate(TIMER_DATE)
      .moveToActivity("subProcess")
        .boundaryEvent("messageBoundary1").message(MESSAGE_NAME)
      .moveToActivity("subProcess")
        .boundaryEvent("signalBoundary1").signal(SIGNAL_NAME)
      .moveToActivity("subProcess")
        .boundaryEvent("conditionalBoundary1").condition(VAR_CONDITION)
      .moveToActivity(USER_TASK_ID)
        .boundaryEvent("timerBoundary2").timerWithDate(TIMER_DATE)
      .moveToActivity(USER_TASK_ID)
        .boundaryEvent("messageBoundary2").message(MESSAGE_NAME)
      .moveToActivity(USER_TASK_ID)
        .boundaryEvent("signalBoundary2").signal(SIGNAL_NAME)
      .moveToActivity(USER_TASK_ID)
      .boundaryEvent("conditionalBoundary2").condition(VAR_CONDITION)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("timerBoundary1", "timerBoundary1")
      .mapActivities("signalBoundary1", "signalBoundary1")
      .mapActivities("conditionalBoundary1", "conditionalBoundary1")
      .updateEventTrigger()
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities("messageBoundary2", "messageBoundary2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("messageBoundary1", MESSAGE_NAME);
    testHelper.assertEventSubscriptionRemoved("signalBoundary2", SIGNAL_NAME);
    testHelper.assertEventSubscriptionRemoved("conditionalBoundary2", null);
    testHelper.assertEventSubscriptionMigrated("signalBoundary1", "signalBoundary1", SIGNAL_NAME);
    testHelper.assertEventSubscriptionMigrated("messageBoundary2", "messageBoundary2", MESSAGE_NAME);
    testHelper.assertEventSubscriptionMigrated("conditionalBoundary1", "conditionalBoundary1", null);
    testHelper.assertEventSubscriptionCreated("messageBoundary1", MESSAGE_NAME);
    testHelper.assertEventSubscriptionCreated("signalBoundary2", SIGNAL_NAME);
    testHelper.assertEventSubscriptionCreated("conditionalBoundary2", null);
    testHelper.assertBoundaryTimerJobRemoved("timerBoundary2");
    testHelper.assertBoundaryTimerJobMigrated("timerBoundary1", "timerBoundary1");
    testHelper.assertBoundaryTimerJobCreated("timerBoundary2");

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventAndEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
        .triggerByEvent()
        .embeddedSubProcess()
          .startEvent("eventStart").message(MESSAGE_NAME)
          .endEvent()
        .subProcessDone()
      .moveToActivity(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).signal(SIGNAL_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(BOUNDARY_ID, BOUNDARY_ID)
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionRemoved("eventStart", MESSAGE_NAME);
    testHelper.assertEventSubscriptionMigrated(BOUNDARY_ID, BOUNDARY_ID, SIGNAL_NAME);
    testHelper.assertEventSubscriptionCreated("eventStart", MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateIncidentForJob() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .userTaskBuilder(USER_TASK_ID)
      .boundaryEvent(BOUNDARY_ID).timerWithDate(TIMER_DATE)
      .serviceTask("failingTask").camundaClass(FailingDelegate.class.getName())
      .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(USER_TASK_ID, "newUserTask")
      .changeElementId(BOUNDARY_ID, "newBoundary");

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // a timer job exists
    Job jobBeforeMigration = rule.getManagementService().createJobQuery().singleResult();
    assertNotNull(jobBeforeMigration);

    // if the timer job is triggered the failing delegate fails and an incident is created
    executeJob(jobBeforeMigration);
    Incident incidentBeforeMigration = rule.getRuntimeService().createIncidentQuery().singleResult();

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, "newUserTask")
      .mapActivities(BOUNDARY_ID, "newBoundary")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then the job and incident still exists
    Job jobAfterMigration = rule.getManagementService().createJobQuery().jobId(jobBeforeMigration.getId()).singleResult();
    assertNotNull(jobAfterMigration);
    Incident incidentAfterMigration = rule.getRuntimeService().createIncidentQuery().singleResult();
    assertNotNull(incidentAfterMigration);

    // and it is still the same incident
    assertEquals(incidentBeforeMigration.getId(), incidentAfterMigration.getId());
    assertEquals(jobAfterMigration.getId(), incidentAfterMigration.getConfiguration());

    // and the activity, process definition and job definition references were updated
    assertEquals("newBoundary", incidentAfterMigration.getActivityId());
    assertEquals(targetProcessDefinition.getId(), incidentAfterMigration.getProcessDefinitionId());
    assertEquals(jobAfterMigration.getJobDefinitionId(), incidentAfterMigration.getJobDefinitionId());
  }

  @Test
  public void testUpdateEventMessage() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).message(MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).message("new" + MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, BOUNDARY_ID).updateEventTrigger()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated(
      BOUNDARY_ID, MESSAGE_NAME,
      BOUNDARY_ID, "new" + MESSAGE_NAME);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().correlateMessage("new" + MESSAGE_NAME);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventSignal() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).signal(SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).signal("new" + SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, BOUNDARY_ID).updateEventTrigger()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated(
      BOUNDARY_ID, SIGNAL_NAME,
      BOUNDARY_ID, "new" + SIGNAL_NAME);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().signalEventReceived("new" + SIGNAL_NAME);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventTimer() {
    // given
    ClockTestUtil.setClockToDateWithoutMilliseconds();

    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).timerWithDate(TIMER_DATE)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).timerWithDuration("PT50M")
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, BOUNDARY_ID).updateEventTrigger()
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Date newDueDate = new DateTime(ClockUtil.getCurrentTime()).plusMinutes(50).toDate();
    testHelper.assertJobMigrated(
        testHelper.snapshotBeforeMigration.getJobs().get(0),
      BOUNDARY_ID,
        newDueDate);

    // and it is possible to successfully complete the migrated instance
    Job jobAfterMigration = testHelper.snapshotAfterMigration.getJobs().get(0);
    rule.getManagementService().executeJob(jobAfterMigration.getId());

    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
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

  @Test
  public void testUpdateEventSignalNameWithExpression() {
    // given
    String signalNameWithExpression = "new" + SIGNAL_NAME + "-${var}";
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).signal(signalNameWithExpression)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
        .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .build();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "foo");

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan, variables);

    // the signal event subscription's event name has changed
    String resolvedSignalName = "new" + SIGNAL_NAME + "-foo";
    testHelper.assertEventSubscriptionCreated(BOUNDARY_ID, resolvedSignalName);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().signalEventReceived(resolvedSignalName);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testUpdateEventMessageNameWithExpression() {
    // given
    String messageNameWithExpression = "new" + MESSAGE_NAME + "-${var}";
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).message(messageNameWithExpression)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
        .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .build();

    HashMap<String, Object> variables = new HashMap<String, Object>();
    variables.put("var", "foo");

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan, variables);

    // the message event subscription's event name has changed
    String resolvedMessageName = "new" + MESSAGE_NAME + "-foo";
    testHelper.assertEventSubscriptionCreated(BOUNDARY_ID, resolvedMessageName);

    // and it is possible to successfully complete the migrated instance
    rule.getRuntimeService().correlateMessage(resolvedMessageName);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }


  @Test
  public void testUpdateConditionalEventExpression() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
      .boundaryEvent(BOUNDARY_ID).condition(FALSE_CONDITION)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
      .boundaryEvent(BOUNDARY_ID).condition(VAR_CONDITION)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, BOUNDARY_ID).updateEventTrigger()
      .build();

    // when process is migrated without update event trigger
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then condition is migrated and has new condition expr
    testHelper.assertEventSubscriptionMigrated(BOUNDARY_ID, BOUNDARY_ID, null);

    // and it is possible to successfully complete the migrated instance
    testHelper.setAnyVariable(testHelper.snapshotAfterMigration.getProcessInstanceId());
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateSignalBoundaryEventKeepTrigger() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder(USER_TASK_ID)
          .boundaryEvent(BOUNDARY_ID).signal(SIGNAL_NAME)
          .userTask(AFTER_BOUNDARY_TASK)
          .endEvent()
        .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).signal("new" + SIGNAL_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, BOUNDARY_ID);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(BOUNDARY_ID, BOUNDARY_ID)
        .build();


    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated(BOUNDARY_ID, BOUNDARY_ID, SIGNAL_NAME);

    // and no event subscription for the new message name exists
    EventSubscription eventSubscription = rule.getRuntimeService().createEventSubscriptionQuery().eventName("new" + SIGNAL_NAME).singleResult();
    assertNull(eventSubscription);
    assertEquals(1, rule.getRuntimeService().createEventSubscriptionQuery().count());

    // and it is possible to trigger the event with the old message name and successfully complete the migrated instance
    rule.getProcessEngine().getRuntimeService().signalEventReceived(SIGNAL_NAME);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateMessageBoundaryEventKeepTrigger() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder(USER_TASK_ID)
          .boundaryEvent(BOUNDARY_ID).message(MESSAGE_NAME)
          .userTask(AFTER_BOUNDARY_TASK)
          .endEvent()
        .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).message("new" + MESSAGE_NAME)
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, BOUNDARY_ID);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(BOUNDARY_ID, BOUNDARY_ID)
        .build();


    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertEventSubscriptionMigrated(BOUNDARY_ID, BOUNDARY_ID, MESSAGE_NAME);

    // and no event subscription for the new message name exists
    EventSubscription eventSubscription = rule.getRuntimeService().createEventSubscriptionQuery().eventName("new" + MESSAGE_NAME).singleResult();
    assertNull(eventSubscription);
    assertEquals(1, rule.getRuntimeService().createEventSubscriptionQuery().count());

    // and it is possible to trigger the event with the old message name and successfully complete the migrated instance
    rule.getProcessEngine().getRuntimeService().correlateMessage(MESSAGE_NAME);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }


  @Test
  public void testMigrateTimerBoundaryEventKeepTrigger() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder(USER_TASK_ID)
          .boundaryEvent(BOUNDARY_ID).timerWithDuration("PT5S")
          .userTask(AFTER_BOUNDARY_TASK)
          .endEvent()
        .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
        .boundaryEvent(BOUNDARY_ID).timerWithDuration("PT10M")
        .userTask(AFTER_BOUNDARY_TASK)
        .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, BOUNDARY_ID);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(BOUNDARY_ID, BOUNDARY_ID)
        .build();


    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertJobMigrated(BOUNDARY_ID, BOUNDARY_ID, TimerExecuteNestedActivityJobHandler.TYPE);

    // and it is possible to trigger the event and successfully complete the migrated instance
    ManagementService managementService = rule.getManagementService();
    Job job = managementService.createJobQuery().singleResult();

    managementService.executeJob(job.getId());
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateConditionalBoundaryEventKeepTrigger() {
    // given
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder(USER_TASK_ID)
      .boundaryEvent(BOUNDARY_ID).condition(FALSE_CONDITION)
      .userTask(AFTER_BOUNDARY_TASK)
      .endEvent()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);

    // when conditional boundary event is migrated without update event trigger
    // then
    assertThatThrownBy(() -> rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities(USER_TASK_ID, USER_TASK_ID)
        .mapActivities(BOUNDARY_ID, BOUNDARY_ID)
        .build())
      .isInstanceOf(MigrationPlanValidationException.class)
      .hasMessageContaining(MIGRATION_CONDITIONAL_VALIDATION_ERROR_MSG);

  }
}
