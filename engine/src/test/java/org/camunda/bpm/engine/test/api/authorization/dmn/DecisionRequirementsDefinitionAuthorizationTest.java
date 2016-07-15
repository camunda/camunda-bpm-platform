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

package org.camunda.bpm.engine.test.api.authorization.dmn;

import static org.camunda.bpm.engine.authorization.Resources.DECISION_REQUIREMENTS_DEFINITION;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.util.Collection;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

/**
 * 
 * @author Deivarayan Azhagappan
 *
 */

@RunWith(Parameterized.class)
public class DecisionRequirementsDefinitionAuthorizationTest {

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";
  protected static final String DRD_FILE = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.png";
 
  protected static final String DEFINITION_KEY = "dish";
 
  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  protected RepositoryService repositoryService;

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter(0)
  public AuthorizationScenario scenario;

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(DECISION_REQUIREMENTS_DEFINITION, DEFINITION_KEY, "userId", Permissions.READ)),
      scenario()
        .withAuthorizations(
          grant(DECISION_REQUIREMENTS_DEFINITION, DEFINITION_KEY, "userId", Permissions.READ))
          .succeeds(),
      scenario()
          .withAuthorizations(
            grant(DECISION_REQUIREMENTS_DEFINITION, "*", "userId", Permissions.READ))
            .succeeds()
      );
  }

  @Before
  public void setUp() throws Exception {
    authRule.createUserAndGroup("userId", "groupId");
    repositoryService = engineRule.getRepositoryService();
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();
  }

  @Test
  @Deployment(resources = { DMN_FILE })
  public void getDecisionRequirementsDefinition() {

    String decisionRequirementsDefinitionId = repositoryService
      .createDecisionRequirementsDefinitionQuery()
      .decisionRequirementsDefinitionKey(DEFINITION_KEY)
      .singleResult().getId();
    
    // when
    authRule.init(scenario).withUser("userId").bindResource("decisionRequirementsDefinitionKey", DEFINITION_KEY).start();

    DecisionRequirementsDefinition decisionRequirementsDefinition = repositoryService.getDecisionRequirementsDefinition(decisionRequirementsDefinitionId);

    if (authRule.assertScenario(scenario)) {
      assertNotNull(decisionRequirementsDefinition);
    }
  }

  @Test
  @Deployment(resources = { DMN_FILE })
  public void getDecisionRequirementsModel() {

    // given
    String decisionRequirementsDefinitionId = repositoryService
      .createDecisionRequirementsDefinitionQuery()
      .decisionRequirementsDefinitionKey(DEFINITION_KEY)
      .singleResult().getId();

    // when
    authRule.init(scenario).withUser("userId").bindResource("decisionRequirementsDefinitionKey", DEFINITION_KEY).start();

    InputStream decisionRequirementsModel = repositoryService.getDecisionRequirementsModel(decisionRequirementsDefinitionId);

    if (authRule.assertScenario(scenario)) {
      assertNotNull(decisionRequirementsModel);
    }
  }

  @Test
  @Deployment(resources = { DMN_FILE, DRD_FILE })
  public void getDecisionRequirementsDiagram() {

    // given
    String decisionRequirementsDefinitionId = repositoryService
      .createDecisionRequirementsDefinitionQuery()
      .decisionRequirementsDefinitionKey(DEFINITION_KEY)
      .singleResult().getId();

    // when
    authRule.init(scenario).withUser("userId").bindResource("decisionRequirementsDefinitionKey", DEFINITION_KEY).start();

    InputStream decisionRequirementsDiagram = repositoryService.getDecisionRequirementsDiagram(decisionRequirementsDefinitionId);

    if (authRule.assertScenario(scenario)) {
      assertNotNull(decisionRequirementsDiagram);
    }
  }
}
