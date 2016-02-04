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

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
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
public class MigrationSameScopesTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testUserTaskMigrationInProcessDefinitionScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

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
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertEquals(task.getId(), migratedTask.getId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());

  }

  @Test
  public void testUserTaskMigrationInSubProcessScope() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

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
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

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
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

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
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

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
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

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

  @Test
  public void testChangeActivityId() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService()
        .createProcessInstanceById(sourceProcessDefinition.getId())
        .startBeforeActivity("userTask1")
        .execute();
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree("userTask2").scope().id(processInstance.getId())
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertNotNull(migratedTask);
    Assert.assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }

}
