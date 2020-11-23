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
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 *
 * @author Deivarayan Azhagappan
 *
 */

public class MultiTenancyDecisionRequirementsDefinitionCmdsTenantCheckTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String DRG_DMN = "org/camunda/bpm/engine/test/api/multitenancy/DecisionRequirementsGraph.dmn";
  protected static final String DRD_DMN = "org/camunda/bpm/engine/test/api/multitenancy/DecisionRequirementsGraph.png";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  protected String decisionRequirementsDefinitionId;

  protected RepositoryService repositoryService;
  protected IdentityService identityService;
  protected ProcessEngineConfiguration processEngineConfiguration;

  @Before
  public void setUp() {
    processEngineConfiguration = engineRule.getProcessEngineConfiguration();
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();

    testRule.deployForTenant(TENANT_ONE, DRG_DMN, DRD_DMN);
    decisionRequirementsDefinitionId = repositoryService.createDecisionRequirementsDefinitionQuery()
      .singleResult().getId();
  }

  @Test
  public void failToGetDecisionRequirementsDefinitionNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.getDecisionRequirementsDefinition(decisionRequirementsDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the decision requirements definition");
  }

  @Test
  public void getDecisionRequirementsDefinitionWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DecisionRequirementsDefinition definition = repositoryService.getDecisionRequirementsDefinition(decisionRequirementsDefinitionId);

    assertThat(definition.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void getDecisionRequirementsDefinitionDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DecisionRequirementsDefinition definition = repositoryService.getDecisionRequirementsDefinition(decisionRequirementsDefinitionId);

    assertThat(definition.getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void failToGetDecisionRequirementsModelNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.getDecisionRequirementsModel(decisionRequirementsDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the decision requirements definition");
  }

  @Test
  public void getDecisionRequirementsModelWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getDecisionRequirementsModel(decisionRequirementsDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void getDecisionRequirementsModelDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getDecisionRequirementsModel(decisionRequirementsDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void failToGetDecisionRequirementsDiagramNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    // when/then
    assertThatThrownBy(() -> repositoryService.getDecisionRequirementsDiagram(decisionRequirementsDefinitionId))
      .isInstanceOf(ProcessEngineException.class)
      .hasMessageContaining("Cannot get the decision requirements definition");
  }

  @Test
  public void getDecisionDiagramWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    InputStream inputStream = repositoryService.getDecisionRequirementsDiagram(decisionRequirementsDefinitionId);

    assertThat(inputStream).isNotNull();
  }

  @Test
  public void getDecisionDiagramDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    InputStream inputStream = repositoryService.getDecisionRequirementsDiagram(decisionRequirementsDefinitionId);

    assertThat(inputStream).isNotNull();
  }

}
