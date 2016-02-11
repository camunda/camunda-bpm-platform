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

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.builder.UserTaskBuilder;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
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
  public static final String TIMER_DATE = "2016-02-11T12:13:14Z";
  public static final String ERROR_CODE = "Error";
  public static final String ESCALATION_CODE = "Escalation";

  protected ProcessEngineRule rule = new ProcessEngineRule();
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
      Assert.fail("Should not succeed");
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
      Assert.fail("Should not succeed");
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
      Assert.fail("Should not succeed");
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
      Assert.fail("Should not succeed");
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
      Assert.fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("thisActivityDoesNotExist", "the mapped activities are either null or not supported");
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
      Assert.fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure(null, "the mapped activities are either null or not supported");
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
      Assert.fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("userTask", "the mapped activities are either null or not supported");
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
      Assert.fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("userTask", "the mapped activities are either null or not supported");
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
  public void testMigrateToDifferentActivityType() {

    ProcessDefinition sourceDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deploy(ProcessModels.ONE_RECEIVE_TASK_PROCESS);

    try {
      runtimeService
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "receiveTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("userTask", "the mapped activities are either null or not supported");
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
      Assert.fail("Should not succeed");
    } catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("subProcess", "the mapped activities are either null or not supported");
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
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("userTask", "the mapped activities are either null or not supported");
    }
  }

  @Test
  public void testMapEqualBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);

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
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);
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
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);
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
      .addMessageBoundaryEvent("userTask1", "boundary", MESSAGE_NAME);
    BpmnModelInstance targetProcess = modify(ProcessModels.PARALLEL_GATEWAY_PROCESS)
      .addMessageBoundaryEvent("userTask2", "boundary", MESSAGE_NAME);

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("userTask1", "userTask1")
        .mapActivities("userTask2", "userTask2")
        .mapActivities("boundary", "boundary")
        .build();
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("boundary", "the mapped boundary event has be migrated together with the activity it is attached to");
    }
  }

  @Test
  public void testMapBoundaryToHigherScope() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);

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
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);
    BpmnModelInstance targetProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);

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
      .addMessageBoundaryEvent("subProcess", "boundary", MESSAGE_NAME);
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask", "userTask")
        .mapActivities("boundary", "boundary")
        .build();
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("boundary", "the mapped boundary event has be migrated together with the activity it is attached to");
    }
  }

  @Test
  public void testNotMapBoundaryToParentActivity() {
    BpmnModelInstance sourceProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEvent("userTask", "boundary", MESSAGE_NAME);
    BpmnModelInstance targetProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addMessageBoundaryEvent("subProcess", "boundary", MESSAGE_NAME);

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(sourceProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(targetProcess);

    try {
      runtimeService
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("subProcess", "subProcess")
        .mapActivities("userTask", "userTask")
        .mapActivities("boundary", "boundary")
        .build();
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("boundary", "the mapped boundary event has be migrated together with the activity it is attached to");
    }
  }

  @Test
  public void testNotMapUnsupportedBoundaryEvents() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addErrorBoundaryEvent("subProcess", "error", ERROR_CODE)
      .addEscalationBoundaryEvent("subProcess", "escalation", ESCALATION_CODE);

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
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(2)
        .hasFailure("error", "the mapped activities are either null or not supported")
        .hasFailure("escalation", "the mapped activities are either null or not supported");
    }

  }

  @Test
  public void testNotMapProcessDefinitionWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.ONE_TASK_PROCESS)
      .addSubProcessToParent(ProcessModels.PROCESS_KEY)
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
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("userTask", "the mapped activities are either null or not supported");
    }
  }

  @Test
  public void testNotMapSubProcessWithEventSubProcess() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
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
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("subProcess", "the mapped activities are either null or not supported");
    }
  }

  @Test
  public void testMapActivityWithUnmappedParentWhichHasAEventSubProcessChild() {
    BpmnModelInstance testProcess = modify(ProcessModels.SUBPROCESS_PROCESS)
      .addSubProcessToParent("subProcess")
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
      .addSubProcessToParent("subProcess")
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
      Assert.fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasFailures(1)
        .hasFailure("innerTask", "the mapped activities are either null or not supported");
    }
  }

  protected void assertExceptionMessage(Exception e, String message) {
    assertThat(e.getMessage(), CoreMatchers.containsString(message));
  }

}
