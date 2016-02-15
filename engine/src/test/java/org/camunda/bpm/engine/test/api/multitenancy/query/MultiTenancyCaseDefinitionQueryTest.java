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

package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;

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
    deploymentForTenant(TENANT_ONE, CMMN);
    deploymentForTenant(TENANT_TWO, CMMN);
  }

  public void testQueryWithoutTenantId() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery();

    assertThat(query.count(), is(2L));
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

  public void testQueryByKey() {
    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, CMMN);

    CaseDefinitionQuery query = repositoryService
        .createCaseDefinitionQuery()
        .caseDefinitionKey(CASE_DEFINITION_KEY)
        .latestVersion()
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    List<CaseDefinition> caseDefinitions = query.list();
    assertThat(caseDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(caseDefinitions.get(0).getVersion(), is(2));
    assertThat(caseDefinitions.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(caseDefinitions.get(1).getVersion(), is(1));
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
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    List<CaseDefinition> caseDefinitions = query.list();
    assertThat(caseDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(caseDefinitions.get(0).getVersion(), is(2));
    assertThat(caseDefinitions.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(caseDefinitions.get(1).getVersion(), is(1));
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
    List<CaseDefinition> caseDefinitions = repositoryService
        .createCaseDefinitionQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(caseDefinitions.size(), is(2));
    assertThat(caseDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(caseDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<CaseDefinition> caseDefinitions = repositoryService
        .createCaseDefinitionQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(caseDefinitions.size(), is(2));
    assertThat(caseDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(caseDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

}
