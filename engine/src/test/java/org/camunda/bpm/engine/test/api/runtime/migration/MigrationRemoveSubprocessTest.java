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

import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.VariableInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateEvent;
import org.camunda.bpm.engine.test.bpmn.multiinstance.DelegateExecutionListener;
import org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationRemoveSubprocessTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testRemoveScopeForNonScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveScopeForScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.SCOPE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration("userTask"))
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveScopeForConcurrentNonScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask1").concurrent().noScope().up()
          .child("userTask2").concurrent().noScope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask1", testHelper.getSingleActivityInstanceBeforeMigration("userTask1").getId())
          .activity("userTask2", testHelper.getSingleActivityInstanceBeforeMigration("userTask2").getId())
        .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveScopeForConcurrentScopeActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SCOPE_TASKS_SUB_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SCOPE_TASKS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child(null).concurrent().noScope()
            .child("userTask1").scope().up().up()
          .child(null).concurrent().noScope()
            .child("userTask2").scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask1", testHelper.getSingleActivityInstanceBeforeMigration("userTask1").getId())
          .activity("userTask2", testHelper.getSingleActivityInstanceBeforeMigration("userTask2").getId())
        .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }


  @Test
  public void testRemoveConcurrentScope() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask1", "userTask")
      .mapActivities("userTask2", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").concurrent().noScope().up()
          .child("userTask").concurrent().noScope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask1").getId())
          .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask2").getId())
        .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testRemoveConcurrentScope2() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess1", "subProcess")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask2").concurrent().noScope().up()
          .child(null).concurrent().noScope()
          .child("userTask1").scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask2", testHelper.getSingleActivityInstanceBeforeMigration("userTask2").getId())
          .beginScope("subProcess", testHelper.getSingleActivityInstanceBeforeMigration("subProcess1").getId())
            .activity("userTask1", testHelper.getSingleActivityInstanceBeforeMigration("userTask1").getId())
        .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  @Ignore("Missing feature CAM-5407")
  public void testRemoveScopeAndMoveToConcurrentActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask1", "userTask1")
      .mapActivities("userTask2", "userTask2")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask2").concurrent().noScope().up()
          .child(null).concurrent().noScope()
            .child("userTask1").scope()
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask2", testHelper.getSingleActivityInstanceBeforeMigration("userTask2").getId())
          .beginScope("subProcess", testHelper.getSingleActivityInstanceBeforeMigration("subProcess").getId())
            .activity("userTask1", testHelper.getSingleActivityInstanceBeforeMigration("userTask1").getId())
        .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(2, migratedTasks.size());
    for (Task migratedTask : migratedTasks) {
      assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());
    }

    // and it is possible to successfully complete the migrated instance
    for (Task migratedTask : migratedTasks) {
      rule.getTaskService().complete(migratedTask.getId());
    }
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  /**
   * Remove when implementing CAM-5407
   */
  @Test
  public void testCannotRemoveScopeAndMoveToConcurrentActivity() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);

    // when
    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask1", "userTask1")
        .mapActivities("userTask2", "userTask2")
        .build();

      Assert.fail("should not validate");
    } catch (MigrationPlanValidationException e) {
      MigrationPlanValidationReportAssert.assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask2",
          "The closest mapped ancestor 'subProcess' is mapped to scope 'subProcess' which is not an ancestor of target scope 'userTask2'"
        );
    }
  }

  @Test
  public void testRemoveMultipleScopes() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .done());


    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    rule.getTaskService().complete(migratedTask.getId());
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }


  @Test
  public void testEndListenerInvocationForRemovedScope() {
    // given
    DelegateEvent.clearEvents();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, DelegateExecutionListener.class.getName())
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(1, recordedEvents.size());

    DelegateEvent event = recordedEvents.get(0);
    assertEquals(sourceProcessDefinition.getId(), event.getProcessDefinitionId());
    assertEquals("subProcess", event.getCurrentActivityId());
    assertEquals(testHelper.getSingleActivityInstanceBeforeMigration("subProcess").getId(), event.getActivityInstanceId());

    DelegateEvent.clearEvents();
  }

  @Test
  public void testSkipListenerInvocationForRemovedScope() {
    // given
    DelegateEvent.clearEvents();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaExecutionListenerClass(ExecutionListener.EVENTNAME_END, DelegateExecutionListener.class.getName())
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = rule.getRuntimeService()
        .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    rule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .skipCustomListeners()
      .execute();

    // then
    List<DelegateEvent> recordedEvents = DelegateEvent.getEvents();
    assertEquals(0, recordedEvents.size());

    DelegateEvent.clearEvents();
  }

  @Test
  public void testIoMappingInvocationForRemovedScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaOutputParameter("foo", "bar")
      .done());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = rule.getRuntimeService()
      .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    rule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    VariableInstance inputVariable = rule.getRuntimeService().createVariableInstanceQuery().singleResult();
    Assert.assertNotNull(inputVariable);
    assertEquals("foo", inputVariable.getName());
    assertEquals("bar", inputVariable.getValue());
    assertEquals(processInstance.getId(), inputVariable.getActivityInstanceId());
  }

  @Test
  public void testSkipIoMappingInvocationForRemovedScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
      .camundaOutputParameter("foo", "bar")
      .done());
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance = rule.getRuntimeService()
      .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    rule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .skipIoMappings()
      .execute();

    // then
    assertEquals(0, rule.getRuntimeService().createVariableInstanceQuery().count());
  }


  @Test
  public void testCannotRemoveParentScopeAndMoveOutOfGrandParentScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.TRIPLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.TRIPLE_SUBPROCESS_PROCESS);

    // when
    try {
      // subProcess2 is not migrated
      // subProcess 3 is moved out of the subProcess1 scope (by becoming a subProcess1 itself)
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess1", "subProcess1")
        .mapActivities("subProcess3", "subProcess1")
        .mapActivities("userTask", "userTask")
        .build();

      Assert.fail("should not validate");
    } catch (MigrationPlanValidationException e) {
      MigrationPlanValidationReportAssert.assertThat(e.getValidationReport())
        .hasInstructionFailures("subProcess3",
          "The closest mapped ancestor 'subProcess1' is mapped to scope 'subProcess1' which is not an ancestor of target scope 'subProcess1'"
        );
    }
  }

}
