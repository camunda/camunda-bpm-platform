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

import org.camunda.bpm.engine.migration.MigrationInstruction;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
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
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);
    testHelper.deploy("oneTaskProcess.bpmn20.xml", ProcessModels.ONE_TASK_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("UserTaskProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("UserTaskProcess", 2);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
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
  public void testMapEqualActivitiesInSameSubProcessScope() {
    // given
    testHelper.deploy("subProcessProcess.bpmn20.xml", ProcessModels.SUBPROCESS_PROCESS);
    testHelper.deploy("subProcessProcess.bpmn20.xml", ProcessModels.SUBPROCESS_PROCESS);

    ProcessDefinition sourceProcessDefinition = testHelper.findProcessDefinition("SubProcess", 1);
    ProcessDefinition targetProcessDefinition = testHelper.findProcessDefinition("SubProcess", 2);

    // when
    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
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

}
