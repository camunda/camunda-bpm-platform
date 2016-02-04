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
import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.UserTask;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationPlanCreationTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testExplicitInstructionGeneration() {

    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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

    MigrationPlan migrationPlan = rule.getRuntimeService()
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
      rule.getRuntimeService()
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
      rule.getRuntimeService()
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
    BpmnModelInstance testProcess = ProcessModels.ONE_TASK_PROCESS.clone()
      .<UserTask>getModelElementById("userTask").builder()
      .multiInstance().parallel().cardinality("3").multiInstanceDone().done();
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(testProcess);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(testProcess);

    // when
    try {
      rule.getRuntimeService()
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

  protected void assertExceptionMessage(Exception e, String message) {
    assertThat(e.getMessage(), CoreMatchers.containsString(message));
  }

}
