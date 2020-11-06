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
package org.camunda.bpm.engine.test.api.authorization.history;

import static org.assertj.core.api.Assertions.assertThat;
import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.time.DateUtils;
import org.camunda.bpm.dmn.engine.impl.DefaultDmnEngineConfiguration;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.test.util.ResetDmnConfigUtil;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Philipp Ossler
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "testProcess";
  protected static final String DECISION_DEFINITION_KEY = "testDecision";

  @Before
  public void setUp() throws Exception {
    testRule.deploy("org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml",
        "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml");

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(true)
        .init();
    super.setUp();
  }

  @After
  public void tearDown() {
    super.tearDown();

    DefaultDmnEngineConfiguration dmnEngineConfiguration =
        processEngineConfiguration.getDmnEngineConfiguration();

    ResetDmnConfigUtil.reset(dmnEngineConfiguration)
        .enableFeelLegacyBehavior(false)
        .init();
  }

  @Test
  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceAndEvaluateDecision();

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testQueryWithReadPermissionOnDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ_HISTORY);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryWithReadPermissionOnAnyDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testQueryWithMultiple() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ_HISTORY);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  @Test
  public void testDeleteHistoricDecisionInstanceWithoutAuthorization(){
    // given
    startProcessInstanceAndEvaluateDecision();
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    try {
      // when
      historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);
      fail("expect authorization exception");
    } catch (AuthorizationException e) {
      // then
      assertThat(e.getMessage()).isEqualTo(
          "The user with id 'test' does not have 'DELETE_HISTORY' permission on resource 'testDecision' of type 'DecisionDefinition'.");
    }
  }

  @Test
  public void testDeleteHistoricDecisionInstanceWithDeleteHistoryPermissionOnDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, DELETE_HISTORY);
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();


    // when
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);

    // then
    disableAuthorization();
    assertThat(historyService.createHistoricDecisionInstanceQuery().count()).isEqualTo(0L);
    enableAuthorization();
}

  @Test
  public void testDeleteHistoricDecisionInstanceWithDeleteHistoryPermissionOnAnyDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, DELETE_HISTORY);
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    // when
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);

    // then
    disableAuthorization();
    assertThat(historyService.createHistoricDecisionInstanceQuery().count()).isEqualTo(0L);
    enableAuthorization();
  }

  @Test
  public void testDeleteHistoricDecisionInstanceByInstanceIdWithoutAuthorization() {

    // given
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ_HISTORY);
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    HistoricDecisionInstance historicDecisionInstance = query.includeInputs().includeOutputs().singleResult();

    try {
      // when
      historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());
      fail("expect authorization exception");
    } catch (AuthorizationException e) {
      // then
      assertThat(e.getMessage()).isEqualTo(
          "The user with id 'test' does not have 'DELETE_HISTORY' permission on resource 'testDecision' of type 'DecisionDefinition'.");
    }
  }

  @Test
  public void testDeleteHistoricDecisionInstanceByInstanceIdWithDeleteHistoryPermissionOnDecisionDefinition() {

    // given
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, DELETE_HISTORY, READ_HISTORY);
    startProcessInstanceAndEvaluateDecision();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();
    verifyQueryResults(query, 1);
    HistoricDecisionInstance historicDecisionInstance = query.includeInputs().includeOutputs().singleResult();

    // when
    historyService.deleteHistoricDecisionInstanceByInstanceId(historicDecisionInstance.getId());

    // then
    verifyQueryResults(query, 0);
  }

  @Test
  public void testHistoryCleanupReportWithoutAuthorization() {
    // given
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testHistoryCleanupReportWithAuthorization() {
    // given
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10);

    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, Permissions.READ, Permissions.READ_HISTORY);
    createGrantAuthorizationGroup(DECISION_DEFINITION, DECISION_DEFINITION_KEY, groupId, Permissions.READ, Permissions.READ_HISTORY);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(1, reportResults.size());
    assertEquals(10, reportResults.get(0).getCleanableDecisionInstanceCount());
    assertEquals(10, reportResults.get(0).getFinishedDecisionInstanceCount());
  }

  @Test
  public void testHistoryCleanupReportWithReadPermissionOnly() {
    // given
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10);

    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, Permissions.READ);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  @Test
  public void testHistoryCleanupReportWithReadHistoryPermissionOnly() {
    // given
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10);

    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, Permissions.READ_HISTORY);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

  protected void startProcessInstanceAndEvaluateDecision() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", null);
    startProcessInstanceByKey(PROCESS_KEY, variables);
  }

  protected void prepareDecisionInstances(String key, int daysInThePast, Integer historyTimeToLive, int instanceCount) {
    DecisionDefinition decisionDefinition = selectDecisionDefinitionByKey(key);
    disableAuthorization();
    repositoryService.updateDecisionDefinitionHistoryTimeToLive(decisionDefinition.getId(), historyTimeToLive);
    enableAuthorization();

    Date oldCurrentTime = ClockUtil.getCurrentTime();
    ClockUtil.setCurrentTime(DateUtils.addDays(oldCurrentTime, daysInThePast));

    Map<String, Object> variables = Variables.createVariables().putValue("input1", null);
    for (int i = 0; i < instanceCount; i++) {
      disableAuthorization();
      decisionService.evaluateDecisionByKey(key).variables(variables).evaluate();
      enableAuthorization();
    }

    ClockUtil.setCurrentTime(oldCurrentTime);
  }

}
