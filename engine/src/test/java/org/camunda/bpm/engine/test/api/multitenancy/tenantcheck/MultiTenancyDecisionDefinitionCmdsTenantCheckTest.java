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

package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.DecisionDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.dmn.DmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author kristin.polenz
 */
public class MultiTenancyDecisionDefinitionCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String DMN_MODEL = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected DecisionDefinition decisionDefinition;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE, DMN_MODEL);

    decisionDefinition = repositoryService.createDecisionDefinitionQuery().singleResult();

  }

  @Test
  public void failToGetDecisionModelNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the decision definition");

    repositoryService.getDecisionModel(decisionDefinition.getId());
  }

  @Test
  public void getDecisionModelWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getDecisionModel(decisionDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getDecisionModelDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getDecisionModel(decisionDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetDecisionDiagramNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the decision definition");

    repositoryService.getDecisionDiagram(decisionDefinition.getId());
  }

  @Test
  public void getDecisionDiagramWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getDecisionDiagram(decisionDefinition.getId());

    // inputStream is always null because there is no decision diagram at the moment
    // what should be deployed as a diagram resource for DMN? 
    assertThat(inputStream, nullValue());
  }

  @Test
  public void getDecisionDiagramDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getDecisionDiagram(decisionDefinition.getId());

    // inputStream is always null because there is no decision diagram at the moment
    // what should be deployed as a diagram resource for DMN? 
    assertThat(inputStream, nullValue());
  }

  @Test
  public void failToGetDecisionDefinitionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the decision definition");

    repositoryService.getDecisionDefinition(decisionDefinition.getId());
  }

  @Test
  public void getDecisionDefinitionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DecisionDefinition definition = repositoryService.getDecisionDefinition(decisionDefinition.getId());

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void getDecisionDefinitionDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DecisionDefinition definition = repositoryService.getDecisionDefinition(decisionDefinition.getId());

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void failToGetDmnModelInstanceNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the decision definition");

    repositoryService.getDmnModelInstance(decisionDefinition.getId());
  }

  @Test
  public void getDmnModelInstanceWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DmnModelInstance modelInstance = repositoryService.getDmnModelInstance(decisionDefinition.getId());

    assertThat(modelInstance, notNullValue());
  }

  @Test
  public void getDmnModelInstanceDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DmnModelInstance modelInstance = repositoryService.getDmnModelInstance(decisionDefinition.getId());

    assertThat(modelInstance, notNullValue());
  }

}
