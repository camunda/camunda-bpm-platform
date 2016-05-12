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
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderCaseInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderHistoricDecisionInstanceContext;
import org.camunda.bpm.engine.impl.cfg.multitenancy.TenantIdProviderProcessInstanceContext;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
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

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule() {
    @Override
    public ProcessEngineConfiguration configureEngine(ProcessEngineConfigurationImpl configuration) {

      TenantIdProvider tenantIdProvider = new VariableBasedTenantIdProvider();
      configuration.setTenantIdProvider(tenantIdProvider);

      return configuration;
    }
  };

  @Rule
  public RuleChain tenantRuleChain = RuleChain.outerRule(engineRule).around(testHelper);


  @Test
  public void cannotMigrateInstanceBetweenDifferentTenants() {
    // given
    ProcessDefinition sharedDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition tenantDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_TWO, ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = startInstanceForTenant(sharedDefinition, TENANT_ONE);
    MigrationPlan migrationPlan = engineRule.getRuntimeService().createMigrationPlan(sharedDefinition.getId(), tenantDefinition.getId())
        .mapEqualActivities()
        .build();

    // when
    try {
      engineRule.getRuntimeService()
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
    ProcessDefinition sharedDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    ProcessInstance processInstance = startInstanceForTenant(sharedDefinition, TENANT_ONE);
    MigrationPlan migrationPlan = engineRule.getRuntimeService().createMigrationPlan(sharedDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    // when
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    assertInstanceOfDefinition(processInstance, targetDefinition);
  }

  @Test
  public void canMigrateWithProcessInstanceQueryAllInstancesOfAuthenticatedTenant() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance1 = startInstanceForTenant(sourceDefinition, TENANT_ONE);
    ProcessInstance processInstance2 = startInstanceForTenant(sourceDefinition, TENANT_TWO);

    // when
    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceQuery(engineRule.getRuntimeService().createProcessInstanceQuery())
      .execute();
    engineRule.getIdentityService().clearAuthentication();

    // then
    assertInstanceOfDefinition(processInstance1, targetDefinition);
    assertInstanceOfDefinition(processInstance2, sourceDefinition);
  }

  @Test
  public void canMigrateWithProcessInstanceQueryAllInstancesOfAuthenticatedTenants() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance1 = startInstanceForTenant(sourceDefinition, TENANT_ONE);
    ProcessInstance processInstance2 = startInstanceForTenant(sourceDefinition, TENANT_TWO);

    // when
    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceQuery(engineRule.getRuntimeService().createProcessInstanceQuery())
      .execute();
    engineRule.getIdentityService().clearAuthentication();

    // then
    assertInstanceOfDefinition(processInstance1, targetDefinition);
    assertInstanceOfDefinition(processInstance2, targetDefinition);
  }

  protected void assertInstanceOfDefinition(ProcessInstance processInstance, ProcessDefinition targetDefinition) {
    Assert.assertEquals(1, engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.getId())
      .processDefinitionId(targetDefinition.getId())
      .count());
  }

  protected ProcessInstance startInstanceForTenant(ProcessDefinition processDefinition, String tenantId) {
    return engineRule.getRuntimeService()
      .startProcessInstanceById(processDefinition.getId(),
          Variables.createVariables().putValue(VariableBasedTenantIdProvider.TENANT_VARIABLE, tenantId));
  }

  public static class VariableBasedTenantIdProvider implements TenantIdProvider {
    public static final String TENANT_VARIABLE = "tenantId";

    @Override
    public String provideTenantIdForProcessInstance(TenantIdProviderProcessInstanceContext ctx) {
      return (String) ctx.getVariables().get(TENANT_VARIABLE);
    }

    @Override
    public String provideTenantIdForCaseInstance(TenantIdProviderCaseInstanceContext ctx) {
      return (String) ctx.getVariables().get(TENANT_VARIABLE);
    }

    @Override
    public String provideTenantIdForHistoricDecisionInstance(TenantIdProviderHistoricDecisionInstanceContext ctx) {
      return null;
    }
  }
}
