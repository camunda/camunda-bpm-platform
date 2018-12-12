/*
 * Copyright © 2013-2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
import org.camunda.bpm.engine.history.CleanableHistoricCaseInstanceReportResult;
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
public class MultiTenancyCleanableHistoricCaseInstanceReportCmdTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  private static final String CASE_DEFINITION_KEY = "one";

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
  }

  private void prepareCaseInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount, String tenantId) {
    // update time to live
    List<CaseDefinition> caseDefinitions = null;
    if (tenantId != null) {
      caseDefinitions = repositoryService.createCaseDefinitionQuery().caseDefinitionKey(key).tenantIdIn(tenantId).list();
    } else {
      caseDefinitions = repositoryService.createCaseDefinitionQuery().caseDefinitionKey(key).withoutTenantId().list();
    }
    assertEquals(1, caseDefinitions.size());
    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitions.get(0).getId(), historyTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(new Date(), daysInThePast));

    for (int i = 0; i < instanceCount; i++) {
      CaseInstance caseInstance = caseService.createCaseInstanceById(caseDefinitions.get(0).getId());
      if (tenantId != null) {
        assertEquals(tenantId, caseInstance.getTenantId());
      }
      caseService.terminateCaseExecution(caseInstance.getId());
      caseService.closeCaseInstance(caseInstance.getId());
    }

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

  @Test
  public void testReportNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    identityService.setAuthentication("user", null, null);

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResults = historyService.createCleanableHistoricCaseInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testReportWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResults = historyService.createCleanableHistoricCaseInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(TENANT_ONE, reportResults.get(0).getTenantId());
  }

  @Test
  public void testReportDisabledTenantCheck() {
    // given
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    testRule.deployForTenant(TENANT_TWO, CMMN_MODEL);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);
    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResults = historyService.createCleanableHistoricCaseInstanceReport().list();

    // then
    assertEquals(2, reportResults.size());
    assertEquals(TENANT_ONE, reportResults.get(0).getTenantId());
    assertEquals(TENANT_TWO, reportResults.get(1).getTenantId());
  }

  @Test
  public void testReportTenantIdInNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);
    testRule.deployForTenant(TENANT_TWO, CMMN_MODEL);

    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricCaseInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(0, reportResultsOne.size());
    assertEquals(0, reportResultsTwo.size());
  }

  @Test
  public void testReportTenantIdInWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);
    testRule.deployForTenant(TENANT_TWO, CMMN_MODEL);

    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricCaseInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
    assertEquals(0, reportResultsTwo.size());
  }

  @Test
  public void testReportTenantIdInDisabledTenantCheck() {
    // given
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);
    testRule.deployForTenant(TENANT_TWO, CMMN_MODEL);

    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricCaseInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
    assertEquals(1, reportResultsTwo.size());
    assertEquals(TENANT_TWO, reportResultsTwo.get(0).getTenantId());
  }

  @Test
  public void testReportWithoutTenantId() {
    // given
    testRule.deploy(CMMN_MODEL);

    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, null);

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResults = historyService.createCleanableHistoricCaseInstanceReport().withoutTenantId().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(null, reportResults.get(0).getTenantId());
  }

  @Test
  public void testReportTenantIdInWithoutTenantId() {
    // given
    testRule.deploy(CMMN_MODEL);
    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL);

    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, null);
    prepareCaseInstances(CASE_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);

    // when
    List<CleanableHistoricCaseInstanceReportResult> reportResults = historyService.createCleanableHistoricCaseInstanceReport().withoutTenantId().list();
    List<CleanableHistoricCaseInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricCaseInstanceReport().tenantIdIn(TENANT_ONE).list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(null, reportResults.get(0).getTenantId());
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
  }
}
