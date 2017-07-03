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

package org.camunda.bpm.engine.test.api.authorization.history;

import org.apache.commons.lang.time.DateUtils;
import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.history.CleanableHistoricDecisionInstanceReportResult;
import org.camunda.bpm.engine.impl.util.ClockUtil;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.AuthorizationTest;
import org.camunda.bpm.engine.variable.Variables;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

/**
 * @author Philipp Ossler
 */
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class HistoricDecisionInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "testProcess";
  protected static final String DECISION_DEFINITION_KEY = "testDecision";

  @Override
  public void setUp() throws Exception {
    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml",
        "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn11.xml")
        .getId();
    super.setUp();
  }

  @Override
  public void tearDown() {
    super.tearDown();
    deleteDeployment(deploymentId);
  }

  public void testQueryWithoutAuthorization() {
    // given
    startProcessInstanceAndEvaluateDecision();

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 0);
  }

  public void testQueryWithReadPermissionOnDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, READ_HISTORY);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

  public void testQueryWithReadPermissionOnAnyDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, READ_HISTORY);

    // when
    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery();

    // then
    verifyQueryResults(query, 1);
  }

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
      assertThat(e.getMessage(),
          is("The user with id 'test' does not have 'DELETE_HISTORY' permission on resource 'testDecision' of type 'DecisionDefinition'."));
    }
  }

  public void testDeleteHistoricDecisionInstanceWithDeleteHistoryPermissionOnDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, ANY, userId, DELETE_HISTORY);
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();


    // when
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);

    // then
    disableAuthorization();
    assertThat(historyService.createHistoricDecisionInstanceQuery().count(), is(0L));
    enableAuthorization();
}

  public void testDeleteHistoricDecisionInstanceWithDeleteHistoryPermissionOnAnyDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, DELETE_HISTORY);
    String decisionDefinitionId = selectDecisionDefinitionByKey(DECISION_DEFINITION_KEY).getId();

    // when
    historyService.deleteHistoricDecisionInstanceByDefinitionId(decisionDefinitionId);

    // then
    disableAuthorization();
    assertThat(historyService.createHistoricDecisionInstanceQuery().count(), is(0L));
    enableAuthorization();
  }

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
      assertThat(e.getMessage(),
          is("The user with id 'test' does not have 'DELETE_HISTORY' permission on resource 'testDecision' of type 'DecisionDefinition'."));
    }
  }

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

  public void testHistoryCleanupReportWithoutAuthorization() {
    // given
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

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

  public void testHistoryCleanupReportWithReadPermissionOnly() {
    // given
    prepareDecisionInstances(DECISION_DEFINITION_KEY, -6, 5, 10);

    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, Permissions.READ);

    // when
    List<CleanableHistoricDecisionInstanceReportResult> reportResults = historyService.createCleanableHistoricDecisionInstanceReport().list();

    // then
    assertEquals(0, reportResults.size());
  }

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
