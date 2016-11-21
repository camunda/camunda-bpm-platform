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

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.util.BpmnEventFactory;
import org.camunda.bpm.engine.test.api.runtime.migration.util.ConditionalEventFactory;
import org.camunda.bpm.engine.test.api.runtime.migration.util.MessageEventFactory;
import org.camunda.bpm.engine.test.api.runtime.migration.util.MigratingBpmnEventTrigger;
import org.camunda.bpm.engine.test.api.runtime.migration.util.SignalEventFactory;
import org.camunda.bpm.engine.test.api.runtime.migration.util.TimerEventFactory;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Christopher Zell <christopher.zell@camunda.com>
 */
@RunWith(Parameterized.class)
public class MigrationActiveEventSubProcessTest {

  @Parameters
  public static Collection<Object[]> data() {
      return Arrays.asList(new Object[][] {
               new Object[]{ new TimerEventFactory() },
               new Object[]{ new MessageEventFactory() },
               new Object[]{ new SignalEventFactory() },
               new Object[]{ new ConditionalEventFactory() }
         });
  }

  @Parameter
  public BpmnEventFactory eventFactory;

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Before
  public void setUp() {
    ClockUtil.setCurrentTime(new Date()); // lock time so that timer job is effectively not updated
  }

  @Test
  public void testMigrateActiveCompensationEventSubProcess() {
    // given
    BpmnModelInstance processModel = ProcessModels.ONE_TASK_PROCESS.clone();
    MigratingBpmnEventTrigger eventTrigger = eventFactory.addEventSubProcess(
        rule.getProcessEngine(),
        processModel,
        ProcessModels.PROCESS_KEY,
        "eventSubProcess",
        "eventSubProcessStart");
    ModifiableBpmnModelInstance.wrap(processModel).startEventBuilder("eventSubProcessStart")
        .userTask("eventSubProcessTask")
        .endEvent()
        .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(processModel);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(processModel);

    ProcessInstance processInstance = rule.getRuntimeService()
      .createProcessInstanceById(sourceProcessDefinition.getId())
      .startBeforeActivity("eventSubProcessStart")
      .executeWithVariablesInReturn();

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventSubProcess", "eventSubProcess")
      .mapActivities("eventSubProcessStart", "eventSubProcessStart").updateEventTrigger()
      .mapActivities("eventSubProcessTask", "eventSubProcessTask")
      .build();

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    eventTrigger.assertEventTriggerMigrated(testHelper, "eventSubProcessStart");

    // and it is possible to complete the process instance
    testHelper.completeTask("eventSubProcessTask");
    testHelper.assertProcessEnded(processInstance.getId());
  }
}
