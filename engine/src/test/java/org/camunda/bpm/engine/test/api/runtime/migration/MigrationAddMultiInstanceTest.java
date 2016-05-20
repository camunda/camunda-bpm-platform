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

import static org.camunda.bpm.engine.test.util.MigrationPlanValidationReportAssert.assertThat;
import static org.junit.Assert.fail;

import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.MultiInstanceProcessModels;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationAddMultiInstanceTest {

  protected ProcessEngineRule rule = new ProvidedProcessEngineRule();
  protected MigrationTestRule testHelper = new MigrationTestRule(rule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(rule).around(testHelper);

  @Test
  public void testAddMultiInstanceBody() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Target activity 'userTask' is a descendant of multi-instance body 'userTask#multiInstanceBody' "
          + "that is not mapped from the source process definition"
        );
    }
  }

  @Test
  public void testRemoveAndAddMultiInstanceBody() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_ONE_TASK_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Target activity 'userTask' is a descendant of multi-instance body 'userTask#multiInstanceBody' "
          + "that is not mapped from the source process definition"
        );
    }
  }

  @Test
  public void testAddMultiInstanceBodyWithDeeperNestedMapping() {
    // given
    ProcessDefinition sourceProcessDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetProcessDefinition = testHelper.deployAndGetDefinition(MultiInstanceProcessModels.PAR_MI_SUBPROCESS_PROCESS);

    try {
      rule.getRuntimeService()
      .createMigrationPlan(sourceProcessDefinition.getId(), targetProcessDefinition.getId())
      .mapActivities("userTask", "userTask")
      .build();
      fail("Should not succeed");
    }
    catch (MigrationPlanValidationException e) {
      e.printStackTrace();
      assertThat(e.getValidationReport())
        .hasInstructionFailures("userTask",
          "Target activity 'userTask' is a descendant of multi-instance body 'subProcess#multiInstanceBody' "
          + "that is not mapped from the source process definition"
        );
    }
  }


}
