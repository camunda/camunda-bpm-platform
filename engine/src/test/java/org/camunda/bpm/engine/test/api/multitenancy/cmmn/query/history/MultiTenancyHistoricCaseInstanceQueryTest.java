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

package org.camunda.bpm.engine.test.api.multitenancy.cmmn.query.history;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricCaseInstance;
import org.camunda.bpm.engine.history.HistoricCaseInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MultiTenancyHistoricCaseInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deployment(CMMN_FILE);
    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey("oneTaskCase").caseDefinitionWithoutTenantId().create();
    createCaseInstance(TENANT_ONE);
    createCaseInstance(TENANT_TWO);
  }

  public void testQueryNoTenantIdSet() {
    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = historyService
        .createHistoricCaseInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByInstancesWithoutTenantId() {
    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByNonExistingTenantId() {
    HistoricCaseInstanceQuery query = historyService
        .createHistoricCaseInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricCaseInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude historic case instances without tenant id because of database-specific ordering
    List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(historicCaseInstances.size(), is(2));
    assertThat(historicCaseInstances.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(historicCaseInstances.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude historic case instances without tenant id because of database-specific ordering
    List<HistoricCaseInstance> historicCaseInstances = historyService.createHistoricCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(historicCaseInstances.size(), is(2));
    assertThat(historicCaseInstances.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(historicCaseInstances.get(1).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(1L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    HistoricCaseInstanceQuery query = historyService.createHistoricCaseInstanceQuery();
    assertThat(query.count(), is(3L));
  }

  protected void createCaseInstance(String tenantId) {
    CaseInstanceBuilder builder = caseService.withCaseDefinitionByKey("oneTaskCase");
    builder.caseDefinitionTenantId(tenantId).create();
  }

}
