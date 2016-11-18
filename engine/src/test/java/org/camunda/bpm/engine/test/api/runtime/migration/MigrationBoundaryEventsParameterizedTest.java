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

import org.camunda.bpm.engine.migration.MigrationInstructionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.EventSubscription;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.BpmnEventTrigger;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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
  public static final String USER_TASK_1_ID = "userTask1";
  public static final String USER_TASK_2_ID = "userTask2";
  public static final String SUB_PROCESS_ID = "subProcess";

  protected static abstract class MigrationBoundaryEventTestConfiguration extends MigrationTestConfiguration {
    public abstract BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId);

    @Override
    public BpmnEventTrigger addEventSubProcess(BpmnModelInstance modelInstance, String parentId) {
      return null;
    }
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {//message boundary event configuration
      new MigrationBoundaryEventTestConfiguration() {
        @Override
        public BpmnEventTrigger addBoundaryEvent(BpmnModelInstance modelInstance, String activityId) {
          final BpmnModelInstance modifiedModel = modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).message(MESSAGE_NAME)
              .userTask(AFTER_BOUNDARY_TASK)
              .endEvent()
            .done();

          return new BpmnEventTrigger() {

            @Override
            public void trigger(ProcessEngineTestRule rule) {
              rule.correlateMessage(MESSAGE_NAME);
            }

            @Override
            public BpmnModelInstance getProcessModel() {
              return modifiedModel;
            }
          };
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
        public String toString() {
          return MIGRATE_MESSAGE_BOUNDARY_EVENT;
        }
      }},
      //signal boundary event configuration
      {new MigrationBoundaryEventTestConfiguration() {

        @Override
        public BpmnEventTrigger addBoundaryEvent(BpmnModelInstance modelInstance, String activityId) {
          final BpmnModelInstance modifiedModel = modify(modelInstance).activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID).signal(SIGNAL_NAME)
              .userTask(AFTER_BOUNDARY_TASK)
              .endEvent()
            .done();

          return new BpmnEventTrigger() {

            @Override
            public void trigger(ProcessEngineTestRule rule) {
              rule.sendSignal(SIGNAL_NAME);
            }

            @Override
            public BpmnModelInstance getProcessModel() {
              return modifiedModel;
            }
          };
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
        public String toString() {
          return MIGRATE_SIGNAL_BOUNDARY_EVENT;
        }
      }},
      //timer boundary event configuration
      {new MigrationBoundaryEventTestConfiguration() {

        @Override
        public BpmnEventTrigger addBoundaryEvent(BpmnModelInstance modelInstance, String activityId) {
          final BpmnModelInstance modifiedModel = modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID)
              .timerWithDate(TIMER_DATE)
              .userTask(AFTER_BOUNDARY_TASK)
              .endEvent()
            .done();

          return new BpmnEventTrigger() {

            @Override
            public void trigger(ProcessEngineTestRule rule) {
              ((MigrationTestRule) rule).triggerTimer();
            }

            @Override
            public BpmnModelInstance getProcessModel() {
              return modifiedModel;
            }
          };
        }

        @Override
        public BpmnModelInstance getTargetProcessWithNewEventName(BpmnModelInstance modelInstance, String activityId) {
          return modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID)
              .timerWithDate(NEW_TIMER_DATE)
              .userTask(AFTER_BOUNDARY_TASK)
              .endEvent()
            .done();
        }

        @Override
        public void assertEventSubscriptionMigration(MigrationTestRule testHelper, String activityIdBefore, String activityIdAfter) {
          testHelper.assertBoundaryTimerJobMigrated(activityIdBefore, activityIdAfter);
        }

        @Override
        public String getEventName() {
          return null;
        }

        @Override
        public String toString() {
          return MIGRATE_TIMER_BOUNDARY_EVENT;
        }
      }},
      //conditional boundary event configuration
      {new MigrationBoundaryEventTestConfiguration() {

        @Override
        public BpmnEventTrigger addBoundaryEvent(BpmnModelInstance modelInstance, String activityId) {
          final BpmnModelInstance modifiedModel = modify(modelInstance)
            .activityBuilder(activityId)
            .boundaryEvent(BOUNDARY_ID)
              .conditionalEventDefinition()
                .condition("${any=='any'}")
              .conditionalEventDefinitionDone()
              .userTask(AFTER_BOUNDARY_TASK)
              .endEvent()
            .done();

          return new BpmnEventTrigger() {

            @Override
            public void trigger(ProcessEngineTestRule rule) {
              rule.setAnyVariable(((MigrationTestRule) rule).snapshotAfterMigration.getProcessInstanceId());
            }

            @Override
            public BpmnModelInstance getProcessModel() {
              return modifiedModel;
            }
          };
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
        public MigrationPlanBuilder createMigrationPlanBuilder(ProcessEngineRule rule, String srcProcDefId, String trgProcDefId, Map<String, String> activities) {
          MigrationPlanBuilder migrationPlanBuilder = createMigrationPlanBuilder(rule, srcProcDefId, trgProcDefId);

          for (String key : activities.keySet()) {
            MigrationInstructionBuilder migrationInstructionBuilder = migrationPlanBuilder.mapActivities(key, activities.get(key));
            if (key.contains(BOUNDARY_ID)) {
              migrationInstructionBuilder.updateEventTrigger();
            }
          }
          return migrationPlanBuilder;
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
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
                                                                           targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateMessageBoundaryEventOnUserTaskAndCorrelateMessage() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();


    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger boundary event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateMessageBoundaryEventAndTriggerByOldMessageName() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = configuration.getTargetProcessWithNewEventName(ProcessModels.ONE_TASK_PROCESS, USER_TASK_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, BOUNDARY_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();


    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, BOUNDARY_ID);

    // and no event subscription for the new message name exists
    if (!configuration.toString().equals(MIGRATE_TIMER_BOUNDARY_EVENT)) {
      EventSubscription eventSubscription = rule.getRuntimeService().createEventSubscriptionQuery().eventName("new" + configuration.getEventName()).singleResult();
      assertNull(eventSubscription);
      assertEquals(1, rule.getRuntimeService().createEventSubscriptionQuery().count());
    }

    // and it is possible to trigger the event with the old message name and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnScopeUserTask() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.SCOPE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnScopeUserTaskAndTriggerEvent() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.SCOPE_TASK_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentUserTask() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.PARALLEL_GATEWAY_PROCESS, USER_TASK_1_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_1_ID, USER_TASK_1_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_2_ID, USER_TASK_2_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_1_ID);
    testHelper.completeTask(USER_TASK_2_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentUserTaskAndTriggerEvent() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.PARALLEL_GATEWAY_PROCESS, USER_TASK_1_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_1_ID, USER_TASK_1_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_2_ID, USER_TASK_2_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.completeTask(USER_TASK_2_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentScopeUserTask() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.PARALLEL_SCOPE_TASKS, USER_TASK_1_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_1_ID, USER_TASK_1_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_2_ID, USER_TASK_2_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_1_ID);
    testHelper.completeTask(USER_TASK_2_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventOnConcurrentScopeUserTaskAndTriggerEvent() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.PARALLEL_SCOPE_TASKS, USER_TASK_1_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_1_ID, USER_TASK_1_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_2_ID, USER_TASK_2_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.completeTask(USER_TASK_2_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcess() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.SUBPROCESS_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(SUB_PROCESS_ID, SUB_PROCESS_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_ID, USER_TASK_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcessAndTriggerEvent() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.SUBPROCESS_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(SUB_PROCESS_ID, SUB_PROCESS_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_ID, USER_TASK_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcessWithScopeUserTask() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(SUB_PROCESS_ID, SUB_PROCESS_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_ID, USER_TASK_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToSubProcessWithScopeUserTaskAndTriggerEvent() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS, USER_TASK_ID);
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(SUB_PROCESS_ID, SUB_PROCESS_ID);
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put(USER_TASK_ID, USER_TASK_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToParallelSubProcess() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.PARALLEL_SUBPROCESS_PROCESS, "subProcess1");
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put("subProcess1", "subProcess1");
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put("subProcess2", "subProcess2");
    activities.put(USER_TASK_1_ID, USER_TASK_1_ID);
    activities.put(USER_TASK_2_ID, USER_TASK_2_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    configuration.assertEventSubscriptionMigration(testHelper, BOUNDARY_ID, NEW_BOUNDARY_ID);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_1_ID);
    testHelper.completeTask(USER_TASK_2_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateBoundaryEventToParallelSubProcessAndTriggerEvent() {
    // given
    BpmnEventTrigger bpmnEventTrigger = configuration.addBoundaryEvent(ProcessModels.PARALLEL_SUBPROCESS_PROCESS, "subProcess1");
    BpmnModelInstance sourceProcess = bpmnEventTrigger.getProcessModel();
    BpmnModelInstance targetProcess = modify(sourceProcess).changeElementId(BOUNDARY_ID, NEW_BOUNDARY_ID);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(BOUNDARY_ID, NEW_BOUNDARY_ID);
    activities.put("subProcess1", "subProcess1");
    activities.put("subProcess2", "subProcess2");
    activities.put(USER_TASK_1_ID, USER_TASK_1_ID);
    activities.put(USER_TASK_2_ID, USER_TASK_2_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger the event and successfully complete the migrated instance
    bpmnEventTrigger.trigger(testHelper);
    testHelper.completeTask(AFTER_BOUNDARY_TASK);
    testHelper.completeTask(USER_TASK_2_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

}
