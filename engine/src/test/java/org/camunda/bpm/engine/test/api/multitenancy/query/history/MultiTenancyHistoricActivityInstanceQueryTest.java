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

package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MultiTenancyHistoricActivityInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .endEvent()
    .done();

    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    startProcessInstanceForTenant(TENANT_ONE);
    startProcessInstanceForTenant(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    HistoricActivityInstanceQuery query = historyService.
        createHistoricActivityInstanceQuery();

    assertThat(query.count(), is(4L));
  }

  public void testQueryByTenantId() {
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(2L));

    query = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantIds() {
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(4L));
  }

  public void testQueryByNonExistingTenantId() {
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricActivityInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(historicActivityInstances.size(), is(4));
    assertThat(historicActivityInstances.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(historicActivityInstances.get(1).getTenantId(), is(TENANT_ONE));
    assertThat(historicActivityInstances.get(2).getTenantId(), is(TENANT_TWO));
    assertThat(historicActivityInstances.get(3).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(historicActivityInstances.size(), is(4));
    assertThat(historicActivityInstances.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(historicActivityInstances.get(1).getTenantId(), is(TENANT_TWO));
    assertThat(historicActivityInstances.get(2).getTenantId(), is(TENANT_ONE));
    assertThat(historicActivityInstances.get(3).getTenantId(), is(TENANT_ONE));
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
    assertThat(query.count(), is(0L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    assertThat(query.count(), is(4L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(2L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();
    assertThat(query.count(), is(4L));
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .processDefinitionTenantId(tenant)
        .execute();
  }

}
