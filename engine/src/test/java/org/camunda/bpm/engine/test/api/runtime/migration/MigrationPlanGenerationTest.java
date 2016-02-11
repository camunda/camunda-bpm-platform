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

import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.assertThat;
import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.migrate;

import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.MigrationPlanAssert;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.SubProcess;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanGenerationTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
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
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("outerSubProcess")
      .builder()
        .id("subProcess")  // make ID match with subprocess ID of source definition
        .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  public void testMapEqualActivitiesToSurroundingSubProcessScope() {
    BpmnModelInstance sourceProcess = ProcessModels.SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
        .<SubProcess>getModelElementById("innerSubProcess")
        .builder()
          .id("subProcess")  // make ID match with subprocess ID of source definition
          .done();

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
    BpmnModelInstance targetProcess = ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .id("userTask3")
      .moveToActivity("userTask2")
      .id("userTask1")
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("subProcess2").to("subProcess2")
      );
  }

  @Test
  public void testMapEqualActivitiesToNestedSiblingScope() {
    BpmnModelInstance sourceProcess = ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .id("userTask3")
      .moveToActivity("userTask2")
      .id("userTask1")
      .done();

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
    BpmnModelInstance testProcess = ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
        .multiInstance().parallel().cardinality("3").multiInstanceDone().done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasEmptyInstructions();
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
    BpmnModelInstance sourceProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("outerSubProcess").builder()
      .id("subProcess")
      .done();
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
    BpmnModelInstance targetProcess = ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("outerSubProcess").builder()
      .id("newOuterSubProcess")
      .moveToActivity("innerSubProcess")
      .id("newInnerSubProcess")
      .done();

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
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message("Message")
      .done();
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS;

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToTaskWithBoundaryEvent() {
    BpmnModelInstance sourceProcess = ProcessModels.ONE_TASK_PROCESS;
    BpmnModelInstance targetProcess = ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message("Message")
      .done();

    assertGeneratedMigrationPlan(sourceProcess, targetProcess)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesWithBoundaryEvent() {
    BpmnModelInstance testProcess = ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .boundaryEvent().message("Message")
      .done();

    assertGeneratedMigrationPlan(testProcess, testProcess)
      .hasEmptyInstructions();
  }

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
