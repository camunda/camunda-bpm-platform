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
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.assertThat;
import static org.camunda.bpm.engine.test.util.ActivityInstanceAssert.describeActivityInstanceTree;
import static org.camunda.bpm.engine.test.util.ExecutionAssert.describeExecutionTree;
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;

import org.camunda.bpm.engine.delegate.ExecutionListener;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ActivityInstance;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.CompensationModels;
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
public class MigrationCompensationAddSubProcessTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testCase1() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_TWO_TASKS_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("compensationHandler", "compensationHandler", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  /**
   * The guarantee given by the API is: Compensation can be triggered in the scope that it could be triggered before
   *   migration. Thus, it should not be possible to trigger compensation from the new sub process instance but only from the
   *   parent scope, i.e. the process definition instance
   */
  @Test
  public void testCase1CannotTriggerCompensationInNewScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(
        modify(CompensationModels.COMPENSATION_TWO_TASKS_SUBPROCESS_MODEL)
        .endEventBuilder("subProcessEnd")
          .compensateEventDefinition()
          .waitForCompletion(true)
          .compensateEventDefinitionDone()
        .done());

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then compensation is only caught outside of the subProcess
    testHelper.completeTask("userTask2");

    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("compensationEvent") // note: this is not subProcess and subProcessEnd
        .beginScope("subProcess")
          .activity("compensationHandler")
      .done());
  }

  @Test
  public void testCase1AssertExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_TWO_TASKS_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then the execution tree is correct
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree(null).scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("userTask2").scope().up()
          .child("subProcess").scope().eventScope()
        .done());
  }

  @Test
  public void testCase2() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertEventSubscriptionMigrated("compensationHandler", "compensationHandler", null);

    // and the compensation can be triggered and completed
    testHelper.completeTask("userTask2");
    testHelper.completeTask("compensationHandler");

    testHelper.assertProcessEnded(processInstance.getId());
  }

  @Test
  public void testCase2AssertExecutionTree() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    testHelper.assertExecutionTreeAfterMigration()
      .hasProcessDefinitionId(targetProcessDefinition.getId())
      .matches(
        describeExecutionTree("userTask2").scope().id(testHelper.snapshotBeforeMigration.getProcessInstanceId())
          .child("subProcess").scope().eventScope()
        .done());
  }

  @Test
  public void testCase2AssertActivityInstance() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // when throwing compensation
    testHelper.completeTask("userTask2");

    // then the activity instance tree is correct
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(processInstance.getId());

    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(targetProcessDefinition.getId())
        .activity("compensationEvent")
        .beginScope("subProcess")
          .activity("compensationHandler")
      .done());
  }

  @Test
  public void testNoListenersCalled() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
      .activityBuilder("subProcess")
        .camundaExecutionListenerExpression(
            ExecutionListener.EVENTNAME_START,
            "${execution.setVariable('foo', 'bar')}")
      .done());

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    Assert.assertEquals(0, testHelper.snapshotAfterMigration.getVariables().size());
  }

  @Ignore("CAM-6035")
  @Test
  public void testNoInputMappingExecuted() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.ONE_COMPENSATION_TASK_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL)
      .activityBuilder("subProcess")
        .camundaInputParameter("foo", "bar")
      .done());

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask2", "userTask2")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());
    testHelper.completeTask("userTask1");

    // when
    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // then
    Assert.assertEquals(0, testHelper.snapshotAfterMigration.getVariables().size());
  }

  @Test
  public void testVariablesInParentEventScopeStillAccessible() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_ONE_TASK_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.DOUBLE_SUBPROCESS_MODEL);

    MigrationPlan migrationPlan = rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "outerSubProcess")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .mapActivities("userTask2", "userTask2")
        .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    Execution subProcessExecution = rule.getRuntimeService().createExecutionQuery().activityId("userTask1").singleResult();
    rule.getRuntimeService().setVariableLocal(subProcessExecution.getId(), "foo", "bar");

    testHelper.completeTask("userTask1");

    testHelper.migrateProcessInstance(migrationPlan, processInstance);

    // when throwing compensation
    testHelper.completeAnyTask("userTask2");

    // then the variable snapshot is available
    Task compensationTask = rule.getTaskService().createTaskQuery().singleResult();
    Assert.assertEquals("bar", rule.getTaskService().getVariable(compensationTask.getId(), "foo"));
  }

  @Test
  public void testCannotAddScopeOnTopOfEventSubProcess() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(CompensationModels.COMPENSATION_EVENT_SUBPROCESS_MODEL);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(modify(CompensationModels.DOUBLE_SUBPROCESS_MODEL)
        .addSubProcessTo("innerSubProcess")
        .id("eventSubProcess")
        .triggerByEvent()
        .embeddedSubProcess()
        .startEvent("eventSubProcessStart")
          .compensateEventDefinition()
          .compensateEventDefinitionDone()
        .endEvent()
        .done());


    try {
      // when
      rule.getRuntimeService().createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "outerSubProcess")
        .mapActivities("eventSubProcessStart", "eventSubProcessStart")
        .mapActivities("compensationBoundary", "compensationBoundary")
        .mapActivities("userTask2", "userTask2")
        .build();
      Assert.fail("exception expected");
    } catch (MigrationPlanValidationException e) {
      // then
      assertThat(e.getValidationReport())
        .hasInstructionFailures("eventSubProcessStart",
          "The source activity's event scope (subProcess) must be mapped to the target activity's event scope (innerSubProcess)"
        );
    }
  }

}
