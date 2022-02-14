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
package org.camunda.bpm.engine.test.api.multitenancy.cmmn.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyCaseDefinitionQueryTest extends PluggableProcessEngineTest {

  protected static final String CASE_DEFINITION_KEY = "Case_1";
  protected static final String CMMN = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    testRule.deploy(CMMN);
    testRule.deployForTenant(TENANT_ONE, CMMN);
    testRule.deployForTenant(TENANT_TWO, CMMN);
  }

  @Test
  public void testQueryNoTenantIdSet() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = repositoryService.
        createCaseDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByDefinitionsWithoutTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .withoutTenantId();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByKey() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(3L);

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count()).isEqualTo(1L);

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(3L);

    Map<String, CaseDefinition> caseDefinitionsForTenant = getCaseDefinitionsForTenant(query.list());
    assertThat(caseDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(2);
    assertThat(caseDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
    assertThat(caseDefinitionsForTenant.get(null).getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithTenantId() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    CaseDefinition caseDefinition = query.singleResult();
    assertThat(caseDefinition.getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(caseDefinition.getVersion()).isEqualTo(2);

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);

    caseDefinition = query.singleResult();
    assertThat(caseDefinition.getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(caseDefinition.getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(2L);

    Map<String, CaseDefinition> caseDefinitionsForTenant = getCaseDefinitionsForTenant(query.list());
    assertThat(caseDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(2);
    assertThat(caseDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
   testRule.deploy(CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);

    CaseDefinition cDefinition = query.singleResult();
    assertThat(cDefinition.getTenantId()).isNull();
    assertThat(cDefinition.getVersion()).isEqualTo(2);
  }

  @Test
  public void testQueryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
   testRule.deploy(CMMN);
    // deploy a third version for tenant one
    testRule.deployForTenant(TENANT_ONE, CMMN);
    testRule.deployForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);

    Map<String, CaseDefinition> caseDefinitionsForTenant = getCaseDefinitionsForTenant(query.list());
    assertThat(caseDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(3);
    assertThat(caseDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
    assertThat(caseDefinitionsForTenant.get(null).getVersion()).isEqualTo(2);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createCaseDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<CaseDefinition> caseDefinitions = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(caseDefinitions).hasSize(2);
    assertThat(caseDefinitions.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(caseDefinitions.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<CaseDefinition> caseDefinitions = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(caseDefinitions).hasSize(2);
    assertThat(caseDefinitions.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(caseDefinitions.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeCaseDefinitionsWithoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

  protected Map<String, CaseDefinition> getCaseDefinitionsForTenant(List<CaseDefinition> definitions) {
    Map<String, CaseDefinition> definitionsForTenant = new HashMap<String, CaseDefinition>();

    for (CaseDefinition definition : definitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

}
