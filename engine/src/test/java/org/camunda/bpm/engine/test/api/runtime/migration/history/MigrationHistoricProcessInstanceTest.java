/*
 * Copyright 2016 camunda services GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime.migration.history;

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.junit.Assert.assertEquals;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricProcessInstanceQuery;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 *
 * @author Christopher Zell
 */
public class MigrationHistoricProcessInstanceTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  //============================================================================
  //===================================Migration================================
  //============================================================================
  protected ProcessDefinition sourceProcessDefinition;
  protected ProcessDefinition targetProcessDefinition;
  protected MigrationPlan migrationPlan;

  @Before
  public void initTest() {
    runtimeService = rule.getRuntimeService();
    historyService = rule.getHistoryService();


    sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ModifiableBpmnModelInstance modifiedModel = modify(ProcessModels.ONE_TASK_PROCESS).changeElementId("Process", "Process2")
                                                                                      .changeElementId("userTask", "userTask2");
    targetProcessDefinition = testHelper.deployAndGetDefinition(modifiedModel);
    migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
                                                                                            .mapActivities("userTask", "userTask2")
                                                                                            .build();
    runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateHistoryProcessInstance() {
    //given
    HistoricProcessInstanceQuery sourceHistoryProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery()
          .processDefinitionId(sourceProcessDefinition.getId());
    HistoricProcessInstanceQuery targetHistoryProcessInstanceQuery =
        historyService.createHistoricProcessInstanceQuery()
          .processDefinitionId(targetProcessDefinition.getId());


    //when
    assertEquals(1, sourceHistoryProcessInstanceQuery.count());
    assertEquals(0, targetHistoryProcessInstanceQuery.count());
    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    runtimeService.newMigration(migrationPlan)
      .processInstanceQuery(sourceProcessInstanceQuery)
      .execute();

    //then
    assertEquals(0, sourceHistoryProcessInstanceQuery.count());
    assertEquals(1, targetHistoryProcessInstanceQuery.count());

    HistoricProcessInstance instance = targetHistoryProcessInstanceQuery.singleResult();
    assertEquals(instance.getProcessDefinitionKey(), targetProcessDefinition.getKey());
  }


}
