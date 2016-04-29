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
package org.camunda.bpm.engine.test.api.authorization.deployment;

import java.util.Collection;

import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario.scenario;
import static org.camunda.bpm.engine.test.api.authorization.util.AuthorizationSpec.grant;

import org.camunda.bpm.engine.AuthorizationService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.authorization.Authorization;
import org.camunda.bpm.engine.authorization.Permissions;
import org.camunda.bpm.engine.authorization.Resources;
import org.camunda.bpm.engine.repository.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationScenario;
import org.camunda.bpm.engine.test.api.authorization.util.AuthorizationTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;


/**
 * @author Roman Smirnov
 *
 */
@RunWith(Parameterized.class)
public class RedeployDeploymentAuthorizationTest {

  public ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  public AuthorizationTestRule authRule = new AuthorizationTestRule(engineRule);

  @Rule
  public RuleChain chain = RuleChain.outerRule(engineRule).around(authRule);

  @Parameter
  public AuthorizationScenario scenario;

  @Parameters(name = "Scenario {index}")
  public static Collection<AuthorizationScenario[]> scenarios() {
    return AuthorizationTestRule.asParameters(
      scenario()
        .withoutAuthorizations()
        .failsDueToRequired(
          grant(Resources.DEPLOYMENT, "*", "userId", Permissions.CREATE)),
      scenario()
        .withAuthorizations(
          grant(Resources.DEPLOYMENT, "*", "userId", Permissions.CREATE))
        .failsDueToRequired(
          grant(Resources.DEPLOYMENT, "deploymentId", "userId", Permissions.READ)),
      scenario()
        .withAuthorizations(
          grant(Resources.DEPLOYMENT, "deploymentId", "userId", Permissions.READ),
          grant(Resources.DEPLOYMENT, "*", "userId", Permissions.CREATE))
        .succeeds()
      );
  }

  @Before
  public void setUp() {
    authRule.createUserAndGroup("userId", "groupId");
  }

  @After
  public void tearDown() {
    authRule.deleteUsersAndGroups();

  }

  @Test
  public void testRedeploy() {
    // given
    RepositoryService repositoryService = engineRule.getRepositoryService();

    BpmnModelInstance model1 = Bpmn.createExecutableProcess("process1").done();
    BpmnModelInstance model2 = Bpmn.createExecutableProcess("process2").done();

    // first deployment
    Deployment deployment1 = repositoryService
        .createDeployment()
        .addModelInstance("process1.bpmn", model1)
        .addModelInstance("process2.bpmn", model2)
        .deploy();

    // when
    authRule
      .init(scenario)
      .withUser("userId")
      .bindResource("deploymentId", deployment1.getId())
      .start();

    Deployment deployment2 = repositoryService
      .createDeployment()
      .addDeploymentResources(deployment1.getId())
      .deploy();

    // then
    if (authRule.assertScenario(scenario)) {
      Assert.assertEquals(2, repositoryService.createDeploymentQuery().count());
      deleteDeployments(deployment2);
      deleteAuthorizations();
    }

    deleteDeployments(deployment1);
  }

  protected void deleteDeployments(Deployment... deployments){
    for (Deployment deployment : deployments) {
      engineRule.getRepositoryService().deleteDeployment(deployment.getId(), true);
    }
  }

  protected void deleteAuthorizations() {
    AuthorizationService authorizationService = engineRule.getAuthorizationService();
    for (Authorization authorization : authorizationService.createAuthorizationQuery().list()) {
      authorizationService.deleteAuthorization(authorization.getId());
    }
  }

}
