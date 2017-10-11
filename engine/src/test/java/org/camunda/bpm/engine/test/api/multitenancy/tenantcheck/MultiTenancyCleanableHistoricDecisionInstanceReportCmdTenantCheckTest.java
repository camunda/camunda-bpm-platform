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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyCleanableHistoricDecisionInstanceReportCmdTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  private static final String DECISION_DEFINITION_KEY = "decision";

  protected static final String DMN_MODEL = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected TaskService taskService;
  protected HistoryService historyService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    historyService = engineRule.getHistoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  public void testReportNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    identityService.setAuthentication("user", null, null);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testReportWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(TENANT_ONE, reportResults.get(0).getTenantId());
  }

  @Test
  public void testReportDisabledTenantCheck() {
    // given
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10,TENANT_ONE);
    testRule.deployForTenant(TENANT_TWO, DMN_MODEL);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(2, reportResults.size());
    assertEquals(TENANT_ONE, reportResults.get(0).getTenantId());
    assertEquals(TENANT_TWO, reportResults.get(1).getTenantId());
  }

  @Test
  public void testReportTenantIdInNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    testRule.deployForTenant(TENANT_TWO, DMN_MODEL);

    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(0, reportResultsOne.size());
    assertEquals(0, reportResultsTwo.size());
  }

  @Test
  public void testReportTenantIdInWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    testRule.deployForTenant(TENANT_TWO, DMN_MODEL);

    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
    assertEquals(0, reportResultsTwo.size());
  }

  @Test
  public void testReportTenantIdInDisabledTenantCheck() {
    // given
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    testRule.deployForTenant(TENANT_TWO, DMN_MODEL);

    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
    assertEquals(1, reportResultsTwo.size());
    assertEquals(TENANT_TWO, reportResultsTwo.get(0).getTenantId());
  }

  @Test
  public void testReportWithoutTenantId() {
    // given
    testRule.deploy(DMN_MODEL);

    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, null);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().withoutTenantId().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(null, reportResults.get(0).getTenantId());
  }

  @Test
  public void testReportTenantIdInWithoutTenantId() {
    // given
    testRule.deploy(DMN_MODEL);
    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);

    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, null);
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().withoutTenantId().list();
    List<CleanableHistoricDecisionInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricDecisionInstanceReport().tenantIdIn(TENANT_ONE).list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(null, reportResults.get(0).getTenantId());
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
  }

  protected void prepareDecisionInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount, String tenantId) {
    List<DecisionDefinition> decisionDefinitions = null;
    if (tenantId != null) {
      decisionDefinitions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(key).tenantIdIn(tenantId).list();
    } else {
      decisionDefinitions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(key).withoutTenantId().list();
    }
    assertEquals(1, decisionDefinitions.size());
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), historyTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysInThePast));

    Map<String, Object> variables = Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
    for (int i = 0; i < instanceCount; i++) {
      if (tenantId != null) {
        engineRule.getDecisionService().evaluateDecisionByKey(key).decisionDefinitionTenantId(tenantId).variables(variables).evaluate();
      } else {
        engineRule.getDecisionService().evaluateDecisionByKey(key).decisionDefinitionWithoutTenantId().variables(variables).evaluate();
      }
    }

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

}
