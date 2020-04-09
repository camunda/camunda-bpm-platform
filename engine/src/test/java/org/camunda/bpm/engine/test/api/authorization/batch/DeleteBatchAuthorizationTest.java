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
package org.camunda.bpm.engine.test.api.authorization.batch;

import static org.camunda.bpm.engine.history.UserOperationLogEntry.CATEGORY_OPERATOR;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE;
import static org.camunda.bpm.engine.history.UserOperationLogEntry.OPERATION_TYPE_DELETE_HISTORY;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.history.UserOperationLogEntry;
import org.camunda.bpm.engine.history.UserOperationLogQuery;
import org.camunda.bpm.engine.impl.history.HistoryLevel;
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
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
@RunWith(Parameterized.class)
public class DeleteBatchAuthorizationTest {

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
          grant(Resources.BATCH, "batchId", "userId", Permissions.DELETE)),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "batchId", "userId", Permissions.DELETE))
        .succeeds()
      );
  }

  protected MigrationPlan migrationPlan;
  protected Batch batch;
  protected boolean cascade;

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
    if (authRule.scenarioFailed()) {
      engineRule.getManagementService().deleteBatch(batch.getId(), true);
    }
    else {
      if (!cascade && engineRule.getProcessEngineConfiguration().getHistoryLevel() == HistoryLevel.HISTORY_LEVEL_FULL) {
        engineRule.getHistoryService().deleteHistoricBatch(batch.getId());
      }
    }
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

    cascade = false;
    engineRule.getManagementService().deleteBatch(batch.getId(), cascade);

    // then
    if (authRule.assertScenario(scenario)) {
      Assert.assertEquals(0, engineRule.getManagementService().createBatchQuery().count());

      List<UserOperationLogEntry> userOperationLogEntries = engineRule.getHistoryService()
        .createUserOperationLogQuery()
        .operationType(OPERATION_TYPE_DELETE)
        .list();

      assertEquals(1, userOperationLogEntries.size());

      UserOperationLogEntry entry = userOperationLogEntries.get(0);
      assertEquals("cascadeToHistory", entry.getProperty());
      assertEquals("false", entry.getNewValue());
      assertEquals(CATEGORY_OPERATOR, entry.getCategory());
    }
  }

  /**
   * Requires no additional DELETE_HISTORY authorization => consistent with deleteDeployment
   */
  @Test
  public void testDeleteBatchCascade() {
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

    cascade = true;
    engineRule.getManagementService().deleteBatch(batch.getId(), cascade);

    // then
    if (authRule.assertScenario(scenario)) {
      Assert.assertEquals(0, engineRule.getManagementService().createBatchQuery().count());
      Assert.assertEquals(0, engineRule.getHistoryService().createHistoricBatchQuery().count());

      UserOperationLogQuery query = engineRule.getHistoryService()
        .createUserOperationLogQuery();

      List<UserOperationLogEntry> userOperationLogEntries = query.operationType(OPERATION_TYPE_DELETE)
        .batchId(batch.getId())
        .list();
      assertEquals(1, userOperationLogEntries.size());

      UserOperationLogEntry entry = userOperationLogEntries.get(0);
      assertEquals("cascadeToHistory", entry.getProperty());
      assertEquals("true", entry.getNewValue());
      assertEquals(CATEGORY_OPERATOR, entry.getCategory());

      // Ensure that HistoricBatch deletion is not logged
      List<UserOperationLogEntry> userOperationLogHistoricEntries = query.operationType(OPERATION_TYPE_DELETE_HISTORY)
        .batchId(batch.getId())
        .list();
      assertEquals(0, userOperationLogHistoricEntries.size());
    }
  }
}
