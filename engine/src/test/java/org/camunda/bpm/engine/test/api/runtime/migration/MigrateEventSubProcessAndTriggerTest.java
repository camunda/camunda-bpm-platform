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
import org.camunda.bpm.engine.migration.MigrationInstructionBuilder;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanBuilder;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.util.BpmnEventTrigger;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
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
import java.util.HashMap;
import java.util.Map;

import static org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_START_ID;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.USER_TASK_ID;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MigrateEventSubProcessAndTriggerTest {

  protected abstract static class MigrateEventSubProcessAndTriggerTestConfiguration extends MigrationTestConfiguration {
    @Override
    public BpmnEventTrigger addBoundaryEvent(BpmnModelInstance modelInstance, String parentId) {
      return null;
    }

    public void assertEventSubscriptionMigration(MigrationTestRule testHelper) {
      assertEventSubscriptionMigration(testHelper, EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID);
    }
  }

  @Parameterized.Parameters(name = "{index}: {0}")
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
      {//message event sub process configuration
        new MigrateEventSubProcessAndTriggerTestConfiguration() {

          @Override
          public BpmnEventTrigger addEventSubProcess(BpmnModelInstance modelInstance, String activityId) {
            return new BpmnEventTrigger() {
              @Override
              public void trigger(ProcessEngineTestRule rule) {
                rule.correlateMessage(EventSubProcessModels.MESSAGE_NAME);
              }

              @Override
              public BpmnModelInstance getProcessModel() {
                return EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS;
              }
            };
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
          public BpmnEventTrigger addEventSubProcess(BpmnModelInstance modelInstance, String activityId) {
            return new BpmnEventTrigger() {
              @Override
              public void trigger(ProcessEngineTestRule rule) {
                rule.sendSignal(EventSubProcessModels.SIGNAL_NAME);
              }

              @Override
              public BpmnModelInstance getProcessModel() {
                return EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS;
              }
            };
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
          public BpmnEventTrigger addEventSubProcess(BpmnModelInstance modelInstance, String activityId) {
            return new BpmnEventTrigger() {
              @Override
              public void trigger(ProcessEngineTestRule rule) {
                ((MigrationTestRule) rule).triggerTimer();
              }

              @Override
              public BpmnModelInstance getProcessModel() {
                return EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS;
              }
            };
          }

          @Override
          public String getEventName() {
            return null;
          }

          @Override
          public void assertEventSubscriptionMigration(MigrationTestRule testHelper) {
            testHelper.assertJobMigrated(EVENT_SUB_PROCESS_START_ID,
              EVENT_SUB_PROCESS_START_ID,
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
          public BpmnEventTrigger addEventSubProcess(BpmnModelInstance modelInstance, String activityId) {
            return new BpmnEventTrigger() {
              @Override
              public void trigger(ProcessEngineTestRule rule) {
                rule.setAnyVariable(((MigrationTestRule) rule).snapshotAfterMigration.getProcessInstanceId());
              }

              @Override
              public BpmnModelInstance getProcessModel() {
                return EventSubProcessModels.CONDITIONAL_EVENT_SUBPROCESS_PROCESS;
              }
            };
          }

          @Override
          public MigrationPlanBuilder createMigrationPlanBuilder(ProcessEngineRule rule, String srcProcDefId, String trgProcDefId, Map<String, String> activities) {
            MigrationPlanBuilder migrationPlanBuilder = createMigrationPlanBuilder(rule, srcProcDefId, trgProcDefId);

            for (String key : activities.keySet()) {
              MigrationInstructionBuilder migrationInstructionBuilder = migrationPlanBuilder.mapActivities(key, activities.get(key));
              if (key.contains(EVENT_SUB_PROCESS_START_ID)) {
                migrationInstructionBuilder.updateEventTrigger();
              }
            }
            return migrationPlanBuilder;
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
    BpmnEventTrigger bpmnEventTrigger = configuration.addEventSubProcess(null, null);
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(bpmnEventTrigger.getProcessModel());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(bpmnEventTrigger.getProcessModel());

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(USER_TASK_ID, USER_TASK_ID);
    activities.put(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    configuration.assertEventSubscriptionMigration(testHelper);

    // and it is possible to trigger the event subprocess
    bpmnEventTrigger.trigger(testHelper);
    Assert.assertEquals(1, rule.getTaskService().createTaskQuery().count());

    // and complete the process instance
    testHelper.completeTask(EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
