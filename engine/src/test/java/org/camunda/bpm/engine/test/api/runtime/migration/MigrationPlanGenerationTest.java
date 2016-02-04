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
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesInSameSubProcessScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );

  }

  @Test
  public void testMapEqualActivitiesToSubProcessScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToNestedSubProcessScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("outerSubProcess")
      .builder()
        .id("subProcess")  // make ID match with subprocess ID of source definition
        .done());

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  public void testMapEqualActivitiesToSurroundingSubProcessScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
        .<SubProcess>getModelElementById("innerSubProcess")
        .builder()
          .id("subProcess")  // make ID match with subprocess ID of source definition
          .done());

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToDeeplyNestedSubProcessScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToSiblingScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_SUBPROCESS_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .id("userTask3")
      .moveToActivity("userTask2")
      .id("userTask1")
      .done()
    );

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("subProcess2").to("subProcess2")
      );
  }

  @Test
  public void testMapEqualActivitiesToNestedSiblingScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_DOUBLE_SUBPROCESS_PROCESS.clone()
      .<UserTask>getModelElementById("userTask1").builder()
      .id("userTask3")
      .moveToActivity("userTask2")
      .id("userTask1")
      .done()
    );

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess1").to("subProcess1"),
        migrate("nestedSubProcess1").to("nestedSubProcess1"),
        migrate("subProcess2").to("subProcess2"),
        migrate("nestedSubProcess2").to("nestedSubProcess2")
      );
  }

  @Test
  public void testMapEqualActivitiesWhichBecomeScope() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SCOPE_TASK_PROCESS);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMapEqualActivitiesWithParallelMultiInstance() {
    // given
    BpmnModelInstance testProcess = ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
        .multiInstance().parallel().cardinality("3").multiInstanceDone().done();
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesIgnoreUnsupportedActivities() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.UNSUPPORTED_ACTIVITIES);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.UNSUPPORTED_ACTIVITIES);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToParentScope() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("outerSubProcess").builder()
      .id("subProcess")
      .done()
    );
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess")
      );
  }

  @Test
  public void testMapEqualActivitiesFromScopeToProcessDefinition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromDoubleScopeToProcessDefinition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromTripleScopeToProcessDefinition() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.TRIPLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromTripleScopeToSingleNewScope() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.TRIPLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesFromTripleScopeToTwoNewScopes() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.TRIPLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesToNewScopes() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.DOUBLE_SUBPROCESS_PROCESS.clone()
      .<SubProcess>getModelElementById("outerSubProcess").builder()
      .id("newOuterSubProcess")
      .moveToActivity("innerSubProcess")
      .id("newInnerSubProcess")
      .done()
    );

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasEmptyInstructions();
  }

  @Test
  public void testMapEqualActivitiesOutsideOfScope() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask1").to("userTask1")
      );
  }

  @Test
  public void testMapEqualActivitiesToHorizontalScope() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_TASK_AND_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask1").to("userTask1")
      );
  }

}
