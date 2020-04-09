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
package org.camunda.bpm.engine.test.api.runtime;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;

import java.util.Collection;

import org.camunda.bpm.engine.authorization.BatchPermissions;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.ProcessDefinitionPermissions;
import org.camunda.bpm.engine.authorization.ProcessInstancePermissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class BatchUpdateSuspensionStateAuthorizationTest {

  protected static final String TEST_REASON = "test reason";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);
  protected BatchModificationHelper helper = new BatchModificationHelper(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testRule);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
          grant(Resources.BATCH, "*", "userId", BatchPermissions.CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "*", "userId", Permissions.CREATE)
        )
        .failsDueToRequired(
          grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", Permissions.UPDATE),
          grant(Resources.PROCESS_DEFINITION, "ProcessDefinition", "userId", Permissions.UPDATE_INSTANCE),
          grant(Resources.PROCESS_INSTANCE, "processInstance1", "userId", ProcessInstancePermissions.SUSPEND),
          grant(Resources.PROCESS_DEFINITION, "ProcessDefinition", "userId", ProcessDefinitionPermissions.SUSPEND_INSTANCE)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.UPDATE)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "*", "userId", Permissions.CREATE),
          grant(Resources.PROCESS_INSTANCE, "*", "userId", ProcessInstancePermissions.SUSPEND)
        ),
      scenario()
        .withAuthorizations(
          grant(Resources.BATCH, "*", "userId", BatchPermissions.CREATE_BATCH_UPDATE_PROCESS_INSTANCES_SUSPEND),
          grant(Resources.PROCESS_INSTANCE, "*", "userId", Permissions.UPDATE)
        )
        .succeeds()
    );
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @After
  public void cleanBatch() {
    Batch batch = engineRule.getManagementService().createBatchQuery().singleResult();
    if (batch != null) {
      engineRule.getManagementService().deleteBatch(
          batch.getId(), true);
    }

    HistoricBatch historicBatch = engineRule.getHistoryService().createHistoricBatchQuery().singleResult();
    if (historicBatch != null) {
      engineRule.getHistoryService().deleteHistoricBatch(
          historicBatch.getId());
    }
  }

  @After
  public void removeBatches() {
    helper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void executeBatch() {
    //given
    testRule.deployAndGetDefinition(ProcessModels.TWO_TASKS_PROCESS);

    ProcessInstance processInstance1 = engineRule.getRuntimeService().startProcessInstanceByKey("Process");

    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("processInstance1", processInstance1.getId())
        .bindResource("updateProcessInstanceSuspensionState", "*")
        .bindResource("ProcessDefinition","Process")
        .bindResource("batchId", "*")
        .start();

    Batch batch = engineRule.getRuntimeService()
        .updateProcessInstanceSuspensionState()
        .byProcessInstanceIds(processInstance1.getId())
        .suspendAsync();

    if (batch != null) {
      Job job = engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getSeedJobDefinitionId()).singleResult();

      // seed job
      engineRule.getManagementService().executeJob(job.getId());

      for (Job pending : engineRule.getManagementService().createJobQuery().jobDefinitionId(batch.getBatchJobDefinitionId()).list()) {
        engineRule.getManagementService().executeJob(pending.getId());
      }
    }
    // then
    if (authRule.assertScenario(scenario)) {
      assertEquals("userId", batch.getCreateUserId());
    }
  }

}
