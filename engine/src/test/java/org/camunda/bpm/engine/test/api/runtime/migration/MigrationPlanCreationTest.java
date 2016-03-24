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
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanCreationTest {

  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  protected ProcessEngineRule rule = new ProcessEngineRule(true);
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  protected RuntimeService runtimeService;

  @Before
  public void initServices() {
    runtimeService = rule.getRuntimeService();
  }

  @Test
  public void testExplicitInstructionGeneration() {

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMigrateNonExistingSourceDefinition() {
    ProcessDefinition processDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan("aNonExistingProcDefId", processDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "source process definition with id aNonExistingProcDefId does not exist");
    }
  }

  @Test
  public void testMigrateNullSourceDefinition() {
    ProcessDefinition processDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(null, processDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "sourceProcessDefinitionId is null");
    }
  }

  @Test
  public void testMigrateNonExistingTargetDefinition() {
    ProcessDefinition processDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    try {
      runtimeService
        .createMigrationPlan(processDefinition.getId(), "aNonExistingProcDefId")
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "target process definition with id aNonExistingProcDefId does not exist");
    }
  }

  @Test
  public void testMigrateNullTargetDefinition() {
    ProcessDefinition processDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(processDefinition.getId(), null)
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "targetProcessDefinitionId is null");
    }
  }

  @Test
  public void testMigrateNonExistingSourceActivityId() {
    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("thisActivityDoesNotExist", "userTask")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("thisActivityDoesNotExist", "Source activity 'thisActivityDoesNotExist' does not exist");
    }
  }

  @Test
  public void testMigrateNullSourceActivityId() {
    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities(null, "userTask")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures(null, "Source activity id is null");
    }
  }

  @Test
  public void testMigrateNonExistingTargetActivityId() {
    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "thisActivityDoesNotExist")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask", "Target activity 'thisActivityDoesNotExist' does not exist");
    }
  }

  @Test
  public void testMigrateNullTargetActivityId() {
    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", null)
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask", "Target activity id is null");
    }
  }

  @Test
  public void testMigrateTaskToHigherScope() {
    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceDefinition)
      .hasTargetProcessDefinition(targetDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testMigrateToUnsupportedActivityType() {

    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.ONE_RECEIVE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "receiveTask")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Activities are of different type which is not supported by the migration (userTask != receiveTask)"
        );
    }
  }

  @Test
  public void testNotMigrateActivitiesOfDifferentType() {
    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(modify(ProcessModels.SUBPROCESS_PROCESS)
      .swapElementIds("userTask", "subProcess")
    );

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask", "Activities are of different type which is not supported by the migration (userTask != subProcess)");
    }
  }

  @Test
  public void testNotMigrateBoundaryEventsOfDifferentType() {
    ProcessDefinition sourceDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
      .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done()
    );
    ProcessDefinition targetDefinition = testHelper.deploy(modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
      .boundaryEvent("boundary").signal(SIGNAL_NAME)
      .done()
    );

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "userTask")
        .mapActivities("boundary", "boundary")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("boundary", "Activities are of different type which is not supported by the migration (boundaryMessage != boundarySignal)");
    }
  }

  @Test
  public void testMigrateSubProcessToProcessDefinition() {
    ProcessDefinition sourceDefinition = testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("subProcess", targetDefinition.getId())
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("subProcess", "Target activity '" + targetDefinition.getId() + "' does not exist");
    }
  }

  @Test
  public void testMapEqualActivitiesWithParallelMultiInstance() {
    // given
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .getBuilderForElementById("userTask", UserTaskBuilder.class)
      .multiInstance().parallel().cardinality("3").multiInstanceDone().done();
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    // when
    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Target activity 'userTask' is a descendant of multi-instance body 'userTask#multiInstanceBody' "
        + "that is not migrated from the source process definition."
        );
    }
  }

  @Test
  public void testMapEqualBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("boundary", "boundary")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("boundary").to("boundary")
      );
  }

  @Test
  public void testMapBoundaryEventsWithDifferentId() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId("boundary", "newBoundary");

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("boundary", "newBoundary")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("boundary").to("newBoundary")
      );
  }

  @Test
  public void testMapBoundaryToMigratedActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(sourceProcess)
      .changeElementId("userTask", "newUserTask");

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "newUserTask")
      .mapActivities("boundary", "boundary")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("newUserTask"),
        migrate("boundary").to("boundary")
      );
  }

  @Test
  public void testNotMapBoundaryToParallelActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask2")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask1", "userTask1")
        .mapActivities("userTask2", "userTask2")
        .mapActivities("boundary", "boundary")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("boundary", "Event scope of the activity has changed and wasn't migrated");
    }
  }

  @Test
  public void testMapBoundaryToHigherScope() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("boundary", "boundary")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("boundary").to("boundary")
      );
  }

  @Test
  public void testMapBoundaryToLowerScope() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .mapActivities("boundary", "boundary")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask"),
        migrate("boundary").to("boundary")
      );
  }

  @Test
  public void testNotMapBoundaryToChildActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask", "userTask")
        .mapActivities("boundary", "boundary")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("boundary", "Event scope of the activity has changed and wasn't migrated");
    }
  }

  @Test
  public void testNotMapBoundaryToParentActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask", "userTask")
        .mapActivities("boundary", "boundary")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("boundary", "Event scope of the activity has changed and wasn't migrated");
    }
  }

  @Test
  public void testNotMapUnsupportedBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("error").error(ERROR_CODE)
      .moveToActivity("subProcess")
        .boundaryEvent("escalation").escalation(ESCALATION_CODE)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("error", "error")
        .mapActivities("escalation", "escalation")
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("error",
          "Type of the source boundary event 'error' is not supported by migration",
          "Type of the target boundary event 'error' is not supported by migration"
        )
        .hasInstructionFailures("escalation",
          "Type of the source boundary event 'escalation' is not supported by migration",
          "Type of the target boundary event 'escalation' is not supported by migration"
        );
    }

  }

  @Test
  public void testNotMapProcessDefinitionWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Source activity 'userTask' has an event sub process child",
          "Target activity 'userTask' has an event sub process child"
        );
    }
  }

  @Test
  public void testNotMapSubProcessWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("subProcess",
          "Source activity 'subProcess' has an event sub process child",
          "Target activity 'subProcess' has an event sub process child"
        );
    }
  }

  @Test
  public void testMapActivityWithUnmappedParentWhichHasAEventSubProcessChild() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("userTask").to("userTask")
      );
  }

  @Test
  public void testNotMapUserTaskInEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .userTask("innerTask")
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask")
        .mapActivities("innerTask", "innerTask")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("innerTask",
          "Source activity 'innerTask' is child of an event sub process",
          "Target activity 'innerTask' is child of an event sub process"
        );
    }
  }

  @Test
  public void testNotMapActivitiesMoreThanOnce() {
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.PARALLEL_GATEWAY_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask1", "userTask1")
        .mapActivities("userTask1", "userTask2")
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask1",
          "There are multiple mappings for source activity id 'userTask1'",
          "There are multiple mappings for source activity id 'userTask1'"
        );
    }
  }

  protected void assertExceptionMessage(Exception e, String message) {
    assertThat(e.getMessage(), CoreMatchers.containsString(message));
  }

}
