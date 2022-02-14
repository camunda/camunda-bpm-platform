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

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.test.api.runtime.migration.ModifiableBpmnModelInstance.modify;
import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.assertThat;
import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.migrate;
import static org.camunda.bpm.engine.test.util.MigrationPlanAssert.variable;
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.EventSubProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.camunda.bpm.engine.variable.value.ObjectValue;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanCreationTest {

  public static final String MESSAGE_NAME = "Message";
  public static final String SIGNAL_NAME = "Signal";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
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

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

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
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan("aNonExistingProcDefId", processDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "Source process definition with id 'aNonExistingProcDefId' does not exist");
    }
  }

  @Test
  public void testMigrateNullSourceDefinition() {
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(null, processDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "Source process definition id is null");
    }
  }

  @Test
  public void testMigrateNonExistingTargetDefinition() {
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    try {
      runtimeService
        .createMigrationPlan(processDefinition.getId(), "aNonExistingProcDefId")
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "Target process definition with id 'aNonExistingProcDefId' does not exist");
    }
  }

  @Test
  public void testMigrateNullTargetDefinition() {
    ProcessDefinition processDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(processDefinition.getId(), null)
        .mapActivities("userTask", "userTask")
        .build();
      fail("Should not succeed");
    } catch (BadUserRequestException e) {
      assertExceptionMessage(e, "Target process definition id is null");
    }
  }

  @Test
  public void testMigrateNonExistingSourceActivityId() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

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
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

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
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

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
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

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
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);

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

    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_RECEIVE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "receiveTask")
        .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Activities have incompatible types (UserTaskActivityBehavior is not compatible with ReceiveTaskActivityBehavior)"
        );
    }
  }

  @Test
  public void testNotMigrateActivitiesOfDifferentType() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.SUBPROCESS_PROCESS)
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
        .hasInstructionFailures("userTask", "Activities have incompatible types (UserTaskActivityBehavior is not "
            + "compatible with SubProcessActivityBehavior)");
    }
  }

  @Test
  public void testNotMigrateBoundaryEventsOfDifferentType() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
      .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done()
    );
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(modify(ProcessModels.ONE_TASK_PROCESS)
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
        .hasInstructionFailures("boundary", "Events are not of the same type (boundaryMessage != boundarySignal)");
    }
  }

  @Test
  public void testMigrateSubProcessToProcessDefinition() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

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
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

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
        + "that is not mapped from the source process definition."
        );
    }
  }

  @Test
  public void testMapEqualBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

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

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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
  public void testMapBoundaryToParallelActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask1")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .activityBuilder("userTask2")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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
        .hasInstructionFailures("boundary",
          "The source activity's event scope (userTask1) must be mapped to the target activity's event scope (userTask2)"
        );
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

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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
  public void testMapBoundaryToChildActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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
        .hasInstructionFailures("boundary",
          "The source activity's event scope (subProcess) must be mapped to the target activity's event scope (userTask)"
        );
    }
  }

  @Test
  public void testMapBoundaryToParentActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("userTask")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("boundary").message(MESSAGE_NAME)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(targetProcess);

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
        .hasInstructionFailures("boundary",
          "The source activity's event scope (userTask) must be mapped to the target activity's event scope (subProcess)",
          "The closest mapped ancestor 'subProcess' is mapped to scope 'subProcess' which is not an ancestor of target scope 'boundary'"
        );
    }
  }

  @Test
  public void testMapAllBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .activityBuilder("subProcess")
        .boundaryEvent("error").error(ERROR_CODE)
      .moveToActivity("subProcess")
        .boundaryEvent("escalation").escalation(ESCALATION_CODE)
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("error", "error")
      .mapActivities("escalation", "escalation")
      .mapActivities("userTask", "userTask")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("error").to("error"),
        migrate("escalation").to("escalation"),
        migrate("userTask").to("userTask")
      );

  }

  @Test
  public void testMapProcessDefinitionWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessTo(ProcessModels.PROCESS_KEY)
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

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
  public void testMapSubProcessWithEventSubProcess() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.NESTED_EVENT_SUB_PROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.NESTED_EVENT_SUB_PROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("subProcess", "subProcess")
      .mapActivities("userTask", "userTask")
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("subProcess").to("subProcess"),
        migrate("userTask").to("userTask")
      );
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

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

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
  public void testMapUserTaskInEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessTo("subProcess")
      .triggerByEvent()
      .embeddedSubProcess()
      .startEvent().message(MESSAGE_NAME)
      .userTask("innerTask")
      .endEvent()
      .subProcessDone()
      .done();

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(testProcess);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask")
        .mapActivities("innerTask", "innerTask")
        .build();

      assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasInstructions(
          migrate("userTask").to("userTask"),
          migrate("innerTask").to("innerTask")
        );
  }

  @Test
  public void testNotMapActivitiesMoreThanOnce() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.PARALLEL_GATEWAY_PROCESS);

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

  @Test
  public void testCannotUpdateEventTriggerForNonEvent() {

    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask", "userTask").updateEventTrigger()
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Cannot update event trigger because the activity does not define a persistent event trigger"
        );
    }
  }

  @Test
  public void testCannotUpdateEventTriggerForEventSubProcess() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("eventSubProcess", "eventSubProcess").updateEventTrigger()
        .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("eventSubProcess",
          "Cannot update event trigger because the activity does not define a persistent event trigger"
        );
    }
  }

  @Test
  public void testCanUpdateEventTriggerForEventSubProcessStartEvent() {
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(EventSubProcessModels.TIMER_EVENT_SUBPROCESS_PROCESS);

    MigrationPlan migrationPlan = runtimeService
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("eventSubProcessStart", "eventSubProcessStart").updateEventTrigger()
      .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasInstructions(
        migrate("eventSubProcessStart").to("eventSubProcessStart").updateEventTrigger(true)
      );
  }

  @Test
  public void shouldSetVariable() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Collections.singletonMap("foo", "bar"))
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasVariables(
            variable().name("foo").value("bar")
        );
  }

  @Test
  public void shouldSetVariables() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    Map<String, Object> variables = new HashMap<>();
    variables.put("foo", "bar");
    variables.put("bar", 5);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(variables)
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasVariables(
            variable().name("foo").value("bar"),
            variable().name("bar").value(5)
        );
  }

  @Test
  public void shouldSetUntypedVariable() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Variables.putValue("foo", "bar"))
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
      .hasSourceProcessDefinition(sourceProcessDefinition)
      .hasTargetProcessDefinition(targetProcessDefinition)
      .hasVariables(
          variable().name("foo").value("bar")
      );
  }

  @Test
  public void shouldSetUntypedVariables() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValue("foo", "bar")
                .putValue("bar", 5)
        )
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasVariables(
            variable().name("foo").value("bar"),
            variable().name("bar").value(5)
        );
  }

  @Test
  public void shouldSetMapOfTypedVariable() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(Collections.singletonMap("foo", Variables.stringValue("bar")))
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasVariables(
            variable().name("foo").value("bar").typed()
        );
  }

  @Test
  public void shouldSetVariableMapOfTypedVariable() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValueTyped("foo", Variables.stringValue("bar"))
        )
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasVariables(
            variable().name("foo").value("bar").typed()
        );
  }

  @Test
  public void shouldSetTypedAndUntypedVariables() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(
            Variables.putValue("foo", "bar")
                .putValueTyped("bar", Variables.integerValue(5))
        )
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .hasVariables(
            variable().name("foo").value("bar"),
            variable().name("bar").value(5).typed()
        );
  }

  @Test
  public void shouldSetNullVariables() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(null)
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .variablesNull();
  }

  @Test
  public void shouldSetEmptyVariables() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .setVariables(new HashMap<>())
        .mapEqualActivities()
        .build();

    assertThat(migrationPlan)
        .hasSourceProcessDefinition(sourceProcessDefinition)
        .hasTargetProcessDefinition(targetProcessDefinition)
        .variablesEmpty();
  }

  @Test
  public void shouldThrowValidationExceptionDueToSerializationFormatForbiddenForVariable() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    try {
      runtimeService
          .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
          .setVariables(
              Variables.putValueTyped("foo",
                  Variables.serializedObjectValue()
                      .serializedValue("[]")
                      .objectTypeName(ArrayList.class.getName())
                      .serializationDataFormat(Variables.SerializationDataFormats.JAVA.getName())
                      .create())
          )
          .mapEqualActivities()
          .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
          .hasVariableFailures("foo",
              "Cannot set variable with name foo. Java serialization format is prohibited");
    }
  }

  @Test
  public void shouldThrowValidationExceptionDueToSerializationFormatForbiddenForVariables() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    ObjectValue objectValue = Variables.serializedObjectValue()
        .serializedValue("[]")
        .objectTypeName(ArrayList.class.getName())
        .serializationDataFormat(Variables.SerializationDataFormats.JAVA.getName())
        .create();

    try {
      runtimeService
          .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
          .setVariables(
              Variables.putValueTyped("foo", objectValue)
              .putValueTyped("bar", objectValue)
          )
          .mapEqualActivities()
          .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
          .hasVariableFailures("foo",
              "Cannot set variable with name foo. Java serialization format is prohibited")
          .hasVariableFailures("bar",
              "Cannot set variable with name bar. Java serialization format is prohibited");
    }
  }

  @Test
  public void shouldThrowExceptionDueToInstructionsAndSerializationFormatForbiddenForVariable() {
    ProcessDefinition sourceProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition =
        testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    ObjectValue objectValue = Variables.serializedObjectValue()
        .serializedValue("[]")
        .objectTypeName(ArrayList.class.getName())
        .serializationDataFormat(Variables.SerializationDataFormats.JAVA.getName())
        .create();

    try {
      runtimeService
          .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
          .setVariables(Variables.putValueTyped("foo", objectValue))
          .mapActivities("foo", "bar")
          .mapActivities("bar", "foo")
          .build();
      fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
          .hasVariableFailures("foo",
              "Cannot set variable with name foo. Java serialization format is prohibited")
          .hasInstructionFailures("foo",
              "Source activity 'foo' does not exist", "Target activity 'bar' does not exist")
          .hasInstructionFailures("bar",
              "Source activity 'bar' does not exist", "Target activity 'foo' does not exist");
    }
  }

  protected void assertExceptionMessage(Exception e, String message) {
    assertThat(e.getMessage()).contains(message);
  }

}
