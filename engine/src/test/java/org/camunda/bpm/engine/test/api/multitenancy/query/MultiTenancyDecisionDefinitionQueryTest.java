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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.repository.DecisionDefinitionQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyDecisionDefinitionQueryTest extends PluggableProcessEngineTest {

  protected static final String DECISION_DEFINITION_KEY = "decision";
  protected static final String DMN = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    testRule.deploy(DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_TWO, DMN);
  }

  @Test
  public void testQueryNoTenantIdSet() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = repositoryService.
        createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByDefinitionsWithoutTenantId() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeDecisionDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeDecisionDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDecisionDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByKey() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(3L);

    query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count()).isEqualTo(1L);

    query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(3L);

    Map<String, DecisionDefinition> decisionDefinitionsForTenant = getDecisionDefinitionsForTenant(query.list());
    assertThat(decisionDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(2);
    assertThat(decisionDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
    assertThat(decisionDefinitionsForTenant.get(null).getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithTenantId() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    DecisionDefinition decisionDefinition = query.singleResult();
    assertThat(decisionDefinition.getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(decisionDefinition.getVersion()).isEqualTo(2);

    query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);

    decisionDefinition = query.singleResult();
    assertThat(decisionDefinition.getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(decisionDefinition.getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(2L);

    Map<String, DecisionDefinition> decisionDefinitionsForTenant = getDecisionDefinitionsForTenant(query.list());
    assertThat(decisionDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(2);
    assertThat(decisionDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
   testRule.deploy(DMN);

    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);

    DecisionDefinition decisionDefinition = query.singleResult();
    assertThat(decisionDefinition.getTenantId()).isNull();
    assertThat(decisionDefinition.getVersion()).isEqualTo(2);
  }

  @Test
  public void testQueryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
   testRule.deploy(DMN);
    // deploy a third version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .decisionDefinitionKey(DECISION_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDecisionDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);

    Map<String, DecisionDefinition> decisionDefinitionsForTenant = getDecisionDefinitionsForTenant(query.list());
    assertThat(decisionDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(3);
    assertThat(decisionDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
    assertThat(decisionDefinitionsForTenant.get(null).getVersion()).isEqualTo(2);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    DecisionDefinitionQuery query = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createDecisionDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<DecisionDefinition> decisionDefinitions = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(decisionDefinitions).hasSize(2);
    assertThat(decisionDefinitions.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(decisionDefinitions.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<DecisionDefinition> decisionDefinitions = repositoryService
        .createDecisionDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(decisionDefinitions).hasSize(2);
    assertThat(decisionDefinitions.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(decisionDefinitions.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeDecisionDefinitionsWithoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DecisionDefinitionQuery query = repositoryService.createDecisionDefinitionQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

  protected Map<String, DecisionDefinition> getDecisionDefinitionsForTenant(List<DecisionDefinition> decisionDefinitions) {
    Map<String, DecisionDefinition> definitionsForTenant = new HashMap<String, DecisionDefinition>();

    for (DecisionDefinition definition : decisionDefinitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

}
