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
package org.camunda.bpm.engine.test.api.multitenancy.cmmn.query.history;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstance;
import org.camunda.bpm.engine.history.HistoricCaseActivityInstanceQuery;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseInstanceBuilder;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_ACTIVITY)
public class MultiTenancyHistoricCaseActivityInstanceQueryTest extends PluggableProcessEngineTestCase {

  protected final static String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    // given
    deploymentForTenant(TENANT_NULL, CMMN_FILE);
    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    createCaseInstance(TENANT_NULL);
    createCaseInstance(TENANT_ONE);
    createCaseInstance(TENANT_TWO);
  }

  public void testQueryWithoutTenantId() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  public void testQueryFilterWithoutTenantId() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .withoutTenantId();

    // then
    assertThat(query.count()).isEqualTo(1L);
  }

  public void testQueryByTenantId() {
    // when
    HistoricCaseActivityInstanceQuery queryTenantOne = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    HistoricCaseActivityInstanceQuery queryTenantTwo = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    // then
    assertThat(queryTenantOne.count()).isEqualTo(1L);
    assertThat(queryTenantTwo.count()).isEqualTo(1L);
  }

  public void testQueryByTenantIds() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  public void testQueryByNonExistingTenantId() {
    // when
    HistoricCaseActivityInstanceQuery query = historyService
        .createHistoricCaseActivityInstanceQuery()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  public void testFailQueryByTenantIdNull() {
    try {
      // when
      historyService.createHistoricCaseActivityInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");

      // then
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // when
    List<HistoricCaseActivityInstance> historicCaseActivityInstances = historyService
        .createHistoricCaseActivityInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicCaseActivityInstances.size()).isEqualTo(3);
    assertThat(historicCaseActivityInstances.get(0).getTenantId()).isEqualTo(TENANT_NULL);
    assertThat(historicCaseActivityInstances.get(1).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicCaseActivityInstances.get(2).getTenantId()).isEqualTo(TENANT_TWO);
  }

  public void testQuerySortingDesc() {
    // when
    List<HistoricCaseActivityInstance> historicCaseActivityInstances = historyService.createHistoricCaseActivityInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicCaseActivityInstances.size()).isEqualTo(3);
    assertThat(historicCaseActivityInstances.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(historicCaseActivityInstances.get(1).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(historicCaseActivityInstances.get(2).getTenantId()).isEqualTo(TENANT_NULL);
  }

  public void testQueryNoAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, null);

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(1L); // null-tenant instances are still included
  }

  public void testQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(2L);  // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  public void testQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L); // null-tenant instances are still included
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  public void testQueryDisabledTenantCheck() {
    // given
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricCaseActivityInstanceQuery query = historyService.createHistoricCaseActivityInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  protected void createCaseInstance(String tenantId) {
    CaseInstanceBuilder builder = caseService.withCaseDefinitionByKey("oneTaskCase");
    builder.caseDefinitionTenantId(tenantId).create();
  }

}
