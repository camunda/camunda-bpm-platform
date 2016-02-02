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

import static org.camunda.bpm.engine.test.util.MigrationInstructionInstanceValidationReportAssert.assertThat;

import java.util.Collections;

import org.camunda.bpm.engine.impl.migration.validation.MigrationInstructionInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationRemoveScopesTest {

  protected ProcessEngineRule rule = new ProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testCannotRemoveScope() {

    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deploy(ProcessModels.SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deploy(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();

    ProcessInstance processInstance = rule.getRuntimeService().startProcessInstanceById(sourceProcessDefinition.getId());

    // when
    try {
      rule.getRuntimeService().executeMigrationPlan(migrationPlan, Collections.singletonList(processInstance.getId()));
      Assert.fail("should fail");
    }
    catch (MigrationInstructionInstanceValidationException e) {
      assertThat(e.getValidationReport())
        .hasProcessInstance(processInstance)
        .hasFailures(1)
        .hasFailure("userTask", "The parent activity instance is not being migrated");
    }
  }

}
