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

package org.camunda.bpm.engine.test.authorization.history;

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Permissions.DELETE_HISTORY;
import static org.camunda.bpm.engine.authorization.Permissions.READ_HISTORY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_DEFINITION;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.HashMap;
import java.util.Map;

import org.camunda.bpm.engine.AuthorizationException;
import org.camunda.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.camunda.bpm.engine.test.authorization.AuthorizationTest;

/**
 * @author Philipp Ossler
 */
public class HistoricDecisionInstanceAuthorizationTest extends AuthorizationTest {

  protected static final String PROCESS_KEY = "testProcess";
  protected static final String DECISION_DEFINITION_KEY = "testDecision";

  @Override
  public void setUp() throws Exception {
    super.setUp();

    deploymentId = createDeployment(null,
        "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.processWithBusinessRuleTask.bpmn20.xml",
        "org/camunda/bpm/engine/test/history/HistoricDecisionInstanceTest.decisionSingleOutput.dmn10.xml")
        .getId();
  }

  @Override
  public void tearDown() {
    deleteDeployment(deploymentId);

    super.tearDown();
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

  public void testDeleteHistoricDecisionInstanceWithoutAuthorization(){
    // given
    startProcessInstanceAndEvaluateDecision();

    try {
      // when
      historyService.deleteHistoricDecisionInstance(DECISION_DEFINITION_KEY);
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

    // when
    historyService.deleteHistoricDecisionInstance(DECISION_DEFINITION_KEY);

    // then
    disableAuthorization();
    assertThat(historyService.createHistoricDecisionInstanceQuery().count(), is(0L));
    enableAuthorization();
}

  public void testDeleteHistoricDecisionInstanceWithDeleteHistoryPermissionOnAnyDecisionDefinition() {
    // given
    startProcessInstanceAndEvaluateDecision();
    createGrantAuthorization(DECISION_DEFINITION, DECISION_DEFINITION_KEY, userId, DELETE_HISTORY);

    // when
    historyService.deleteHistoricDecisionInstance(DECISION_DEFINITION_KEY);

    // then
    disableAuthorization();
    assertThat(historyService.createHistoricDecisionInstanceQuery().count(), is(0L));
    enableAuthorization();
  }

  protected void startProcessInstanceAndEvaluateDecision() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("input1", null);
    startProcessInstanceByKey(PROCESS_KEY, variables);
  }

}
