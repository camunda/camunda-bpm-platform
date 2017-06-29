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

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.CaseService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.history.HistoricFinishedCaseInstanceReportResult;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyFinishedCaseInstanceReportCmdTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";

  protected static final String CMMN_MODEL = "org/camunda/bpm/engine/test/repository/one.cmmn";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;
  protected CaseService caseService;
  protected HistoryService historyService;

  protected String caseDefinitionId;

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    caseService = engineRule.getCaseService();
    historyService = engineRule.getHistoryService();

    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);

    prepareCaseInstances("one", -6, 5, 10);
  }

  private void prepareCaseInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount) {
    // update time to live
    List<CaseDefinition> caseDefinitions = repositoryService.createCaseDefinitionQuery().caseDefinitionKey(key).list();
    assertEquals(1, caseDefinitions.size());
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitions.get(0).getId(), historyTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), daysInThePast));

    for (int i = 0; i < instanceCount; i++) {
      CaseInstance caseInstance = caseService.createCaseInstanceByKey(key);
      caseService.terminateCaseExecution(caseInstance.getId());
      caseService.closeCaseInstance(caseInstance.getId());
    }

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  @Test
  public void testReportNoAuthenticatedTenants() {
    // given
    // Case Definition and 10 case instances
    identityService.setAuthentication("user", null, null);

    // when
    List<HistoricFinishedCaseInstanceReportResult> reportResults = historyService.createHistoricFinishedCaseInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testReportWithAuthenticatedTenants() {
    // given
    // Case Definition and 10 case instances
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<HistoricFinishedCaseInstanceReportResult> reportResults = historyService.createHistoricFinishedCaseInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
  }

  @Test
  public void testReportDisabledTenantCheck() {
    // given
    // Case Definition and 10 case instances
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<HistoricFinishedCaseInstanceReportResult> reportResults = historyService.createHistoricFinishedCaseInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
  }
}
