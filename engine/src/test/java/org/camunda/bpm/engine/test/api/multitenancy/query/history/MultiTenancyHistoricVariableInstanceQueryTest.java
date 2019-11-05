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
package org.camunda.bpm.engine.test.api.multitenancy.query.history;

import static org.assertj.core.api.Assertions.assertThat;

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

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected final static String TENANT_NULL_VAR = "tenantNullVar";
  protected final static String TENANT_ONE_VAR = "tenant1Var";
  protected final static String TENANT_TWO_VAR = "tenant2Var";

  @Override
  protected void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .endEvent()
    .done();

    // given
    deploymentForTenant(TENANT_NULL, oneTaskProcess);
    deploymentForTenant(TENANT_ONE, oneTaskProcess);
    deploymentForTenant(TENANT_TWO, oneTaskProcess);

    startProcessInstanceForTenant(TENANT_NULL, TENANT_NULL_VAR);
    startProcessInstanceForTenant(TENANT_ONE, TENANT_ONE_VAR);
    startProcessInstanceForTenant(TENANT_TWO, TENANT_TWO_VAR);
  }

  public void testQueryWithoutTenantId() {
    // when
    HistoricVariableInstanceQuery query = historyService.
        createHistoricVariableInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3);
  }

  public void testQueryByTenantId() {
    // when
    HistoricVariableInstanceQuery queryTenantOne = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    HistoricVariableInstanceQuery queryTenantTwo = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(1);
    assertEquals(queryTenantOne.list().get(0).getValue(), TENANT_ONE_VAR);
    assertThat(queryTenantTwo.count()).isEqualTo(1);
    assertEquals(queryTenantTwo.list().get(0).getValue(), TENANT_TWO_VAR);
  }

  public void testQueryByTenantIds() {
    // when
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(2);
  }

  public void testQueryByNonExistingTenantId() {
    // when
    HistoricVariableInstanceQuery query = historyService
        .createHistoricVariableInstanceQuery()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0);
  }

  public void testFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricVariableInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // when
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicVariableInstances.size()).isEqualTo(3); // null-tenant instances are still included
    assertThat(historicVariableInstances.get(0).getTenantId()).isEqualTo(TENANT_NULL);
    assertEquals(historicVariableInstances.get(0).getValue(), TENANT_NULL_VAR);
    assertThat(historicVariableInstances.get(1).getTenantId()).isEqualTo(TENANT_ONE);
    assertEquals(historicVariableInstances.get(1).getValue(), TENANT_ONE_VAR);
    assertThat(historicVariableInstances.get(2).getTenantId()).isEqualTo(TENANT_TWO);
    assertEquals(historicVariableInstances.get(2).getValue(), TENANT_TWO_VAR);
  }

  public void testQuerySortingDesc() {
    // when
    List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicVariableInstances.size()).isEqualTo(3); // null-tenant instances are still included
    assertThat(historicVariableInstances.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertEquals(historicVariableInstances.get(0).getValue(), TENANT_TWO_VAR);
    assertThat(historicVariableInstances.get(1).getTenantId()).isEqualTo(TENANT_ONE);
    assertEquals(historicVariableInstances.get(1).getValue(), TENANT_ONE_VAR);
    assertThat(historicVariableInstances.get(2).getTenantId()).isEqualTo(TENANT_NULL);
    assertEquals(historicVariableInstances.get(2).getValue(), TENANT_NULL_VAR);
  }

  public void testQueryNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(1); // null-tenant instances are still included
  }

  public void testQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(2); // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1);
    assertThat(query.withoutTenantId().count()).isEqualTo(1);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1);
  }

  public void testQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3); // null-tenant instances are still included
    assertThat(query.withoutTenantId().count()).isEqualTo(1);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1);
  }

  public void testQueryDisabledTenantCheck() {
    // given
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricVariableInstanceQuery query = historyService.createHistoricVariableInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3);
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant, String var) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .setVariable("myVar", var)
        .processDefinitionTenantId(tenant)
        .execute();
  }

}
