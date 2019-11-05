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
import org.camunda.bpm.engine.history.HistoricActivityInstance;
import org.camunda.bpm.engine.history.HistoricActivityInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MultiTenancyHistoricActivityInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

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

    startProcessInstanceForTenant(TENANT_NULL);
    startProcessInstanceForTenant(TENANT_ONE);
    startProcessInstanceForTenant(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    // when
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(6L);
  }

  public void testQueryFilterWithoutTenantId() {
    // when
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(2L);
  }

  public void testQueryByTenantId() {
    // when
    HistoricActivityInstanceQuery queryTenantOne = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    HistoricActivityInstanceQuery queryTenantTwo = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(2L);
    assertThat(queryTenantTwo.count()).isEqualTo(2L);
  }

  public void testQueryByTenantIds() {
    // when
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(4L);
  }

  public void testQueryByNonExistingTenantId() {
    // when
    HistoricActivityInstanceQuery query = historyService
        .createHistoricActivityInstanceQuery()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  public void testFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricActivityInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // when
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicActivityInstances.size()).isEqualTo(6);
    assertThat(historicActivityInstances.get(0).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicActivityInstances.get(1).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicActivityInstances.get(2).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicActivityInstances.get(3).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicActivityInstances.get(4).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicActivityInstances.get(5).getTenantId()).isEqualTo(TENANT_TWO);
  }

  public void testQuerySortingDesc() {
    // when
    List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicActivityInstances.size()).isEqualTo(6);
    assertThat(historicActivityInstances.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicActivityInstances.get(1).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicActivityInstances.get(2).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicActivityInstances.get(3).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicActivityInstances.get(4).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicActivityInstances.get(5).getTenantId()).isEqualTo(TENANT_NULL);
  }

  public void testQueryNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(2L); // null-tenant instances are still included
  }

  public void testQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(4L); // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.withoutTenantId().count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(2L);
  }

  public void testQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(6L); // null-tenant instances are still included
    assertThat(query.withoutTenantId().count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(2L);
  }

  public void testQueryDisabledTenantCheck() {
    // given
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricActivityInstanceQuery query = historyService.createHistoricActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(6L);
  }

  protected ProcessInstance startProcessInstanceForTenant(String tenant) {
    return runtimeService.createProcessInstanceByKey("testProcess")
        .processDefinitionTenantId(tenant)
        .execute();
  }

}
