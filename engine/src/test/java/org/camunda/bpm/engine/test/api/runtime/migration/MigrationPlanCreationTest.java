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

import java.util.List;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
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

    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    Assert.assertNotNull(migrationPlan);
    Assert.assertEquals(sourceProcessDefinition.getId(), migrationPlan.getSourceProcessDefinitionId());
    Assert.assertEquals(targetProcessDefinition.getId(), migrationPlan.getTargetProcessDefinitionId());

    List<MigrationInstruction> instructions = migrationPlan.getInstructions();
    Assert.assertNotNull(instructions);
    Assert.assertEquals(1, instructions.size());
    Assert.assertEquals(1, instructions.get(0).getSourceActivityIds().size());
    Assert.assertEquals("userTask", instructions.get(0).getSourceActivityIds().get(0));
    Assert.assertEquals(1, instructions.get(0).getTargetActivityIds().size());
    Assert.assertEquals("userTask", instructions.get(0).getTargetActivityIds().get(0));
  }

  @Test
  public void testMigrateNonExistingSourceDefinition() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition processDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);

    try {
      rule.getRuntimeService()
        .createMigrationPlan("aNonExistingProcDefId", processDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("source process definition with id aNonExistingProcDefId does not exist"));
    }
  }

  @Test
  public void testMigrateNullSourceDefinition() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition processDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);

    try {
      rule.getRuntimeService()
        .createMigrationPlan(null, processDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("sourceProcessDefinitionId is null"));
    }
  }

  @Test
  public void testMigrateNonExistingTargetDefinition() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition processDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    try {
      rule.getRuntimeService()
        .createMigrationPlan(processDefinition.getId(), "aNonExistingProcDefId")
        .mapActivities("userTask", "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("target process definition with id aNonExistingProcDefId does not exist"));
    }
  }

  @Test
  public void testMigrateNullTargetDefinition() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition processDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);

    try {
      rule.getRuntimeService()
        .createMigrationPlan(processDefinition.getId(), null)
        .mapActivities("userTask", "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("targetProcessDefinitionId is null"));
    }
  }

  @Test
  public void testMigrateNonExistingSourceActivityId() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);
    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("thisActivityDoesNotExist", "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("sourceActivity is null"));
    }
  }

  @Test
  public void testMigrateNullSourceActivityId() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);
    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities(null, "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("sourceActivityId is null"));
    }
  }

  @Test
  public void testMigrateNonExistingTargetActivityId() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);
    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "thisActivityDoesNotExist")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("targetActivity is null"));
    }
  }

  @Test
  public void testMigrateNullTargetActivityId() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);
    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", null)
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("targetActivityId is null"));
    }
  }

  @Test
  public void testMigrateTaskToHigherScope() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.SUBPROCESS_PROCESS);

    ProcessDefinition sourceDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetDefinition = testHelper.findProcessDefinition("SubProcess", 1);

    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "userTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Source activity userTask and"
          + " target activity userTask are not contained in the same sub process"));
    }

  }

  @Test
  public void testMigrateToDifferentActivityType() {
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_RECEIVE_TASK_PROCESS);

    ProcessDefinition sourceDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetDefinition = testHelper.findProcessDefinition("ReceiveTaskProcess", 1);

    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapActivities("userTask", "receiveTask")
        .build();
      Assert.fail("Should not succeed");
    } catch (BadUserRequestException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Invalid migration instruction"));
    }
  }

}
