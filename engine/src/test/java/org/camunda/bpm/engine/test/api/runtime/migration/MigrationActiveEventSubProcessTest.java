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

import static org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_ID;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_START_ID;
import static org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MigrationActiveEventSubProcessTest {

  protected abstract static class MigrationActiveEventSubProcessTestConfiguration extends MigrationTestConfiguration {
    public abstract BpmnModelInstance getBpmnModel();

    @Override
    public BpmnEventTrigger addBoundaryEvent(BpmnModelInstance modelInstance, String activityId) {
      return null;
    }

    @Override
    public BpmnEventTrigger addEventSubProcess(BpmnModelInstance modelInstance, String parentId) {
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
        new MigrationActiveEventSubProcessTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.MESSAGE_EVENT_SUBPROCESS_PROCESS;
          }

          @Override
          public String getEventName() {
            return EventSubProcessModels.MESSAGE_NAME;
          }

          @Override
          public String toString() {
            return "MigrationActiveMessageEventSubProcess";
          }
        }
      },//signal event sub process configuration
      {
        new MigrationActiveEventSubProcessTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.SIGNAL_EVENT_SUBPROCESS_PROCESS;
          }

          @Override
          public String getEventName() {
            return EventSubProcessModels.SIGNAL_NAME;
          }

          @Override
          public String toString() {
            return "MigrationActiveSignalEventSubProcess";
          }
        }
      },
      {//timer event sub process configuration
        new MigrationActiveEventSubProcessTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS;
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
            return "MigrationActiveTimerEventSubProcess";
          }
        }
      },
      {//conditional event sub process configuration
        new MigrationActiveEventSubProcessTestConfiguration() {
          @Override
          public BpmnModelInstance getBpmnModel() {
            return EventSubProcessModels.CONDITIONAL_EVENT_SUBPROCESS_PROCESS;
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
            return "MigrationActiveConditionalEventSubProcess";
          }
        }
      }
    });
  }

  @Parameterized.Parameter
  public MigrationActiveEventSubProcessTestConfiguration configuration;

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateActiveCompensationEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(configuration.getBpmnModel());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(configuration.getBpmnModel());

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity(EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    Map<String, String> activities = new HashMap<String, String>();
    activities.put(EVENT_SUB_PROCESS_ID, EVENT_SUB_PROCESS_ID);
    activities.put(EVENT_SUB_PROCESS_START_ID, EVENT_SUB_PROCESS_START_ID);
    activities.put(EVENT_SUB_PROCESS_TASK_ID, EVENT_SUB_PROCESS_TASK_ID);
    MigrationPlan migrationPlan = configuration.createMigrationPlanBuilder(rule, sourceProcessDefinition.getId(),
      targetProcessDefinition.getId(), activities).build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    configuration.assertEventSubscriptionMigration(testHelper);

    // and it is possible to complete the process instance
    testHelper.completeTask(EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
