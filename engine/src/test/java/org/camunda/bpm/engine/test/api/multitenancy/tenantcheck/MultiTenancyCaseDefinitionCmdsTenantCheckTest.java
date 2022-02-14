/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.api.multitenancy.tenantcheck;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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

    // when/then
    assertThatThrownBy(() -> repositoryService.getCaseModel(caseDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the case definition");
  }

  @Test
  public void getCaseModelWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getCaseModel(caseDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void getCaseModelDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getCaseModel(caseDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void failToGetCaseDiagramNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.getCaseDiagram(caseDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the case definition");
  }

  @Test
  public void getCaseDiagramWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getCaseDiagram(caseDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void getCaseDiagramDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getCaseDiagram(caseDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void failToGetCaseDefinitionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.getCaseDefinition(caseDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the case definition");

  }

  @Test
  public void getCaseDefinitionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void getCaseDefinitionDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void failToGetCmmnModelInstanceNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.getCmmnModelInstance(caseDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the case definition");
  }

  @Test
  public void getCmmnModelInstanceWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinitionId);

    assertThat(modelInstance).isNotNull();
  }

  @Test
  public void getCmmnModelInstanceDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CmmnModelInstance modelInstance = repositoryService.getCmmnModelInstance(caseDefinitionId);

    assertThat(modelInstance).isNotNull();
  }

  @Test
  public void updateHistoryTimeToLiveWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, 6);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(definition.getHistoryTimeToLive()).isEqualTo(6);
  }

  @Test
  public void updateHistoryTimeToLiveDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, 6);

    CaseDefinition definition = repositoryService.getCaseDefinition(caseDefinitionId);

    assertThat(definition.getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(definition.getHistoryTimeToLive()).isEqualTo(6);
  }

  @Test
  public void updateHistoryTimeToLiveNoAuthenticatedTenants(){
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.updateCaseDefinitionHistoryTimeToLive(caseDefinitionId, 6))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot update the case definition");
  }

}
