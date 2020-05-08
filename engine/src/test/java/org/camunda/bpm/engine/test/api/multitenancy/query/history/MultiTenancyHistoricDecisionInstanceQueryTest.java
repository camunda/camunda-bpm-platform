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
import static org.assertj.core.api.Assertions.fail;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.historicDecisionInstanceByTenantId;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyHistoricDecisionInstanceQueryTest {

  protected static final String DMN = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected final static String TENANT_NULL = null;
  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected HistoryService historyService;
  protected DecisionService decisionService;
  protected RepositoryService repositoryService;
  protected IdentityService identityService;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Before
  public void setUp() {
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    decisionService = engineRule.getDecisionService();
    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_NULL, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_TWO, DMN);

    // given
    evaluateDecisionInstance();
    evaluateDecisionInstanceForTenant(TENANT_ONE);
    evaluateDecisionInstanceForTenant(TENANT_TWO);
  }

  @Test
  public void shouldQueryWithoutTenantId() {
    HistoricDecisionInstanceQuery query = historyService.
        createHistoricDecisionInstanceQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void shouldQueryByTenantId() {
    HistoricDecisionInstanceQuery query = historyService
        .createHistoricDecisionInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = historyService
        .createHistoricDecisionInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryByTenantIds() {
    // when
    HistoricDecisionInstanceQuery query = historyService
        .createHistoricDecisionInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    // then
    assertThat(query.count()).isEqualTo(2L);
  }

  public void shouldQueryByNonExistingTenantId() {
    // when
    HistoricDecisionInstanceQuery query = historyService
        .createHistoricDecisionInstanceQuery()
        .tenantIdIn("nonExisting");

    // then
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void shouldFailQueryByTenantIdNull() {
    try {
      historyService.createHistoricDecisionInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void shouldQuerySortingAsc() {
    // when
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
        .orderByTenantId()
        .asc()
        .list();

    // then
    assertThat(historicDecisionInstances.size()).isEqualTo(3);
    verifySorting(historicDecisionInstances, historicDecisionInstanceByTenantId());
  }

  @Test
  public void shouldQuerySortingDesc() {
    // when
    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
        .orderByTenantId()
        .desc()
        .list();

    // then
    assertThat(historicDecisionInstances.size()).isEqualTo(3);
    verifySorting(historicDecisionInstances, inverted(historicDecisionInstanceByTenantId()));
  }

  @Test
  public void shouldQueryNoAuthenticatedTenants() {
    // givem
    identityService.setAuthentication("user", null, null);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(1L); // null-tenant instances are still visible
  }

  @Test
  public void shouldQueryAuthenticatedTenant() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(2L); // null-tenant instances are also visible
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryAuthenticatedTenants() {
    // given
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L); // null-tenant instances are also visible
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void shouldQueryDisabledTenantCheck() {
    // given
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void shouldQueryFilterWithoutTenantId() {
    // when
    HistoricDecisionInstanceQuery query = historyService
        .createHistoricDecisionInstanceQuery()
        .withoutTenantId();

    //then
    assertThat(query.count()).isEqualTo(1L);
  }

  protected void evaluateDecisionInstanceForTenant(String tenant) {
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery()
        .tenantIdIn(tenant)
        .singleResult()
        .getId();

    VariableMap variables = Variables.createVariables().putValue("status", "bronze");
    decisionService.evaluateDecisionTableById(decisionDefinitionId, variables);
  }

  protected void evaluateDecisionInstance() {
    String decisionDefinitionId = repositoryService.createDecisionDefinitionQuery()
         .withoutTenantId()
         .singleResult()
         .getId();

    VariableMap variables = Variables.createVariables().putValue("status", "bronze");
    decisionService.evaluateDecisionTableById(decisionDefinitionId, variables);
  }

}
