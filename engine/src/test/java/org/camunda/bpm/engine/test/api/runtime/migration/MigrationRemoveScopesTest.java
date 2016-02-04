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
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateEvent;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateExecutionListener;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationRemoveScopesTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testRemoveScopeForNonScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree("userTask").scope().id(processInstance.getId())
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstance(activityInstance, "userTask").getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveScopeForScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
      .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivity(activityInstance, "userTask"))
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstance(activityInstance, "userTask").getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveScopeForConcurrentNonScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask1").concurrent().noScope().up()
        .child("userTask2").concurrent().noScope()
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
          .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveScopeForConcurrentScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS_SUB_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).concurrent().noScope()
          .child("userTask1").scope().up().up()
        .child(null).concurrent().noScope()
          .child("userTask2").scope()
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
          .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(processInstance.getId());
  }


  @Ignore("Suffers of CAM-3604")
  @Test
  public void testRemoveConcurrentScope() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask")
      .mapActivities("userTask2", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask1").concurrent().noScope().up()
        .child("userTask2").concurrent().noScope()
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
          .activity("userTask", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveConcurrentScope2() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask2").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child("userTask1").scope()
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
          .beginScope("subProcess", testHelper.getSingleActivityInstance(activityInstance, "subProcess1").getId())
            .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveScopeAndMoveToConcurrentActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask2").concurrent().noScope().up()
        .child(null).concurrent().noScope()
          .child("userTask1").scope()
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
          .beginScope("subProcess", testHelper.getSingleActivityInstance(activityInstance, "subProcess").getId())
            .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testRemoveMultipleScopes() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Arrays.asList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree("userTask").scope().id(processInstance.getId())
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstance(activityInstance, "userTask").getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }


  @Test
  public void testEndListenerInvocationForRemovedScope() {
    // given
    DelegateEvent.clearEvents();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS.clone()
        .<SubProcess>getModelElementById("subProcess")
        .builder()
        .camundaExecutionListenerClass(
            ExecutionListener.EVENTNAME_END,
            DelegateExecutionListener.class.getName())
        .done());
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(1, recordedEvents.size());

    DelegateEvent event = recordedEvents.get(0);
    assertEquals(sourceProcessDefinition.getId(), event.getProcessDefinitionId());
    assertEquals("subProcess", event.getCurrentActivityId());
    assertEquals(testHelper.getSingleActivityInstance(activityInstance, "subProcess").getId(), event.getActivityInstanceId());

    DelegateEvent.clearEvents();
  }

}
