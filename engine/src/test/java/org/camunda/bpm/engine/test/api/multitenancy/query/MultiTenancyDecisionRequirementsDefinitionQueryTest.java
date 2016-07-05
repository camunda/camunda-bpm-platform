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

package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.IdentityService;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementsDefinitionQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyDecisionRequirementsDefinitionQueryTest {

  protected static final String DECISION_REQUIREMENTS_DEFINITION_KEY = "score";
  protected static final String DMN = "org/camunda/bpm/engine/test/dmn/deployment/drdScore.dmn11.xml";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected ProcessEngineRule engineRule = new ProvidedProcessEngineRule();
  protected ProcessEngineTestRule testRule = new ProcessEngineTestRule(engineRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(testRule);

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  protected RepositoryService repositoryService;
  protected IdentityService identityService;

  @Before
  public void setUp() {
    repositoryService = engineRule.getRepositoryService();
    identityService = engineRule.getIdentityService();

    testRule.deploy(DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_TWO, DMN);
  }

  @Test
	public void queryNoTenantIdSet() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery();

    assertThat(query.count(), is(3L));
  }

  @Test
	public void queryByTenantId() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = repositoryService.
        createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryByTenantIds() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  @Test
	public void queryByDefinitionsWithoutTenantId() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeDecisionRequirementsDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeDecisionRequirementsDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDecisionRequirementsDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));
  }

  @Test
	public void queryByKey() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count(), is(1L));

    query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    Map<String, DecisionRequirementsDefinition> definitionsForTenant = getDecisionRequirementsDefinitionsForTenant(query.list());
    assertThat(definitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(definitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(definitionsForTenant.get(null).getVersion(), is(1));
  }

  @Test
	public void queryByLatestWithTenantId() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    DecisionRequirementsDefinition DecisionRequirementsDefinition = query.singleResult();
    assertThat(DecisionRequirementsDefinition.getTenantId(), is(TENANT_ONE));
    assertThat(DecisionRequirementsDefinition.getVersion(), is(2));

    query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));

    DecisionRequirementsDefinition = query.singleResult();
    assertThat(DecisionRequirementsDefinition.getTenantId(), is(TENANT_TWO));
    assertThat(DecisionRequirementsDefinition.getVersion(), is(1));
  }

  @Test
	public void queryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    Map<String, DecisionRequirementsDefinition> definitionsForTenant = getDecisionRequirementsDefinitionsForTenant(query.list());
    assertThat(definitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(definitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
  }

  @Test
	public void queryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
    testRule.deploy(DMN);

    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count(), is(1L));

    DecisionRequirementsDefinition DecisionRequirementsDefinition = query.singleResult();
    assertThat(DecisionRequirementsDefinition.getTenantId(), is(nullValue()));
    assertThat(DecisionRequirementsDefinition.getVersion(), is(2));
  }

  @Test
	public void queryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
    testRule.deploy(DMN);
    // deploy a third version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .decisionRequirementsDefinitionKey(DECISION_REQUIREMENTS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDecisionRequirementsDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));

    Map<String, DecisionRequirementsDefinition> definitionsForTenant = getDecisionRequirementsDefinitionsForTenant(query.list());
    assertThat(definitionsForTenant.get(TENANT_ONE).getVersion(), is(3));
    assertThat(definitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(definitionsForTenant.get(null).getVersion(), is(2));
  }

  @Test
	public void queryByNonExistingTenantId() {
    DecisionRequirementsDefinitionQuery query = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  @Test
  public void failQueryByTenantIdNull() {

    thrown.expect(NullValueException.class);

    repositoryService.createDecisionRequirementsDefinitionQuery()
      .tenantIdIn((String) null);
  }

  @Test
	public void querySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<DecisionRequirementsDefinition> DecisionRequirementsDefinitions = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(DecisionRequirementsDefinitions.size(), is(2));
    assertThat(DecisionRequirementsDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(DecisionRequirementsDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  @Test
	public void querySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<DecisionRequirementsDefinition> DecisionRequirementsDefinitions = repositoryService
        .createDecisionRequirementsDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(DecisionRequirementsDefinitions.size(), is(2));
    assertThat(DecisionRequirementsDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(DecisionRequirementsDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

  @Test
	public void queryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();
    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeDecisionRequirementsDefinitionsWithoutTenantId().count(), is(2L));
  }

  @Test
	public void queryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  @Test
	public void queryDisabledTenantCheck() {
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DecisionRequirementsDefinitionQuery query = repositoryService.createDecisionRequirementsDefinitionQuery();
    assertThat(query.count(), is(3L));
  }

  protected Map<String, DecisionRequirementsDefinition> getDecisionRequirementsDefinitionsForTenant(List<DecisionRequirementsDefinition> definitions) {
    Map<String, DecisionRequirementsDefinition> definitionsForTenant = new HashMap<String, DecisionRequirementsDefinition>();

    for (DecisionRequirementsDefinition definition : definitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

}
