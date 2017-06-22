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
import org.camunda.bpm.engine.history.HistoricFinishedDecisionInstanceReportResult;
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
public class MultiTenancyFinishedDecisionInstanceReportCmdTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

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

    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);
    prepareDecisionInstances("decision", -6, 5, 10);
  }

  @Test
  public void testReportNoAuthenticatedTenants() {
    // given
    // Decision Definition and 10 decision instances
    identityService.setAuthentication("user", null, null);

    // when
    List<HistoricFinishedDecisionInstanceReportResult> reportResults = historyService.createHistoricFinishedDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testReportWithAuthenticatedTenants() {
    // given
    // Decision Definition and 10 decision instances
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<HistoricFinishedDecisionInstanceReportResult> reportResults = historyService.createHistoricFinishedDecisionInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
  }

  @Test
  public void testReportDisabledTenantCheck() {
    // given
    // Decision Definition and 10 decision instances
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<HistoricFinishedDecisionInstanceReportResult> reportResults = historyService.createHistoricFinishedDecisionInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
  }

  protected void prepareDecisionInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount) {
    List<DecisionDefinition> decisionDefinitions = repositoryService.createDecisionDefinitionQuery().decisionDefinitionKey(key).list();
    assertEquals(1, decisionDefinitions.size());
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinitions.get(0).getId(), historyTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysInThePast));

    Map<String, Object> variables = Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
    for (int i = 0; i < instanceCount; i++) {
      engineRule.getDecisionService().evaluateDecisionByKey(key).variables(variables).evaluate();
    }

    ClockUtil.setCurrentTime(oldCurrentTime);
  }
}
