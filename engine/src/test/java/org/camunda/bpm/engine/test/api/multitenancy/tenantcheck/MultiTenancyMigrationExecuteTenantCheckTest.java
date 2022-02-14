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
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.migration.MigrationPlan;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.api.runtime.migration.models.ProcessModels;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class MultiTenancyMigrationExecuteTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProvidedProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testHelper);

  @Test
  public void canMigrateWithAuthenticatedTenant() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // when
    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_ONE));
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    assertMigratedTo(processInstance, targetDefinition);

  }

  @Test
  public void cannotMigrateOfNonAuthenticatedTenant() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    engineRule.getIdentityService().setAuthentication("user", null, Arrays.asList(TENANT_TWO));

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .execute())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot migrate process instance '" + processInstance.getId()
      + "' because it belongs to no authenticated tenant");

  }

  @Test
  public void cannotMigrateWithNoAuthenticatedTenant() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());
    engineRule.getIdentityService().setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> engineRule.getRuntimeService()
        .newMigration(migrationPlan)
        .processInstanceIds(Arrays.asList(processInstance.getId()))
        .execute())
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot migrate process instance '" + processInstance.getId()
      + "' because it belongs to no authenticated tenant");
  }

  @Test
  public void canMigrateSharedInstanceWithNoTenant() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployAndGetDefinition(ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // when
    engineRule.getIdentityService().setAuthentication("user", null, null);
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    assertMigratedTo(processInstance, targetDefinition);

  }

  @Test
  public void canMigrateInstanceWithTenantCheckDisabled() {
    // given
    ProcessDefinition sourceDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);
    ProcessDefinition targetDefinition = testHelper.deployForTenantAndGetDefinition(TENANT_ONE, ProcessModels.ONE_TASK_PROCESS);

    MigrationPlan migrationPlan = engineRule
        .getRuntimeService()
        .createMigrationPlan(sourceDefinition.getId(), targetDefinition.getId())
        .mapEqualActivities()
        .build();

    ProcessInstance processInstance = engineRule.getRuntimeService().startProcessInstanceById(sourceDefinition.getId());

    // when
    engineRule.getIdentityService().setAuthentication("user", null, null);
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    engineRule.getRuntimeService()
      .newMigration(migrationPlan)
      .processInstanceIds(Arrays.asList(processInstance.getId()))
      .execute();

    // then
    assertMigratedTo(processInstance, targetDefinition);

  }

  protected void assertMigratedTo(ProcessInstance processInstance, ProcessDefinition targetDefinition) {
    Assert.assertEquals(1, engineRule.getRuntimeService()
      .createProcessInstanceQuery()
      .processInstanceId(processInstance.getId())
      .processDefinitionId(targetDefinition.getId())
      .count());
  }
}
