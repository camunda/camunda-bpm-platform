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

package org.camunda.bpm.engine.test.api.multitenancy.cmmn.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;

public class MultiTenancyCaseDefinitionQueryTest extends PluggableProcessEngineTestCase {

  protected static final String CASE_DEFINITION_KEY = "Case_1";
  protected static final String CMMN = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deployment(CMMN);
    deploymentForTenant(TENANT_ONE, CMMN);
    deploymentForTenant(TENANT_TWO, CMMN);
  }

  public void testQueryNoTenantIdSet() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = repositoryService.
        createCaseDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByDefinitionsWithoutTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .withoutTenantId();
    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByKey() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count(), is(1L));

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count(), is(1L));
  }

  public void testQueryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    Map<String, CaseDefinition> caseDefinitionsForTenant = getCaseDefinitionsForTenant(query.list());
    assertThat(caseDefinitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(caseDefinitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(caseDefinitionsForTenant.get(null).getVersion(), is(1));
  }

  public void testQueryByLatestWithTenantId() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    CaseDefinition caseDefinition = query.singleResult();
    assertThat(caseDefinition.getTenantId(), is(TENANT_ONE));
    assertThat(caseDefinition.getVersion(), is(2));

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));

    caseDefinition = query.singleResult();
    assertThat(caseDefinition.getTenantId(), is(TENANT_TWO));
    assertThat(caseDefinition.getVersion(), is(1));
  }

  public void testQueryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    Map<String, CaseDefinition> caseDefinitionsForTenant = getCaseDefinitionsForTenant(query.list());
    assertThat(caseDefinitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(caseDefinitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
  }

  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
    deployment(CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count(), is(1L));

    CaseDefinition cDefinition = query.singleResult();
    assertThat(cDefinition.getTenantId(), is(nullValue()));
    assertThat(cDefinition.getVersion(), is(2));
  }

  public void testQueryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
    deployment(CMMN);
    // deploy a third version for tenant one
    deploymentForTenant(TENANT_ONE, CMMN);
    deploymentForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeCaseDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));

    Map<String, CaseDefinition> caseDefinitionsForTenant = getCaseDefinitionsForTenant(query.list());
    assertThat(caseDefinitionsForTenant.get(TENANT_ONE).getVersion(), is(3));
    assertThat(caseDefinitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(caseDefinitionsForTenant.get(null).getVersion(), is(2));
  }

  public void testQueryByNonExistingTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createCaseDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<CaseDefinition> caseDefinitions = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(caseDefinitions.size(), is(2));
    assertThat(caseDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(caseDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<CaseDefinition> caseDefinitions = repositoryService
        .createCaseDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(caseDefinitions.size(), is(2));
    assertThat(caseDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(caseDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeCaseDefinitionsWithoutTenantId().count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseDefinitionQuery query = repositoryService.createCaseDefinitionQuery();
    assertThat(query.count(), is(3L));
  }

  protected Map<String, CaseDefinition> getCaseDefinitionsForTenant(List<CaseDefinition> definitions) {
    Map<String, CaseDefinition> definitionsForTenant = new HashMap<String, CaseDefinition>();

    for (CaseDefinition definition : definitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

}
