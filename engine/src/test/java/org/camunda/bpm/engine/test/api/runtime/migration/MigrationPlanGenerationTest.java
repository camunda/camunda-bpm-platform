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
import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.assertThat;
import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.migrate;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MultiInstanceProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.MigrationPlanAssert;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanGenerationTest {

  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  protected ProcessEngineRule rule = new ProcessEngineRule(true);
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testMapEqualActivitiesInProcessDefinitionScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesInSameSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );

  }

  @Test
  public void testMapEqualActivitiesToSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToNestedSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("outerSubProcess", "subProcess"); // make ID match with subprocess ID of source definition

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  public void testMapEqualActivitiesToSurroundingSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("innerSubProcess", "subProcess"); // make ID match with subprocess ID of source definition

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToDeeplyNestedSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToSiblingScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_SUBPROCESS_PROCESS)
      .swapElementIds("userTask1", "userTask2");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("subProcess2").to("subProcess2")
      );
  }

  @Test
  public void testMapEqualActivitiesToNestedSiblingScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS)
      .swapElementIds("userTask1", "userTask2");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("nestedSubProcess1").to("nestedSubProcess1"),
        migrate("subProcess2").to("subProcess2"),
        migrate("nestedSubProcess2").to("nestedSubProcess2")
      );
  }

  @Test
  public void testMapEqualActivitiesWhichBecomeScope() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SCOPE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesWithParallelMultiInstance() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .<UserTask>getModelElementById("userTask").builder()
        .multiInstance().parallel().cardinality("3").multiInstanceDone().done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("userTask#multiInstanceBody").to("userTask#multiInstanceBody"));
  }

  @Test
  public void testMapEqualActivitiesIgnoreUnsupportedActivities() {
    BpmnModelInstance sourceProcess = ProcessModels.UNSUPPORTED_ACTIVITIES;
    BpmnModelInstance targetProcess = ProcessModels.UNSUPPORTED_ACTIVITIES;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToParentScope() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("outerSubProcess", "subProcess");
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  public void testMapEqualActivitiesFromScopeToProcessDefinition() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromDoubleScopeToProcessDefinition() {
    BpmnModelInstance sourceProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromTripleScopeToProcessDefinition() {
    BpmnModelInstance sourceProcess = ProcessModels.TRIPLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromTripleScopeToSingleNewScope() {
    BpmnModelInstance sourceProcess = ProcessModels.TRIPLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromTripleScopeToTwoNewScopes() {
    BpmnModelInstance sourceProcess = ProcessModels.TRIPLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToNewScopes() {
    BpmnModelInstance sourceProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.DOUBLE_SUBPROCESS_PROCESS)
      .changeElementId("outerSubProcess", "newOuterSubProcess")
      .changeElementId("innerSubProcess", "newInnerSubProcess");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesOutsideOfScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask1").to("userTask1")
      );
  }

  @Test
  public void testMapEqualActivitiesToHorizontalScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask1").to("userTask1")
      );
  }

  @Test
  public void testMapEqualActivitiesFromTaskWithBoundaryEvent() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent(null).message("Message")
      .done();
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesToTaskWithBoundaryEvent() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent(null).message("Message")
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesWithBoundaryEvent() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("messageBoundary").message(MESSAGE_NAME)
      .moveToActivity("userTask")
        .boundaryEvent("signalBoundary").signal(SIGNAL_NAME)
      .moveToActivity("userTask")
        .boundaryEvent("timerBoundary").timerWithDate(TIMER_DATE)
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("messageBoundary").to("messageBoundary"),
        migrate("userTask").to("userTask"),
        migrate("signalBoundary").to("signalBoundary"),
        migrate("timerBoundary").to("timerBoundary")
      );
  }

  @Test
  public void testNotMapBoundaryEventsWithDifferentIds() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId("message", "newMessage");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testIgnoreNotSupportedBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("messageBoundary").message(MESSAGE_NAME)
      .moveToActivity("subProcess")
        .boundaryEvent("errorBoundary").error(ERROR_CODE)
      .moveToActivity("subProcess")
        .boundaryEvent("escalationBoundary").escalation(ESCALATION_CODE)
      .moveToActivity("userTask")
        .boundaryEvent("signalBoundary").signal(SIGNAL_NAME)
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("messageBoundary").to("messageBoundary"),
        migrate("userTask").to("userTask"),
        migrate("signalBoundary").to("signalBoundary")
      );
  }

  @Test
  public void testNotMigrateBoundaryToParallelActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask2")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask1").to("userTask1"),
        migrate("userTask2").to("userTask2")
      );
  }

  @Test
  public void testNotMigrateBoundaryToChildActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("message").message(MESSAGE_NAME)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testNotMigrateProcessInstanceWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .triggerByEvent()
      .embeddedSubProcess()
        .startEvent().message(MESSAGE_NAME)
        .endEvent()
      .subProcessDone()
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasEmptyInstructions();

    assertGeneratedMigrationPlan(testProcess, ProcessModels.ONE_TASK_PROCESS)
      .hasEmptyInstructions();

    assertGeneratedMigrationPlan(ProcessModels.ONE_TASK_PROCESS, testProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testNotMigrateSubProcessWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .endEvent()
      .subProcessDone()
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasEmptyInstructions();

    assertGeneratedMigrationPlan(testProcess, ProcessModels.SUBPROCESS_PROCESS)
      .hasEmptyInstructions();

    assertGeneratedMigrationPlan(ProcessModels.SUBPROCESS_PROCESS, testProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testNotMigrateUserTaskInEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .userTask("innerTask")
      .endEvent()
      .subProcessDone()
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasEmptyInstructions();

    assertGeneratedMigrationPlan(testProcess, ProcessModels.SUBPROCESS_PROCESS)
      .hasEmptyInstructions();

    assertGeneratedMigrationPlan(ProcessModels.SUBPROCESS_PROCESS, testProcess)
      .hasEmptyInstructions();

  }

  @Test
  public void testNotMigrateActivitiesOfDifferentType() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .swapElementIds("userTask", "subProcess");

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testNotMigrateBoundaryEventsOfDifferentType() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").signal(SIGNAL_NAME)
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testNotMigrateMultiInstanceOfDifferentType() {
    BpmnModelInstance sourceProcess = MultiInstanceProcessModels.SEQ_MI_ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testNotMigrateBoundaryEventsWithInvalidEventScopeInstruction() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
        .activityBuilder("userTask")
        .boundaryEvent("boundary")
        .message("foo")
        .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_RECEIVE_TASK_PROCESS)
        .changeElementId("receiveTask", "userTask")
        .activityBuilder("userTask")
        .boundaryEvent("boundary")
        .message("foo")
        .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }


  // helper

  protected MigrationPlanAssert assertGeneratedMigrationPlan(BpmnModelInstance sourceProcess, BpmnModelInstance targetProcess) {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition);

    return assertThat(migrationPlan);
  }

}
