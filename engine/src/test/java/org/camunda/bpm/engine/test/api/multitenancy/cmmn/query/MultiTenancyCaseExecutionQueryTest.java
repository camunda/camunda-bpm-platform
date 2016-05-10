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
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseExecutionQuery;

public class MultiTenancyCaseExecutionQueryTest extends PluggableProcessEngineTestCase {

  protected final static String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deployment(CMMN_FILE);
    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    createCaseInstance(null);
    createCaseInstance(TENANT_ONE);
    createCaseInstance(TENANT_TWO);
  }

  public void testQueryNoTenantIdSet() {
    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    assertThat(query.count(), is(6L));
  }

  public void testQueryByTenantId() {
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(2L));

    query = caseService
        .createCaseExecutionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantIds() {
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(4L));
  }

  public void testQueryByExecutionsWithoutTenantId() {
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .withoutTenantId();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    CaseExecutionQuery query = caseService
        .createCaseExecutionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      caseService.createCaseExecutionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude case executions without tenant id because of database-specific ordering
    List<CaseExecution> caseExecutions = caseService.createCaseExecutionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(caseExecutions.size(), is(4));
    assertThat(caseExecutions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(caseExecutions.get(1).getTenantId(), is(TENANT_ONE));
    assertThat(caseExecutions.get(2).getTenantId(), is(TENANT_TWO));
    assertThat(caseExecutions.get(3).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude case executions without tenant id because of database-specific ordering
    List<CaseExecution> caseExecutions = caseService.createCaseExecutionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(caseExecutions.size(), is(4));
    assertThat(caseExecutions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(caseExecutions.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(caseExecutions.get(2).getTenantId(), is(TENANT_ONE));
    assertThat(caseExecutions.get(3).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();
    assertThat(query.count(), is(2L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    assertThat(query.count(), is(4L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();

    assertThat(query.count(), is(6L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(2L));
    assertThat(query.withoutTenantId().count(), is(2L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseExecutionQuery query = caseService.createCaseExecutionQuery();
    assertThat(query.count(), is(6L));
  }

  protected void createCaseInstance(String tenantId) {
    String caseDefinitionId = null;

    CaseDefinitionQuery caseDefinitionQuery = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("oneTaskCase");
    if (tenantId == null) {
      caseDefinitionId = caseDefinitionQuery.withoutTenantId().singleResult().getId();
    } else {
      caseDefinitionId = caseDefinitionQuery.tenantIdIn(tenantId).singleResult().getId();
    }

    caseService.withCaseDefinition(caseDefinitionId).create();
  }

}
