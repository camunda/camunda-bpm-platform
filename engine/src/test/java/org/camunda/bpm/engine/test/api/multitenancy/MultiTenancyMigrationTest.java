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

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiTenancyMigrationTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule defaultEngineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule defaultTestRule = new ProcessEngineTestRule(defaultEngineRule);

  @Rule
  public RuleChain defaultRuleChin = RuleChain.outerRule(defaultEngineRule).around(defaultTestRule);

  @Test
  public void cannotCreateMigrationPlanBetweenDifferentTenants() {
    // given
    ProcessDefinition tenant1Definition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenant2Definition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_TWO, ProcessModels.ONE_TASK_PROCESS);

    // when
    try {
      defaultEngineRule.getRuntimeService().createMigrationPlan(tenant1Definition.getId(), tenant2Definition.getId())
      .mapEqualActivities()
      .build();
      Assert.fail("exception expected");
    } catch (ProcessEngineException e) {
      // then
      Assert.assertThat(e.getMessage(),
          CoreMatchers.containsString("Cannot migrate process instances between processes of different tenants ('tenant1' != 'tenant2')"));
    }
  }

  @Test
  public void canCreateMigrationPlanFromTenantToNoTenant() {
    // given
    ProcessDefinition sharedDefinition = defaultTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenantDefinition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);


    // when
    MigrationPlan migrationPlan = defaultEngineRule.getRuntimeService().createMigrationPlan(tenantDefinition.getId(), sharedDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    Assert.assertNotNull(migrationPlan);
  }

  @Test
  public void canCreateMigrationPlanFromNoTenantToTenant() {
    // given
    ProcessDefinition sharedDefinition = defaultTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenantDefinition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);


    // when
    MigrationPlan migrationPlan = defaultEngineRule.getRuntimeService().createMigrationPlan(sharedDefinition.getId(), tenantDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    Assert.assertNotNull(migrationPlan);
  }

  @Test
  public void canCreateMigrationPlanForNoTenants() {
    // given
    ProcessDefinition sharedDefinition = defaultTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);


    // when
    MigrationPlan migrationPlan = defaultEngineRule.getRuntimeService().createMigrationPlan(sharedDefinition.getId(), sharedDefinition.getId())
      .mapEqualActivities()
      .build();

    // then
    Assert.assertNotNull(migrationPlan);
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

    // when
    defaultEngineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

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

    // when
    try {
      defaultEngineRule.getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .execute();
      Assert.fail("exception expected");
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(),
          CoreMatchers.containsString("Cannot migrate process instance '" + processInstance.getId()
              + "' without tenant to a process definition with a tenant ('tenant1')"));
    }
  }

  @Test
  public void canMigrateInstanceWithTenantIdToDefinitionWithoutTenantId() {
    // given
    ProcessDefinition sourceDefinition = defaultTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = defaultTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = defaultEngineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    MigrationPlan migrationPlan = defaultEngineRule.getRuntimeService().createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    // when
    defaultEngineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    assertMigratedTo(processInstance, targetDefinition);
  }

  protected void assertMigratedTo(ProcessInstance processInstance, ProcessDefinition targetDefinition) {
    Assert.assertEquals(1, defaultEngineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.getId())
      .processDefinitionId(targetDefinition.getId())
      .count());
  }
}
