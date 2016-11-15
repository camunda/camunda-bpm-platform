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

import org.camunda.bpm.engine.impl.jobexecutor.TimerStartEventSubprocessJobHandler;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MigrateEventSubProcessAndTriggerTest {


  protected abstract static class MigrateEventSubProcessAndTriggerTestConfiguration {
    public abstract BpmnModelInstance getBpmnModel();
    public abstract void triggerEvent(MigrationTestRule testHelper);
    public abstract String getEventName();

    public void assertMigration(MigrationTestRule testHelper) {
      testHelper.assertEventSubscriptionMigrated(EventSubProcessModels.EVENT_SUB_PROCESS_START_ID,
        EventSubProcessModels.EVENT_SUB_PROCESS_START_ID,
        getEventName());
    }
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {//message event sub process configuration
        new MigrateEventSubProcessAndTriggerTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS;
          }

          @Override
          public void triggerEvent(MigrationTestRule testHelper) {
            testHelper.correlateMessage(EventSubProcessModels.MESSAGE_NAME);
          }

          @Override
          public String getEventName() {
            return EventSubProcessModels.MESSAGE_NAME;
          }

          @Override
          public String toString() {
            return "MigrationTriggerMessageEventSubProcess";
          }
        }
      },//signal event sub process configuration
      {
        new MigrateEventSubProcessAndTriggerTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS;
          }

          @Override
          public void triggerEvent(MigrationTestRule testHelper) {
            testHelper.sendSignal(EventSubProcessModels.SIGNAL_NAME);
          }

          @Override
          public String getEventName() {
            return EventSubProcessModels.SIGNAL_NAME;
          }

          @Override
          public String toString() {
            return "MigrationTriggerSignalEventSubProcess";
          }
        }
      },
      {//timer event sub process configuration
        new MigrateEventSubProcessAndTriggerTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS;
          }

          @Override
          public void triggerEvent(MigrationTestRule testHelper) {
            testHelper.triggerTimer();
          }

          @Override
          public String getEventName() {
            return null;
          }

          @Override
          public void assertMigration(MigrationTestRule testHelper) {
            testHelper.assertJobMigrated(EventSubProcessModels.EVENT_SUB_PROCESS_START_ID,
              EventSubProcessModels.EVENT_SUB_PROCESS_START_ID,
              TimerStartEventSubprocessJobHandler.TYPE);
          }

          @Override
          public String toString() {
            return "MigrationTriggerTimerEventSubProcess";
          }
        }
      },
      {//conditional event sub process configuration
        new MigrateEventSubProcessAndTriggerTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.CONDITIONAL_EVENT_SUBPROCESS_PROCESS;
          }


          @Override
          public void triggerEvent(MigrationTestRule testHelper) {
            testHelper.setAnyVariable(testHelper.snapshotAfterMigration.getProcessInstanceId());
          }

          @Override
          public String getEventName() {
            return null;
          }

          @Override
          public String toString() {
            return "MigrationTriggerConditionalEventSubProcess";
          }
        }
      }
    });
  }

  @Parameterized.Parameter
  public MigrateEventSubProcessAndTriggerTestConfiguration configuration;

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateEventSubprocessSignalTrigger() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(configuration.getBpmnModel());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(configuration.getBpmnModel());

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(EventSubProcessModels.USER_TASK_ID, EventSubProcessModels.USER_TASK_ID)
      .mapActivities(EventSubProcessModels.EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.EVENT_SUB_PROCESS_START_ID)
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    configuration.assertMigration(testHelper);

    // and it is possible to trigger the event subprocess
    configuration.triggerEvent(testHelper);
    Assert.assertEquals(1, rule.getTaskService().createTaskQuery().count());

    // and complete the process instance
    testHelper.completeTask(EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
