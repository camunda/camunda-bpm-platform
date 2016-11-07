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

import org.camunda.bpm.engine.DecisionService;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.engine.variable.Variables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

/**
 * @author Askar Akhmerov
 */
@RunWith(Parameterized.class)
public class HistoricDecisionInstanceStatisticsAuthorizationTest {

  protected static final String DISH_DRG_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);
  protected ProcessEngineTestRule testHelper = new ProcessEngineTestRule(engineRule);
  protected DecisionService decisionService;
  protected HistoryService historyService;
  protected RepositoryService repositoryService;

  protected DecisionRequirementsDefinition decisionRequirementsDefinition;

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(authRule).around(testHelper);

  @Parameterized.Parameter
  public AuthorizationScenario scenario;

  @Parameterized.Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
        scenario()
            .withoutAuthorizations()
            .failsDueToRequired(
                grant(Resources.DECISION_REQUIREMENTS_DEFINITION, "dish", "userId", Permissions.READ)
            ),
        scenario()
            .withAuthorizations(
                grant(Resources.DECISION_REQUIREMENTS_DEFINITION, "drd", "userId", Permissions.READ)
            ).succeeds()
    );
  }

  @Before
  public void setUp() {
    testHelper.deploy(DISH_DRG_DMN);
    decisionService = engineRule.getDecisionService();
    historyService = engineRule.getHistoryService();
    repositoryService = engineRule.getRepositoryService();

    authRule.createUserAndGroup("userId", "groupId");

    decisionService.evaluateDecisionTableByKey("dish-decision")
        .variables(Variables.createVariables().putValue("temperature", 21).putValue("dayType", "Weekend"))
        .evaluate();

    decisionRequirementsDefinition = repositoryService.createDecisionRequirementsDefinitionQuery().singleResult();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  public void testCreateStatistics() {
    //given
    authRule
        .init(scenario)
        .withUser("userId")
        .bindResource("drd", "*")
        .start();

    // when
    historyService.createHistoricDecisionInstanceStatisticsQuery(
        decisionRequirementsDefinition.getId()).list();

    // then
    authRule.assertScenario(scenario);
  }

}
