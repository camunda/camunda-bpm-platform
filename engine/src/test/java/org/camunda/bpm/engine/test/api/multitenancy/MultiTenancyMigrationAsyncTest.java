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
package org.camunda.bpm.engine.test.api.multitenancy;

import java.util.Arrays;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.batch.history.HistoricBatch;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiTenancyMigrationAsyncTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule defaultEngineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule defaultTestRule = new ProcessEngineTestRule(defaultEngineRule);
  protected MigrationTestRule migrationRule = new MigrationTestRule(defaultEngineRule);

  @Rule
  public RuleChain defaultRuleChin = RuleChain.outerRule(defaultEngineRule).around(defaultTestRule).around(migrationRule);

  protected BatchMigrationHelper batchHelper = new BatchMigrationHelper(defaultEngineRule, migrationRule);

  @After
  public void removeBatches() {
    batchHelper.removeAllRunningAndHistoricBatches();
  }

  @Test
  public void canMigrateInstanceBetweenSameTenantCase1() {
    // given
    ProcessDefinition sourceDefinition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = defaultEngineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    MigrationPlan migrationPlan = defaultEngineRule.getRuntimeService().createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    Batch batch = defaultEngineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .executeAsync();

    batchHelper.executeSeedJob(batch);

    // when
    batchHelper.executeJobs(batch);

    // then
    assertMigratedTo(processInstance, targetDefinition);
  }

  @Test
  public void cannotMigrateInstanceWithoutTenantIdToDifferentTenant() {
    // given
    ProcessDefinition sourceDefinition = defaultTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = defaultEngineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    MigrationPlan migrationPlan = defaultEngineRule.getRuntimeService().createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    Batch batch = defaultEngineRule.getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .executeAsync();

    batchHelper.executeSeedJob(batch);

    // when
    batchHelper.executeJobs(batch);

    // then
    Job migrationJob = batchHelper.getExecutionJobs(batch).get(0);
    Assert.assertThat(migrationJob.getExceptionMessage(),
        CoreMatchers.containsString("Cannot migrate process instance '" + processInstance.getId()
            + "' without tenant to a process definition with a tenant ('tenant1')"));
  }

  protected void assertMigratedTo(ProcessInstance processInstance, ProcessDefinition targetDefinition) {
    Assert.assertEquals(1, defaultEngineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.getId())
      .processDefinitionId(targetDefinition.getId())
      .count());
  }
}
