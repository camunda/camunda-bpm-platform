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

import static org.camunda.bpm.engine.authorization.Authorization.ANY;
import static org.camunda.bpm.engine.authorization.Resources.DECISION_REQUIREMENTS_DEFINITION;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
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

@RunWith(Parameterized.class)
public class DecisionRequirementsDefinitionQueryAuthorizationTest {

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";
  protected static final String ANOTHER_DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml";

  protected static final String DEFINITION_KEY = "score";
  protected static final String ANOTHER_DEFINITION_KEY = "dish";

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  protected RepositoryService repositoryService;

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter(0)
  public AuthorizationScenario scenario;

  @Parameter(1)
  public String[] expectedDefinitionKeys;

  @Parameters(name = "scenario {index}")
  public static Collection<Object[]> scenarios() {
    return Arrays.asList(new Object[][] {
      { scenario()
          .withoutAuthorizations()
          .succeeds(), expectedDefinitions() },
      { scenario()
          .withAuthorizations(
           grant(DECISION_REQUIREMENTS_DEFINITION, DEFINITION_KEY, "userId", Permissions.READ))
          .succeeds(), expectedDefinitions(DEFINITION_KEY) },
      { scenario()
        .withAuthorizations(
          grant(DECISION_REQUIREMENTS_DEFINITION, ANY, "userId", Permissions.READ))
        .succeeds(), expectedDefinitions(DEFINITION_KEY, ANOTHER_DEFINITION_KEY) },
      { scenario()
          .withAuthorizations(
            grant(DECISION_REQUIREMENTS_DEFINITION, DEFINITION_KEY, "userId", Permissions.READ),
            grant(DECISION_REQUIREMENTS_DEFINITION, ANY, "userId", Permissions.READ))
          .succeeds(), expectedDefinitions(DEFINITION_KEY, ANOTHER_DEFINITION_KEY) }
    });
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
  @Deployment(resources = { DMN_FILE, ANOTHER_DMN })
  public void queryDecisionRequirementsDefinitions() {

    // when
    authRule.init(scenario).withUser("userId").bindResource("decisionRequirementsDefinitionKey", DEFINITION_KEY).start();

    DecisionRequirementsDefinitionQuery query = engineRule.getRepositoryService().createDecisionRequirementsDefinitionQuery();
    long count = query.count();

    // then
    if (authRule.assertScenario(scenario)) {
      assertThat(count, is((long) expectedDefinitionKeys.length));

      List<String> definitionKeys = getDefinitionKeys(query.list());
      assertThat(definitionKeys, hasItems(expectedDefinitionKeys));
    }
  }

  protected List<String> getDefinitionKeys(List<DecisionRequirementsDefinition> definitions) {
    List<String> definitionKeys = new ArrayList<String>();
    for (DecisionRequirementsDefinition definition : definitions) {
      definitionKeys.add(definition.getKey());
    }
    return definitionKeys;
  }

  protected static String[] expectedDefinitions(String... keys) {
    return keys;
  }

}
