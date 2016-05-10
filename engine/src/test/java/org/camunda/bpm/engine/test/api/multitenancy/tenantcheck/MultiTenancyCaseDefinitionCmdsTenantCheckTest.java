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
import static org.junit.Assert.assertThat;

import java.io.InputStream;
import java.util.Arrays;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.model.cmmn.CmmnModelInstance;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

/**
 * @author kristin.polenz
 */
public class MultiTenancyCaseDefinitionCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String CMMN_MODEL = "org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.cmmn";
  protected static final String CMMN_DIAGRAM = "org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.png";

  protected ProcessEngineRule engineRule = new ProcessEngineRule(true);

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected CaseDefinition caseDefinition;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL, CMMN_DIAGRAM);

    caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
  }

  @Test
  public void failToGetCaseModelNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCaseModel(caseDefinition.getId());
  }

  @Test
  public void getCaseModelWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getCaseModel(caseDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getDecisionModelDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getCaseModel(caseDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetCaseDiagramNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCaseDiagram(caseDefinition.getId());
  }

  @Test
  public void getDecisionDiagramWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getCaseDiagram(caseDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getCaseDiagramDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getCaseDiagram(caseDefinition.getId());

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetCaseDefinitionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCaseDefinition(caseDefinition.getId());
  }

  @Test
  public void getCaseDefinitionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinition.getId());

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void getCaseDefinitionDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinition.getId());

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void failToGetCmmnModelInstanceNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCmmnModelInstance(caseDefinition.getId());
  }

  @Test
  public void getCmmnModelInstanceWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinition.getId());

    assertThat(modelInstance, notNullValue());
  }

  @Test
  public void getCmmnModelInstanceDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinition.getId());

    assertThat(modelInstance, notNullValue());
  }

}
