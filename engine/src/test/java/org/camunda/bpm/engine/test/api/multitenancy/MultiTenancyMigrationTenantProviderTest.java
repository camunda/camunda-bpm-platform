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

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProvider;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiTenancyMigrationTenantProviderTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule tenantProviderEngineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule tenantProviderTestRule = new ProcessEngineTestRule(tenantProviderEngineRule);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {

      TenantIdProvider tenantIdProvider = new StaticTenantIdTestProvider(TENANT_ONE);
      configuration.setTenantIdProvider(tenantIdProvider);

      return configuration;
    }
  };

  @Rule
  public RuleChain tenantRuleChain = RuleChain.outerRule(tenantProviderEngineRule).around(tenantProviderTestRule);


  @Test
  public void cannotMigrateInstanceBetweenDifferentTenants() {
    // given
    ProcessDefinition sharedDefinition = tenantProviderTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenantDefinition = tenantProviderTestRule.deployForTenantAndGetDefinition(TENANT_TWO, ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = tenantProviderEngineRule.getRuntimeService().startProcessInstanceById(sharedDefinition.getId());
    MigrationPlan migrationPlan = tenantProviderEngineRule.getRuntimeService().createMigrationPlan(sharedDefinition.getId(), tenantDefinition.getId())
        .mapEqualActivities()
        .build();

    // when
    try {
      tenantProviderEngineRule.getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .execute();
      Assert.fail("exception expected");
    } catch (ProcessEngineException e) {
      Assert.assertThat(e.getMessage(),
          CoreMatchers.containsString("Cannot migrate process instance '" + processInstance.getId() + "' "
              + "to a process definition of a different tenant ('tenant1' != 'tenant2')"));
    }

    // then
    Assert.assertNotNull(migrationPlan);
  }

  @Test
  public void canMigrateInstanceBetweenSameTenantCase2() {
    // given
    ProcessDefinition sharedDefinition = tenantProviderTestRule.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = tenantProviderTestRule.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = tenantProviderEngineRule.getRuntimeService().startProcessInstanceById(sharedDefinition.getId());
    MigrationPlan migrationPlan = tenantProviderEngineRule.getRuntimeService().createMigrationPlan(sharedDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    // when
    tenantProviderEngineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    assertMigratedTo(processInstance, targetDefinition);
  }

  protected void assertMigratedTo(ProcessInstance processInstance, ProcessDefinition targetDefinition) {
    Assert.assertEquals(1, tenantProviderEngineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.getId())
      .processDefinitionId(targetDefinition.getId())
      .count());
  }
}
