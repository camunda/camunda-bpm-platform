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
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
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

  protected static final String CMMN_MODEL = "org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.cmmn";
  protected static final String CMMN_DIAGRAM = "org/camunda/bpm/engine/test/api/cmmn/emptyStageCase.png";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();

  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown= ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  protected String caseDefinitionId;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE, CMMN_MODEL, CMMN_DIAGRAM);

    caseDefinitionId = repositoryService.createCaseDefinitionQuery().singleResult().getId();
  }

  @Test
  public void failToGetCaseModelNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCaseModel(caseDefinitionId);
  }

  @Test
  public void getCaseModelWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getCaseModel(caseDefinitionId);

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getCaseModelDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getCaseModel(caseDefinitionId);

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetCaseDiagramNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCaseDiagram(caseDefinitionId);
  }

  @Test
  public void getCaseDiagramWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getCaseDiagram(caseDefinitionId);

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void getCaseDiagramDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getCaseDiagram(caseDefinitionId);

    assertThat(inputStream, notNullValue());
  }

  @Test
  public void failToGetCaseDefinitionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCaseDefinition(caseDefinitionId);
  }

  @Test
  public void getCaseDefinitionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void getCaseDefinitionDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId(), is(TENANT_ONE));
  }

  @Test
  public void failToGetCmmnModelInstanceNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot get the case definition");

    repositoryService.getCmmnModelInstance(caseDefinitionId);
  }

  @Test
  public void getCmmnModelInstanceWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinitionId);

    assertThat(modelInstance, notNullValue());
  }

  @Test
  public void getCmmnModelInstanceDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinitionId);

    assertThat(modelInstance, notNullValue());
  }

  @Test
  public void updateHistoryTimeToLiveWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, 6);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId(), is(TENANT_ONE));
    assertThat(definition.getHistoryTimeToLive(), is(6));
  }

  @Test
  public void updateHistoryTimeToLiveDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, 6);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId(), is(TENANT_ONE));
    assertThat(definition.getHistoryTimeToLive(), is(6));
  }

  @Test
  public void updateHistoryTimeToLiveNoAuthenticatedTenants(){
    identityService.setAuthentication("user", null, null);

    // declare expected exception
    thrown.expect(ProcessEngineException.class);
    thrown.expectMessage("Cannot update the case definition");

    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, 6);
  }

}
