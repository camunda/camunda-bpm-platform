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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.history.CleanableHistoricProcessInstanceReportResult;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class MultiTenancyCleanableHistoricProcessInstanceReportCmdTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected static final BpmnModelInstance BPMN_PROCESS = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .userTask()
      .endEvent()
    .done();

  @Before
  public void init() {
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();
    runtimeService = engineRule.getRuntimeService();
    historyService = engineRule.getHistoryService();
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
  }

  @Test
  public void testReportNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);

    identityService.setAuthentication("user", null, null);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testReportWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(TENANT_ONE, reportResults.get(0).getTenantId());
  }

  @Test
  public void testReportDisabledTenantCheck() {
    // given
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);
    testRule.deployForTenant(TENANT_TWO, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().list();

    // then
    assertEquals(2, reportResults.size());
    assertEquals(TENANT_ONE, reportResults.get(0).getTenantId());
    assertEquals(TENANT_TWO, reportResults.get(1).getTenantId());
  }

  @Test
  public void testReportTenantIdInNoAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);
    testRule.deployForTenant(TENANT_TWO, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricProcessInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(0, reportResultsOne.size());
    assertEquals(0, reportResultsTwo.size());
  }

  @Test
  public void testReportTenantIdInWithAuthenticatedTenants() {
    // given
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);
    testRule.deployForTenant(TENANT_TWO, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricProcessInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
    assertEquals(0, reportResultsTwo.size());
  }

  @Test
  public void testReportTenantIdInDisabledTenantCheck() {
    // given
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);
    testRule.deployForTenant(TENANT_TWO, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_TWO);

    identityService.setAuthentication("user", null, null);
    processEngineConfiguration.setTenantCheckEnabled(false);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_ONE).list();
    List<CleanableHistoricProcessInstanceReportResult> reportResultsTwo = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_TWO).list();

    // then
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
    assertEquals(1, reportResultsTwo.size());
    assertEquals(TENANT_TWO, reportResultsTwo.get(0).getTenantId());
  }

  @Test
  public void testReportWithoutTenantId() {
    // given
    testRule.deploy(BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, null);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().withoutTenantId().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(null, reportResults.get(0).getTenantId());
  }

  @Test
  public void testReportTenantIdInWithoutTenantId() {
    // given
    testRule.deploy(BPMN_PROCESS);
    testRule.deployForTenant(TENANT_ONE, BPMN_PROCESS);

    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, TENANT_ONE);
    prepareProcessInstances(PROCESS_DEFINITION_KEY, -6, 5, 10, null);

    // when
    List<CleanableHistoricProcessInstanceReportResult> reportResults = historyService.createCleanableHistoricProcessInstanceReport().withoutTenantId().list();
    List<CleanableHistoricProcessInstanceReportResult> reportResultsOne = historyService.createCleanableHistoricProcessInstanceReport().tenantIdIn(TENANT_ONE).list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(null, reportResults.get(0).getTenantId());
    assertEquals(1, reportResultsOne.size());
    assertEquals(TENANT_ONE, reportResultsOne.get(0).getTenantId());
  }

  protected void prepareProcessInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount, String tenantId) {
    List<ProcessDefinition> processDefinitions = null;
    if (tenantId == null) {
      processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).withoutTenantId().list();
    } else {
      processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionKey(key).tenantIdIn(tenantId).list();
    }
    assertEquals(1, processDefinitions.size());
    repositoryService.updateProcessDefinitionHistoryTimeToLive(processDefinitions.get(0).getId(), historyTimeToLive);

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysInThePast));

    List<String> processInstanceIds = new ArrayList<String>();
    {
      for (int i = 0; i < instanceCount; i++) {
        String processInstanceId = null;
        if (tenantId == null) {
          processInstanceId = runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionWithoutTenantId().execute().getId();
        } else {
          processInstanceId = runtimeService.createProcessInstanceByKey(PROCESS_DEFINITION_KEY).processDefinitionTenantId(tenantId).execute().getId();
        }
        processInstanceIds.add(processInstanceId);
      }
    }
    runtimeService.deleteProcessInstances(processInstanceIds, null, true, true);

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

}
