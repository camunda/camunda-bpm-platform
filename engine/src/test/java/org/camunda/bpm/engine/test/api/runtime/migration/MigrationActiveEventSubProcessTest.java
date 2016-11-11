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
public class MigrationActiveEventSubProcessTest {

  protected abstract static class MigrationActiveEventSubProcessTestConfiguration {
    public abstract BpmnModelInstance getBpmnModel();

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
          public void assertMigration(MigrationTestRule testHelper) {
            testHelper.assertJobMigrated(EventSubProcessModels.EVENT_SUB_PROCESS_START_ID,
              EventSubProcessModels.EVENT_SUB_PROCESS_START_ID,
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
            return EventSubProcessModels.TRUE_CONDITIONAL_EVENT_SUBPROCESS_PROCESS;
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
      .startBeforeActivity(EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID)
      .execute();

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(EventSubProcessModels.EVENT_SUB_PROCESS_ID, EventSubProcessModels.EVENT_SUB_PROCESS_ID)
      .mapActivities(EventSubProcessModels.EVENT_SUB_PROCESS_START_ID, EventSubProcessModels.EVENT_SUB_PROCESS_START_ID)
      .mapActivities(EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID, EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID)
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    configuration.assertMigration(testHelper);

    // and it is possible to complete the process instance
    testHelper.completeTask(EventSubProcessModels.EVENT_SUB_PROCESS_TASK_ID);
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
