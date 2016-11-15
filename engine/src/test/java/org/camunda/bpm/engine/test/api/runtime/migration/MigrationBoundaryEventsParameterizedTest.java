/*
 * Copyright 2016 camunda services GmbH.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.camunda.bpm.engine.test.api.runtime.migration;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MigrationBoundaryEventsParameterizedTest {

  public static final String AFTER_BOUNDARY_TASK = "afterBoundary";
  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  public static final String NEW_TIMER_DATE = "2018-02-11T12:13:14Z";
  protected static final String BOUNDARY_ID = "boundary";
  protected static final String MIGRATE_MESSAGE_BOUNDARY_EVENT = "MigrateMessageBoundaryEvent";
  protected static final String MIGRATE_SIGNAL_BOUNDARY_EVENT = "MigrateSignalBoundaryEvent";
  protected static final String MIGRATE_TIMER_BOUNDARY_EVENT = "MigrateTimerBoundaryEvent";
  protected static final String MIGRATE_CONDITIONAL_BOUNDARY_EVENT = "MigrateConditionalBoundaryEvent";
  protected static final String USER_TASK_ID = "userTask";
  protected static final String NEW_BOUNDARY_ID = "newBoundary";


  protected static abstract class MigrationBoundaryEventTestConfiguration {
    public abstract BpmnModelInstance getSourceProcess(BpmnModelInstance modelInstance, String activityId);
    public abstract BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId);
    public abstract String getEventName();

    public void assertMigration(MigrationTestRule testHelper, String activityIdBefore, String activityIdAfter) {
      testHelper.assertEventSubscriptionMigrated(activityIdBefore, activityIdAfter, getEventName());
    }

    public abstract void triggerBoundaryEvent(MigrationTestRule testHelper);
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {//message boundary event configuration
      new MigrationBoundaryEventTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).message(MESSAGE_NAME)
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).message("new" + MESSAGE_NAME)
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public String getEventName() {
          return MESSAGE_NAME;
        }

        @Override
        public void triggerBoundaryEvent(MigrationTestRule testHelper) {
          testHelper.correlateMessage(MESSAGE_NAME);
        }

        @Override
        public String toString() {
          return MIGRATE_MESSAGE_BOUNDARY_EVENT;
        }
      }},
      //signal boundary event configuration
      {new MigrationBoundaryEventTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance).activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).signal(SIGNAL_NAME)
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).signal("new" + SIGNAL_NAME)
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public String getEventName() {
          return SIGNAL_NAME;
        }

        @Override
        public void triggerBoundaryEvent(MigrationTestRule testHelper) {
          testHelper.sendSignal(SIGNAL_NAME);
        }

        @Override
        public String toString() {
          return MIGRATE_SIGNAL_BOUNDARY_EVENT;
        }
      }},
      //timer boundary event configuration
      {new MigrationBoundaryEventTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).timerWithDate(TIMER_DATE)
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).timerWithDate(NEW_TIMER_DATE)
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public void assertMigration(MigrationTestRule testHelper, String activityIdBefore, String activityIdAfter) {
          testHelper.assertBoundaryTimerJobMigrated(activityIdBefore, activityIdAfter);
        }

        @Override
        public String getEventName() {
          return null;
        }

        @Override
        public void triggerBoundaryEvent(MigrationTestRule testHelper) {
          testHelper.triggerTimer();
        }

        @Override
        public String toString() {
          return MIGRATE_TIMER_BOUNDARY_EVENT;
        }
      }},
      //conditional boundary event configuration
      {new MigrationBoundaryEventTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID)
            .conditionalEventDefinition()
              .condition("${any=='any'}")
            .conditionalEventDefinitionDone()
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID)
            .conditionalEventDefinition()
            .condition("${true}")
            .conditionalEventDefinitionDone()
            .userTask(AFTER_BOUNDARY_TASK)
            .endEvent()
            .done();
        }

        @Override
        public String getEventName() {
          return null;
        }

        @Override
        public void triggerBoundaryEvent(MigrationTestRule testHelper) {
          testHelper.setAnyVariable(testHelper.snapshotAfterMigration.getProcessInstanceId());
        }

        @Override
        public String toString() {
          return MIGRATE_CONDITIONAL_BOUNDARY_EVENT;
        }
      }}
    });
  }

  @Parameterized.Parameter
  public MigrationBoundaryEventTestConfiguration configuration;


  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  // tests ////////////////////////////////////////////////////////////////////////////////////////////////////////////

  @Test
  public void testMigrateMessageBoundaryEventOnUserTask() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateMessageBoundaryEventOnUserTaskAndCorrelateMessage() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger boundary event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateMessageBoundaryEventAndTriggerByOldMessageName() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance targetProcess = configuration.getTargetProcessWithNewEventName(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, BOUNDARY_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, BOUNDARY_ID);

    // and no event subscription for the new message name exists
    if (!configuration.toString().equals(MIGRATE_TIMER_BOUNDARY_EVENT)) {
      EventSubscription eventSubscription = rule.getRuntimeService().createEventSubscriptionQuery().eventName("new" + configuration.getEventName()).singleResult();
      assertNull(eventSubscription);
      assertEquals(1, rule.getRuntimeService().createEventSubscriptionQuery().count());
    }

    // and it is possible to trigger the event with the old message name and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnScopeUserTask() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.SCOPE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnScopeUserTaskAndTriggerEvent() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.SCOPE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentUserTask() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.PARALLEL_GATEWAY_PROCESS, "userTask1");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentUserTaskAndTriggerEvent() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.PARALLEL_GATEWAY_PROCESS, "userTask1");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentScopeUserTask() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.PARALLEL_SCOPE_TASKS, "userTask1");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentScopeUserTaskAndTriggerEvent() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.PARALLEL_SCOPE_TASKS, "userTask1");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcess() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.SUBPROCESS_PROCESS, "subProcess");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcessAndTriggerEvent() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.SUBPROCESS_PROCESS, "subProcess");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS, "subProcess");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcessWithScopeUserTaskAndTriggerEvent() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS, "subProcess");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToParallelSubProcess() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.PARALLEL_SUBPROCESS_PROCESS, "subProcess1");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask1");
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToParallelSubProcessAndTriggerEvent() {
    // given
    BpmnModelInstance sourceProcess = configuration.getSourceProcess(ProcessModels.PARALLEL_SUBPROCESS_PROCESS, "subProcess1");
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess1")
      .mapActivities(BOUNDARY_ID, NEW_BOUNDARY_ID)
      .mapActivities("subProcess2", "subProcess2")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    configuration.triggerBoundaryEvent(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.completeTask("userTask2");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

}
