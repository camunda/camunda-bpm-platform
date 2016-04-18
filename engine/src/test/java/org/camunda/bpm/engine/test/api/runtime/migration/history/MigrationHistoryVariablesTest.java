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

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.test.util.CachedProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationHistoryVariablesTest {

  protected ProcessEngineRule rule = new CachedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  protected static final BpmnModelInstance ONE_BOUNDARY_TASK = ModifiableBpmnModelInstance.modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
      .boundaryEvent()
      .message("Message")
      .done();

  protected static final BpmnModelInstance CONCURRENT_BOUNDARY_TASKS = ModifiableBpmnModelInstance.modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
      .boundaryEvent()
      .message("Message")
      .moveToActivity("userTask2")
      .boundaryEvent()
      .message("Message")
      .done();

  protected static final BpmnModelInstance SUBPROCESS_CONCURRENT_BOUNDARY_TASKS = ModifiableBpmnModelInstance.modify(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS)
      .activityBuilder("userTask1")
      .boundaryEvent()
      .message("Message")
      .moveToActivity("userTask2")
      .boundaryEvent()
      .message("Message")
      .done();

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;
  protected TaskService taskService;
  protected HistoryService historyService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
    taskService = rule.getTaskService();
    historyService = rule.getHistoryService();
  }

  @Test
  public void noHistoryUpdateOnSameStructureMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ONE_BOUNDARY_TASK);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ONE_BOUNDARY_TASK);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = runtimeService
        .startProcessInstanceById(sourceProcessDefinition.getId());
    ExecutionTree executionTreeBeforeMigration =
        ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());

    ExecutionTree scopeExecution = executionTreeBeforeMigration.getExecutions().get(0);

    runtimeService.setVariableLocal(scopeExecution.getId(), "foo", 42);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then there is still one historic variable instance
    Assert.assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());

    // and no additional historic details
    Assert.assertEquals(1, historyService.createHistoricDetailQuery().count());
  }

  @Test
  public void noHistoryUpdateOnAddScopeMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(CONCURRENT_BOUNDARY_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(SUBPROCESS_CONCURRENT_BOUNDARY_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = runtimeService
        .startProcessInstanceById(sourceProcessDefinition.getId());
    ExecutionTree executionTreeBeforeMigration =
        ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());

    ExecutionTree userTask1CCExecutionBefore  = executionTreeBeforeMigration
        .getLeafExecutions("userTask1")
        .get(0)
        .getParent();

    runtimeService.setVariableLocal(userTask1CCExecutionBefore.getId(), "foo", 42);

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then there is still one historic variable instance
    Assert.assertEquals(1, historyService.createHistoricVariableInstanceQuery().count());

    // and no additional historic details
    Assert.assertEquals(1, historyService.createHistoricDetailQuery().count());
  }
}
