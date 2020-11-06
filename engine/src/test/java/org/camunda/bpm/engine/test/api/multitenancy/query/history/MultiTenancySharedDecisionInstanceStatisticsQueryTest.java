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

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceStatisticsQuery;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.multitenancy.StaticTenantIdTestProvider;
import org.camunda.bpm.engine.test.util.ProcessEngineBootstrapRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;


/**
 * @author Askar Akhmerov
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancySharedDecisionInstanceStatisticsQueryTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String DISH_DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected static final String DISH_DECISION = "dish-decision";
  protected static final String TEMPERATURE = "temperature";
  protected static final String DAY_TYPE = "dayType";
  protected static final String WEEKEND = "Weekend";
  protected static final String USER_ID = "user";

  protected static StaticTenantIdTestProvider tenantIdProvider;

  @ClassRule
  public static ProcessEngineBootstrapRule bootstrapRule = new ProcessEngineBootstrapRule(configuration -> {
    tenantIdProvider = new StaticTenantIdTestProvider(TENANT_ONE);
    configuration.setTenantIdProvider(tenantIdProvider);
  });
  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule(bootstrapRule);
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain tenantRuleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected DecisionService decisionService;
  protected RepositoryService repositoryService;
  protected HistoryService historyService;
  protected IdentityService identityService;


  @Before
  public void setUp() {
    decisionService = engineRule.getDecisionService();
    repositoryService = engineRule.getRepositoryService();
    historyService = engineRule.getHistoryService();
    identityService = engineRule.getIdentityService();

    testRule.deploy(DISH_DRG_DMN);

    decisionService.evaluateDecisionByKey(DISH_DECISION)
        .variables(Variables.createVariables().putValue(TEMPERATURE, 21).putValue(DAY_TYPE, WEEKEND))
        .evaluate();
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    DecisionRequirementsDefinition decisionRequirementsDefinition =
        repositoryService.createDecisionRequirementsDefinitionQuery()
            .singleResult();

    identityService.setAuthentication(USER_ID, null, null);

    HistoricDecisionInstanceStatisticsQuery query = historyService.
        createHistoricDecisionInstanceStatisticsQuery(decisionRequirementsDefinition.getId());

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    DecisionRequirementsDefinition decisionRequirementsDefinition =
        repositoryService.createDecisionRequirementsDefinitionQuery()
            .singleResult();

    identityService.setAuthentication(USER_ID, null, Arrays.asList(TENANT_ONE));

    HistoricDecisionInstanceStatisticsQuery query = historyService.
        createHistoricDecisionInstanceStatisticsQuery(decisionRequirementsDefinition.getId());

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    DecisionRequirementsDefinition decisionRequirementsDefinition =
        repositoryService.createDecisionRequirementsDefinitionQuery()
            .singleResult();

    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication(USER_ID, null, null);

    HistoricDecisionInstanceStatisticsQuery query = historyService.
        createHistoricDecisionInstanceStatisticsQuery(decisionRequirementsDefinition.getId());

    assertThat(query.count()).isEqualTo(3L);
  }
}
