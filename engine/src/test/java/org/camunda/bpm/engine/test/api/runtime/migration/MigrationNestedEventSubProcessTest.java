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
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
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
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MigrationNestedEventSubProcessTest {

  protected static final String USER_TASK_ID = "userTask";
  protected static final String EVENT_SUB_PROCESS_START_ID = "eventSubProcessStart";
  protected static final String EVENT_SUB_PROCESS_TASK_ID = "eventSubProcessTask";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";

  protected static abstract class MigrationEventSubProcessTestConfiguration {
    public abstract BpmnModelInstance getSourceProcess();

    public abstract String getEventName();

    public void assertMigration(MigrationTestRule testHelper) {
      testHelper.assertEventSubscriptionRemoved(EVENT_SUB_PROCESS_START_ID, getEventName());
      testHelper.assertEventSubscriptionCreated(EVENT_SUB_PROCESS_START_ID, getEventName());
    }

    public abstract void triggerEventSubProcess(MigrationTestRule testHelper);
  }


  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {//message event sub process configuration
        new MigrationEventSubProcessTestConfiguration() {
          @Override
          public BpmnModelInstance getSourceProcess() {
            return EventSubProcessModels.NESTED_EVENT_SUB_PROCESS_PROCESS;
          }

          @Override
          public String getEventName() {
            return EventSubProcessModels.MESSAGE_NAME;
          }

          @Override
          public void triggerEventSubProcess(MigrationTestRule testHelper) {
            testHelper.correlateMessage(EventSubProcessModels.MESSAGE_NAME);
          }

          @Override
          public String toString() {
            return "MigrateMessageEventSubProcess";
          }
        }},
      //signal event sub process configuration
      {new MigrationEventSubProcessTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess() {
          return modify(ProcessModels.SUBPROCESS_PROCESS)
            .addSubProcessTo(EventSubProcessModels.SUB_PROCESS_ID)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent(EVENT_SUB_PROCESS_START_ID).signal(EventSubProcessModels.SIGNAL_NAME)
            .userTask(EVENT_SUB_PROCESS_TASK_ID)
            .endEvent()
            .subProcessDone()
            .done();
        }

        @Override
        public String getEventName() {
          return EventSubProcessModels.SIGNAL_NAME;
        }

        @Override
        public void triggerEventSubProcess(MigrationTestRule testHelper) {
          testHelper.sendSignal(EventSubProcessModels.SIGNAL_NAME);
        }

        @Override
        public String toString() {
          return "MigrateSignalEventSubProcess";
        }
      }},
      //timer event sub process configuration
      {new MigrationEventSubProcessTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess() {
          return modify(ProcessModels.SUBPROCESS_PROCESS)
            .addSubProcessTo(EventSubProcessModels.SUB_PROCESS_ID)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent(EVENT_SUB_PROCESS_START_ID).timerWithDate(TIMER_DATE)
            .userTask(EVENT_SUB_PROCESS_TASK_ID)
            .endEvent()
            .subProcessDone()
            .done();
        }

        @Override
        public void assertMigration(MigrationTestRule testHelper) {
          testHelper.assertEventSubProcessTimerJobRemoved(EVENT_SUB_PROCESS_START_ID);
          testHelper.assertEventSubProcessTimerJobCreated(EVENT_SUB_PROCESS_START_ID);
        }

        @Override
        public String getEventName() {
          return null;
        }

        @Override
        public void triggerEventSubProcess(MigrationTestRule testHelper) {
          testHelper.triggerTimer();
        }

        @Override
        public String toString() {
          return "MigrateTimerEventSubProcess";
        }
      }},
      //conditional event sub process configuration
      {new MigrationEventSubProcessTestConfiguration() {
        @Override
        public BpmnModelInstance getSourceProcess() {
          return modify(ProcessModels.SUBPROCESS_PROCESS)
            .addSubProcessTo(EventSubProcessModels.SUB_PROCESS_ID)
            .triggerByEvent()
            .embeddedSubProcess()
            .startEvent(EVENT_SUB_PROCESS_START_ID)
            .condition(EventSubProcessModels.VAR_CONDITION)
            .userTask(EVENT_SUB_PROCESS_TASK_ID)
            .endEvent()
            .subProcessDone()
            .done();
        }

        @Override
        public String getEventName() {
          return null;
        }

        @Override
        public void triggerEventSubProcess(MigrationTestRule testHelper) {
          testHelper.setAnyVariable(testHelper.snapshotAfterMigration.getProcessInstanceId());
        }

        @Override
        public String toString() {
          return "MigrateConditionalEventSubProcess";
        }
      }}
    });
  }

  @Parameterized.Parameter
  public MigrationEventSubProcessTestConfiguration configuration;

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMapUserTaskSiblingOfEventSubProcess() {

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(configuration.getSourceProcess());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(configuration.getSourceProcess());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child(USER_TASK_ID).scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .beginScope(EventSubProcessModels.SUB_PROCESS_ID)
        .activity(USER_TASK_ID, testHelper.getSingleActivityInstanceBeforeMigration(USER_TASK_ID).getId())
        .done());

    configuration.assertMigration(testHelper);

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask(USER_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMapUserTaskSiblingOfEventSubProcessAndTriggerEvent() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(configuration.getSourceProcess());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(configuration.getSourceProcess());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(USER_TASK_ID, USER_TASK_ID)
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then it is possible to trigger event sub process and successfully complete the migrated instance
    configuration.triggerEventSubProcess(testHelper);
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }
}
