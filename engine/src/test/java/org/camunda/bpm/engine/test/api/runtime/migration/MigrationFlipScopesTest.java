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

import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationFlipScopesTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testCannotFlipAncestorScopes() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.DOUBLE_SUBPROCESS_PROCESS);

    // when
    try {
      rule.getRuntimeService()
        .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
        .mapActivities("outerSubProcess", "innerSubProcess")
        .mapActivities("innerSubProcess", "outerSubProcess")
        .mapActivities("userTask", "userTask")
        .build();

      Assert.fail("should not validate");
    } catch (MigrationPlanValidationException e) {
      MigrationPlanValidationReportAssert.assertThat(e.getValidationReport())
        .hasInstructionFailures("innerSubProcess",
          "The closest mapped ancestor 'outerSubProcess' is mapped to scope 'innerSubProcess' which is not an ancestor of target scope 'outerSubProcess'"
        );
    }
  }
}
