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
package org.camunda.bpm.engine.test.api.authorization.batch;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import java.util.Arrays;
import java.util.Collection;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author Thorben Lindhauer
 *
 */
@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class DeleteHistoricBatchAuthorizationTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameter
  public AuthorizationScenario scenario;

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.BATCH, "batchId", "userId", Permissions.DELETE_HISTORY)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.DELETE_HISTORY))
        .succeeds()
      );
  }

  protected MigrationPlan migrationPlan;
  protected Batch batch;

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
  }

  @Before
  public void deployProcessesAndCreateMigrationPlan() {
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .build();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @After
  public void deleteBatch() {
    engineRule.getManagementService().deleteBatch(batch.getId(), true);
  }

  @Test
  public void testDeleteBatch() {

    // given
    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(migrationPlan.getSourceProcessDefinitionId());
    batch = engineRule
        .getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .executeAsync();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("batchId", batch.getId())
      .start();

    engineRule.getHistoryService().deleteHistoricBatch(batch.getId());

    // then
    if (authRule.assertScenario(scenario)) {
      Assert.assertEquals(0, engineRule.getHistoryService().createHistoricBatchQuery().count());
    }
  }

}
