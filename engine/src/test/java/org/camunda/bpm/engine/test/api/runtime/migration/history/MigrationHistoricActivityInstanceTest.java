/* Licensed under the Apache License, Version 2.0 (the "License");
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

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationHistoricActivityInstanceTest {


  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected HistoryService historyService;

  @Before
  public void initServices() {
    historyService = rule.getHistoryService();
    runtimeService = rule.getRuntimeService();
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateHistoryActivityInstance() {
    //given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(
        modify(ProcessModels.ONE_TASK_PROCESS)
          .changeElementId("Process", "Process2")
          .changeElementId("userTask", "userTask2")
          .changeElementName("userTask", "new activity name"));

    MigrationPlan migrationPlan = runtimeService.createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask2")
        .build();

    ProcessInstance processInstance = runtimeService.startProcessInstanceById(sourceProcessDefinition.getId());

    HistoricActivityInstanceQuery sourceHistoryActivityInstanceQuery =
        historyService.createHistoricActivityInstanceQuery()
          .processDefinitionId(sourceProcessDefinition.getId());
    HistoricActivityInstanceQuery targetHistoryActivityInstanceQuery =
        historyService.createHistoricActivityInstanceQuery()
          .processDefinitionId(targetProcessDefinition.getId());

    //when
    assertEquals(2, sourceHistoryActivityInstanceQuery.count());
    assertEquals(0, targetHistoryActivityInstanceQuery.count());
    ProcessInstanceQuery sourceProcessInstanceQuery = runtimeService.createProcessInstanceQuery().processDefinitionId(sourceProcessDefinition.getId());
    runtimeService.newMigration(migrationPlan)
      .processInstanceQuery(sourceProcessInstanceQuery)
      .execute();

    // then one instance of the start event still belongs to the source process
    // and one active user task instances is now migrated to the target process
    assertEquals(1, sourceHistoryActivityInstanceQuery.count());
    assertEquals(1, targetHistoryActivityInstanceQuery.count());

    HistoricActivityInstance instance = targetHistoryActivityInstanceQuery.singleResult();
    assertMigratedTo(instance, targetProcessDefinition, "userTask2");
    assertEquals("new activity name", instance.getActivityName());
    assertEquals(processInstance.getId(), instance.getParentActivityInstanceId());
    assertEquals("userTask", instance.getActivityType());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateHistoricSubProcessInstance() {
    //given
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(processDefinition.getId(), processDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(processDefinition.getId());

    // when
    rule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    List<HistoricActivityInstance> historicInstances = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .unfinished()
        .orderByActivityId()
        .asc()
        .list();

    Assert.assertEquals(2, historicInstances.size());

    assertMigratedTo(historicInstances.get(0), processDefinition, "subProcess");
    assertMigratedTo(historicInstances.get(1), processDefinition, "userTask");
    assertEquals(processInstance.getId(), historicInstances.get(0).getParentActivityInstanceId());
    assertEquals(historicInstances.get(0).getId(), historicInstances.get(1).getParentActivityInstanceId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateHistoricSubProcessRename() {
    //given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
        .changeElementId("subProcess", "newSubProcess"));

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("subProcess", "newSubProcess")
        .mapActivities("userTask", "userTask")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // when
    rule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    List<HistoricActivityInstance> historicInstances = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .unfinished()
        .orderByActivityId()
        .asc()
        .list();

    Assert.assertEquals(2, historicInstances.size());

    assertMigratedTo(historicInstances.get(0), targetDefinition, "newSubProcess");
    assertMigratedTo(historicInstances.get(1), targetDefinition, "userTask");
    assertEquals(processInstance.getId(), historicInstances.get(0).getParentActivityInstanceId());
    assertEquals(historicInstances.get(0).getId(), historicInstances.get(1).getParentActivityInstanceId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testHistoricActivityInstanceBecomeScope() {
    //given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // when
    rule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    List<HistoricActivityInstance> historicInstances = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .unfinished()
        .orderByActivityId()
        .asc()
        .list();

    Assert.assertEquals(1, historicInstances.size());

    assertMigratedTo(historicInstances.get(0), targetDefinition, "userTask");
    assertEquals(processInstance.getId(), historicInstances.get(0).getParentActivityInstanceId());
  }

  @Test
  @RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
  public void testMigrateHistoricActivityInstanceAddScope() {
    //given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // when
    rule.getRuntimeService().newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    List<HistoricActivityInstance> historicInstances = historyService
        .createHistoricActivityInstanceQuery()
        .processInstanceId(processInstance.getId())
        .unfinished()
        .orderByActivityId()
        .asc()
        .list();

    Assert.assertEquals(2, historicInstances.size());

    assertMigratedTo(historicInstances.get(0), targetDefinition, "subProcess");
    assertMigratedTo(historicInstances.get(1), targetDefinition, "userTask");
    assertEquals(processInstance.getId(), historicInstances.get(0).getParentActivityInstanceId());
    assertEquals(historicInstances.get(0).getId(), historicInstances.get(1).getParentActivityInstanceId());
  }

  protected void assertMigratedTo(HistoricActivityInstance activityInstance, ProcessDefinition processDefinition, String activityId) {
    Assert.assertEquals(processDefinition.getId(), activityInstance.getProcessDefinitionId());
    Assert.assertEquals(processDefinition.getKey(), activityInstance.getProcessDefinitionKey());
    Assert.assertEquals(activityId, activityInstance.getActivityId());
  }

}
