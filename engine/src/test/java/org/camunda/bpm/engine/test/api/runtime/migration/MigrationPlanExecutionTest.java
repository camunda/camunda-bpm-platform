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
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanExecutionTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testUserTaskMigrationInProcessDefinitionScope() {
    // given
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    Task task = rule.getTaskService().createTaskQuery().singleResult();

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then

    // the entities were migrated
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree("userTask").scope().id(processInstance.getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertEquals(task.getId(), migratedTask.getId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());

  }

  @Test
  public void testUserTaskMigrationInSubProcessScope() {

    // given
    testHelper.deploy("subProcess.bpmn20.xml", ProcessModels.SUBPROCESS_PROCESS);
    testHelper.deploy("subProcess.bpmn20.xml", ProcessModels.SUBPROCESS_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("SubProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("SubProcess", 2);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ExecutionTree sourceExecutionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    Task task = rule.getTaskService().createTaskQuery().singleResult();

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then

    // the entities were migrated
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask").scope().id(sourceExecutionTree.getExecutions().get(0).getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertEquals(task.getId(), migratedTask.getId());
    Assert.assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testConcurrentUserTaskMigration() {
    // given
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.PARALLEL_GATEWAY_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.PARALLEL_GATEWAY_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("ParallelGatewayProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("ParallelGatewayProcess", 2);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then

    // the entities were migrated
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask1").concurrent().noScope().up()
        .child("userTask2").concurrent().noScope()
      .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(2, migratedTasks.size());

    for (Task migratedTask : migratedTasks) {
      Assert.assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCannotMigrateWhenNotAllActivityInstancesAreMapped() {
    // given
    testHelper.deploy("parallelGateway.bpmn20.xml", ProcessModels.PARALLEL_GATEWAY_PROCESS);
    testHelper.deploy("subProcess.bpmn20.xml", ProcessModels.PARALLEL_GATEWAY_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("ParallelGatewayProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("ParallelGatewayProcess", 2);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    try {
      rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));
      Assert.fail("should not succeed because the userTask2 instance is not mapped");
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("There are no migration instructions that apply to the following activity instances"));
    }
  }

}
