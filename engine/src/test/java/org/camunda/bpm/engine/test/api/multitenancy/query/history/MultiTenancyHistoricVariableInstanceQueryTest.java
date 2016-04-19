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
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_AUDIT)
public class MultiTenancyHistoricVariableInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected final static String TENANT_ONE_VAR = "tenant1Var";
  protected final static String TENANT_TWO_VAR = "tenant2Var";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .endEvent()
    .done();

    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    startProcessInstanceForTenant(TENANT_ONE, TENANT_ONE_VAR);
    startProcessInstanceForTenant(TENANT_TWO, TENANT_TWO_VAR);
  }

  public void testQueryWithoutTenantId() {
    HistoricVariableInstanceQuery query = historyService.
        createHistoricVariableInstanceQuery();

    assertThat(query.count(), is(2L));
  }

  public void testQueryByTenantId() {
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));
    assertEquals(query.list().get(0).getValue(), TENANT_ONE_VAR);

    query = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
    assertEquals(query.list().get(0).getValue(), TENANT_TWO_VAR);
  }

  public void testQueryByTenantIds() {
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByNonExistingTenantId() {
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricVariableInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(historicVariableInstances.size(), is(2));
    assertThat(historicVariableInstances.get(0).getTenantId(), is(TENANT_ONE));
    assertEquals(historicVariableInstances.get(0).getValue(), TENANT_ONE_VAR);
    assertThat(historicVariableInstances.get(1).getTenantId(), is(TENANT_TWO));
    assertEquals(historicVariableInstances.get(1).getValue(), TENANT_TWO_VAR);
  }

  public void testQuerySortingDesc() {
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(historicVariableInstances.size(), is(2));
    assertThat(historicVariableInstances.get(0).getTenantId(), is(TENANT_TWO));
    assertEquals(historicVariableInstances.get(0).getValue(), TENANT_TWO_VAR);
    assertThat(historicVariableInstances.get(1).getTenantId(), is(TENANT_ONE));
    assertEquals(historicVariableInstances.get(1).getValue(), TENANT_ONE_VAR);
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertThat(query.count(), is(0L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count(), is(1L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();
    assertThat(query.count(), is(2L));
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant, String var) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .setVariable("myVar", var)
        .processDefinitionTenantId(tenant)
        .execute();
  }

}
