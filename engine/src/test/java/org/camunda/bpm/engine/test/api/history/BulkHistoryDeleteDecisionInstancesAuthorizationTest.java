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

package org.camunda.bpm.engine.test.api.history;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.history.HistoricDecisionInstance;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.RequiredHistoryLevel;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.VariableMap;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertEquals;

/**
 * @author Svetlana Dorokhova
 */
@RunWith(Parameterized.class)
@RequiredHistoryLevel(ProcessEngineConfiguration.HISTORY_FULL)
public class BulkHistoryDeleteDecisionInstancesAuthorizationTest {

  public static final String DECISION = "decision";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  public ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  private HistoryService historyService;
  private DecisionService decisionService;

  @Before
  public void init() {
    historyService = engineRule.getHistoryService();
    decisionService = engineRule.getDecisionService();

    authRule.createUserAndGroup("demo", "groupId");
  }

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .failsDueToRequired(
                grant(Resources.DECISION_DEFINITION, "*", "demo", Permissions.DELETE_HISTORY)
            )
                ,
        scenario()
            .withAuthorizations(
                grant(Resources.DECISION_DEFINITION, "someId", "demo", Permissions.DELETE_HISTORY)
            )
            .failsDueToRequired(
                grant(Resources.DECISION_DEFINITION, "*", "demo", Permissions.DELETE_HISTORY)
            )
        ,
        scenario()
            .withAuthorizations(
                grant(Resources.DECISION_DEFINITION, "*", "demo", Permissions.DELETE_HISTORY)
            )
            .succeeds()
    );
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  @Deployment(resources = {
      "org/camunda/bpm/engine/test/api/dmn/Example.dmn"})
  public void testCleanupHistory() {
    //given
    final List<String> ids = prepareHistoricDecisions();

    // when
    authRule
        .init(scenario)
        .withUser("demo")
        .start();

    historyService.deleteHistoricDecisionInstancesBulk(ids);

    //then
    if (authRule.assertScenario(scenario)) {
      assertEquals(0, historyService.createHistoricDecisionInstanceQuery().decisionDefinitionKey(DECISION).count());
    }

  }

  private List<String> prepareHistoricDecisions() {
    for (int i = 0; i < 5; i++) {
      decisionService.evaluateDecisionByKey(DECISION).variables(createVariables()).evaluate();
    }
    final List<HistoricDecisionInstance> decisionInstances = historyService.createHistoricDecisionInstanceQuery().list();
    final List<String> decisionInstanceIds = new ArrayList<String>();
    for (HistoricDecisionInstance decisionInstance : decisionInstances) {
      decisionInstanceIds.add(decisionInstance.getId());
    }
    return decisionInstanceIds;
  }

  protected VariableMap createVariables() {
    return Variables.createVariables().putValue("status", "silver").putValue("sum", 723);
  }

}
