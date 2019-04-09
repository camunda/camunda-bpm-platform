/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.runtime.migration;

import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.impl.bpmn.parser.BpmnParse;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MultiInstanceProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationMultiInstanceTest {

  public static final String NUMBER_OF_INSTANCES = "nrOfInstances";
  public static final String NUMBER_OF_ACTIVE_INSTANCES = "nrOfActiveInstances";
  public static final String NUMBER_OF_COMPLETED_INSTANCES = "nrOfCompletedInstances";
  public static final String LOOP_COUNTER = "loopCounter";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMigrateParallelMultiInstanceTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child(null).scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration(miBodyOf("userTask")))
            .child("userTask").concurrent().noScope().up()
            .child("userTask").concurrent().noScope().up()
            .child("userTask").concurrent().noScope().up()
          .done());

    ActivityInstance[] userTaskInstances = testHelper.snapshotBeforeMigration.getActivityTree().getActivityInstances("userTask");

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginMiBody("userTask", testHelper.getSingleActivityInstanceBeforeMigration(miBodyOf("userTask")).getId())
            .activity("userTask", userTaskInstances[0].getId())
            .activity("userTask", userTaskInstances[1].getId())
            .activity("userTask", userTaskInstances[2].getId())
        .done());

    List<Task> migratedTasks = testHelper.snapshotAfterMigration.getTasks();
    Assert.assertEquals(3, migratedTasks.size());
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
  public void testMigrateParallelMultiInstanceTasksVariables() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService()
      .startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());

    List<Task> tasksBeforeMigration = rule.getTaskService().createTaskQuery().list();
    Map<String, Integer> loopCounterDistribution = new HashMap<String, Integer>();
    for (Task task : tasksBeforeMigration) {
      Integer loopCounter = (Integer) rule.getTaskService().getVariable(task.getId(), LOOP_COUNTER);
      loopCounterDistribution.put(task.getId(), loopCounter);
    }

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    List<Task> tasks = testHelper.snapshotAfterMigration.getTasks();
    Task firstTask = tasks.get(0);
    Assert.assertEquals(3, rule.getTaskService().getVariable(firstTask.getId(), NUMBER_OF_INSTANCES));
    Assert.assertEquals(3, rule.getTaskService().getVariable(firstTask.getId(), NUMBER_OF_ACTIVE_INSTANCES));
    Assert.assertEquals(0, rule.getTaskService().getVariable(firstTask.getId(), NUMBER_OF_COMPLETED_INSTANCES));

    for (Task task : tasks) {
      Integer loopCounter = (Integer) rule.getTaskService().getVariable(task.getId(), LOOP_COUNTER);
      Assert.assertNotNull(loopCounter);
      Assert.assertEquals(loopCounterDistribution.get(task.getId()), loopCounter);
    }
  }

  @Test
  public void testMigrateParallelMultiInstancePartiallyComplete() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance =
        rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    testHelper.completeAnyTask("userTask");
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child(null).scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration(miBodyOf("userTask")))
            .child("userTask").concurrent().noScope().up()
            .child("userTask").concurrent().noScope().up()
            .child("userTask").concurrent().noScope().up()
          .done());

    ActivityInstance[] userTaskInstances = testHelper.snapshotBeforeMigration.getActivityTree().getActivityInstances("userTask");

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginMiBody("userTask", testHelper.getSingleActivityInstanceBeforeMigration(miBodyOf("userTask")).getId())
            .activity("userTask", userTaskInstances[0].getId())
            .activity("userTask", userTaskInstances[1].getId())
            .transition("userTask") // bug CAM-5609
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
  public void testMigrateParallelMiBodyRemoveSubprocess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("subProcess"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures(miBodyOf("subProcess"),
          "Cannot remove the inner activity of a multi-instance body when the body is mapped"
        );
    }
  }


  @Test
  public void testMigrateParallelMiBodyAddSubprocess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_SUBPROCESS_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("subProcess"))
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures(miBodyOf("userTask"),
          "Must map the inner activity of a multi-instance body when the body is mapped"
        );
    }
  }

  @Test
  public void testMigrateSequentialMultiInstanceTask() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration(miBodyOf("userTask")))
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginMiBody("userTask", testHelper.getSingleActivityInstanceBeforeMigration(miBodyOf("userTask")).getId())
            .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    Task migratedTask = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertNotNull(migratedTask);
    assertEquals(targetProcessDefinition.getId(), migratedTask.getProcessDefinitionId());

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.completeTask("userTask");
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }

  @Test
  public void testMigrateSequentialMultiInstanceTasksVariables() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();

    // when
    testHelper.createProcessInstanceAndMigrate(migrationPlan);

    // then
    Task task = testHelper.snapshotAfterMigration.getTaskForKey("userTask");
    Assert.assertEquals(3, rule.getTaskService().getVariable(task.getId(), NUMBER_OF_INSTANCES));
    Assert.assertEquals(1, rule.getTaskService().getVariable(task.getId(), NUMBER_OF_ACTIVE_INSTANCES));
    Assert.assertEquals(0, rule.getTaskService().getVariable(task.getId(), NUMBER_OF_COMPLETED_INSTANCES));
    Assert.assertEquals(0, rule.getTaskService().getVariable(task.getId(), NUMBER_OF_COMPLETED_INSTANCES));
  }

  @Test
  public void testMigrateSequentialMultiInstancePartiallyComplete() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();

    // when
    ProcessInstance processInstance =
        rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    testHelper.completeAnyTask("userTask");
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask").scope().id(testHelper.getSingleExecutionIdForActivityBeforeMigration(miBodyOf("userTask")))
          .done());

    testHelper.assertActivityTreeAfterMigration().hasStructure(
        describeActivityInstanceTree(targetProcessDefinition.getId())
          .beginMiBody("userTask", testHelper.getSingleActivityInstanceBeforeMigration(miBodyOf("userTask")).getId())
            .activity("userTask", testHelper.getSingleActivityInstanceBeforeMigration("userTask").getId())
        .done());

    // and it is possible to successfully complete the migrated instance
    testHelper.completeTask("userTask");
    testHelper.completeTask("userTask");
    testHelper.assertProcessEnded(testHelper.snapshotBeforeMigration.getProcessInstanceId());
  }


  @Test
  public void testMigrateSequenatialMiBodyRemoveSubprocess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("subProcess"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures(miBodyOf("subProcess"),
          "Cannot remove the inner activity of a multi-instance body when the body is mapped"
        );
    }
  }


  @Test
  public void testMigrateSequentialMiBodyAddSubprocess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_SUBPROCESS_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("subProcess"))
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures(miBodyOf("userTask"),
          "Must map the inner activity of a multi-instance body when the body is mapped"
        );
    }
  }

  @Test
  public void testMigrateParallelToSequential() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities(miBodyOf("userTask"), miBodyOf("userTask"))
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures(miBodyOf("userTask"),
          "Activities have incompatible types (ParallelMultiInstanceActivityBehavior is not "
          + "compatible with SequentialMultiInstanceActivityBehavior)"
        );
    }
  }

  protected String miBodyOf(String activityId) {
    return activityId + BpmnParse.MULTI_INSTANCE_BODY_ID_SUFFIX;
  }

}
