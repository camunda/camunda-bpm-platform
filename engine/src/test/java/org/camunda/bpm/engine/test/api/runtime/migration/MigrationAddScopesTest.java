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
import static org.camunda.bpm.engine.test.util.MigrationInstructionInstanceValidationReportAssert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateEvent;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateExecutionListener;
import org.camunda.bpm.engine.test.util.ExecutionTree;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.model.bpmn.instance.ParallelGateway;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationAddScopesTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);


  @Test
  public void testScopeUserTaskMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope()
          .child("userTask").scope().id(activityInstance.getActivityInstances("userTask")[0].getExecutionIds()[0])
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
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
  public void testConcurrentScopeUserTaskMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS_SUB_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope()
          .child(null).concurrent().noScope()
            .child("userTask1").scope().id(activityInstance.getActivityInstances("userTask1")[0].getExecutionIds()[0]).up().up()
          .child(null).concurrent().noScope()
            .child("userTask2").scope().id(activityInstance.getActivityInstances("userTask2")[0].getExecutionIds()[0])
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
            .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    assertEquals(2, migratedTasks.size());

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
  public void testUserTaskMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask").scope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
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
  public void testConcurrentUserTaskMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope()
          .child("userTask1").concurrent().noScope().up()
          .child("userTask2").concurrent().noScope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
            .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    assertEquals(2, migratedTasks.size());

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
  public void testConcurrentThreeUserTaskMigration() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS.clone()
        .<ParallelGateway>getModelElementById("fork").builder()
        .userTask("userTask3")
        .endEvent()
        .done());
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS.clone()
        .<ParallelGateway>getModelElementById("fork").builder()
        .userTask("userTask3")
        .endEvent()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask2")
      .mapActivities("userTask2", "userTask3")
      .mapActivities("userTask3", "userTask1")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope()
          .child("userTask1").concurrent().noScope().up()
          .child("userTask2").concurrent().noScope().up()
          .child("userTask3").concurrent().noScope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .activity("userTask1", testHelper.getSingleActivityInstance(activityInstance, "userTask3").getId())
            .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
            .activity("userTask3", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    assertEquals(3, migratedTasks.size());

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
  public void testNestedScopesMigration1() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("subProcess", "outerSubProcess")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope().id(activityInstance.getActivityInstances("subProcess")[0].getExecutionIds()[0])
          .child("userTask").scope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("outerSubProcess", testHelper.getSingleActivityInstance(activityInstance, "subProcess").getId())
            .beginScope("innerSubProcess")
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
  public void testNestedScopesMigration2() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("subProcess", "innerSubProcess")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope()
          .child("userTask").scope().id(activityInstance.getActivityInstances("subProcess")[0].getExecutionIds()[0])
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("outerSubProcess")
            .beginScope("innerSubProcess", testHelper.getSingleActivityInstance(activityInstance, "subProcess").getId())
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
  public void testMultipleInstancesOfScope() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("subProcess", "outerSubProcess")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().createProcessInstanceById(sourceProcessDefinition.getId())
        .startBeforeActivity("subProcess")
        .startBeforeActivity("subProcess")
        .execute();
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("userTask").scope().up().up().up()
        .child(null).concurrent().noScope()
          .child(null).scope()
            .child("userTask").scope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("outerSubProcess", activityInstance.getActivityInstances("subProcess")[0].getId())
            .beginScope("innerSubProcess")
              .activity("userTask", activityInstance.getActivityInstances("subProcess")[0].getActivityInstances("userTask")[0].getId())
            .endScope()
          .endScope()
          .beginScope("outerSubProcess", activityInstance.getActivityInstances("subProcess")[1].getId())
            .beginScope("innerSubProcess")
              .activity("userTask", activityInstance.getActivityInstances("subProcess")[1].getActivityInstances("userTask")[0].getId())
        .done());

    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    assertEquals(2, migratedTasks.size());

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
  public void testChangeActivityId() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);

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
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child("userTask2").scope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testChangeScopeActivityId() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS_SUB_PROCESS);

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
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ExecutionTree executionTree = ExecutionTree.forExecution(processInstance.getId(), rule.getProcessEngine());
    assertThat(executionTree)
    .matches(
      describeExecutionTree(null).scope().id(processInstance.getId())
        .child(null).scope()
          .child("userTask2").scope()
      .done());
    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask1").getId())
        .done());

    Task migratedTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testListenerInvocationForNewlyCreatedScope() {
    // given
    DelegateEvent.clearEvents();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(
      ProcessModels.SUBPROCESS_PROCESS.clone()
        .<SubProcess>getModelElementById("subProcess")
        .builder()
        .camundaExecutionListenerClass(
            ExecutionListener.EVENTNAME_START,
            DelegateExecutionListener.class.getName())
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(1, recordedEvents.size());

    DelegateEvent event = recordedEvents.get(0);
    assertEquals(targetProcessDefinition.getId(), event.getProcessDefinitionId());
    assertEquals("subProcess", event.getCurrentActivityId());

    DelegateEvent.clearEvents();
  }

  @Test
  public void testDeleteMigratedInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SCOPE_TASKS_SUB_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then it is possible to delete the process instance
    rule.getRuntimeService().deleteProcessInstance(processInstance.getId(), null);
    testHelper.assertProcessEnded(processInstance.getId());
  }

  /**
   * Readd when we implement migration for multi-instance
   */
  @Test
  @Ignore
  public void testAddParentScopeToMultiInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(
      ProcessModels.ONE_TASK_PROCESS.clone()
        .<UserTask>getModelElementById("userTask").builder()
        .multiInstance()
          .parallel()
          .camundaCollection("collectionVar")
          .camundaElementVariable("elementVar")
        .done());
    ProcessDefinition targetProcessDefinition = testHelper.deploy(
      ProcessModels.SUBPROCESS_PROCESS.clone()
        .<UserTask>getModelElementById("userTask").builder()
        .multiInstance()
          .parallel()
          .camundaCollection("collectionVar")
          .camundaElementVariable("elementVar")
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask#multiInstanceBody", "userTask#multiInstanceBody")
      .mapActivities("userTask", "userTask")
      .build();

    List<String> miElements = new ArrayList<String>();
    miElements.add("a");
    miElements.add("b");
    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(sourceProcessDefinition.getId(),
            Variables.createVariables().putValue("collectionVar", miElements));

    // when
    rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));

    // then
    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .beginMiBody("userTask")
              .activity("userTask")
              .activity("userTask")
              .activity("userTask")
        .done());

    // the element variables still exist
    List<Task> migratedTasks = rule.getTaskService().createTaskQuery().list();
    assertEquals(2, migratedTasks.size());

    List<String> collectedElementsVars = new ArrayList<String>();
    for (Task migratedTask : migratedTasks) {
      collectedElementsVars.add((String) rule.getTaskService().getVariable(migratedTask.getId(), "elementVar"));
    }

    Assert.assertTrue(collectedElementsVars.contains("a"));
    Assert.assertTrue(collectedElementsVars.contains("b"));

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCannotAddTwoScopes() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    try {
      rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
      Assert.fail("should fail");
    }
    catch (MigrationInstructionInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasProcessInstance(processInstance)
        .hasFailures(1)
        .hasFailure("userTask", "Parent activity instance must be migrated to the parent or grandparent scope");
    }
  }

  @Test
  public void testCannotMigrateParentScopeWayTooHigh() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.TRIPLE_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess1")
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    try {
      rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
      Assert.fail("should fail");
    }
    catch (MigrationInstructionInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasProcessInstance(processInstance)
        .hasFailures(1)
        .hasFailure("userTask", "Parent activity instance must be migrated to the parent or grandparent scope");
    }
  }

  @Test
  public void testMoveConcurrentActivityIntoSiblingScope() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);

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
        .child(null).concurrent().noScope()
          .child("userTask2").scope().up().up()
        .child(null).concurrent().noScope()
          .child("userTask1").scope()
      .done());

    assertThat(executionTree).hasProcessDefinitionId(targetProcessDefinition.getId());

    ActivityInstance updatedTree = rule.getRuntimeService().getActivityInstance(processInstance.getId());
    assertThat(updatedTree).hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginScope("subProcess")
            .activity("userTask2", testHelper.getSingleActivityInstance(activityInstance, "userTask2").getId())
          .endScope()
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

}
