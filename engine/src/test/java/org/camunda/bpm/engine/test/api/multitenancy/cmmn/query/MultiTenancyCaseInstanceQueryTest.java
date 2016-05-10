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
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

public class MultiTenancyCaseInstanceQueryTest extends PluggableProcessEngineTestCase {

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
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByInstancesWithoutTenantId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByNonExistingTenantId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      caseService.createCaseInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude case instances without tenant id because of database-specific ordering
    List<CaseInstance> caseInstances = caseService.createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(caseInstances.size(), is(2));
    assertThat(caseInstances.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(caseInstances.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude case instances without tenant id because of database-specific ordering
    List<CaseInstance> caseInstances = caseService.createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(caseInstances.size(), is(2));
    assertThat(caseInstances.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(caseInstances.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(1L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(3L));
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
