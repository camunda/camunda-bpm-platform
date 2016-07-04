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
import org.camunda.bpm.engine.repository.DecisionRequirementDefinition;
import org.camunda.bpm.engine.repository.DecisionRequirementDefinitionQuery;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProcessEngineTestRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;

public class MultiTenancyDecisionRequirementDefinitionQueryTest {

  protected static final String DECISION_REQUIREMENT_DEFINITION_KEY = "score";
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
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery();

    assertThat(query.count(), is(3L));
  }

  @Test
	public void queryByTenantId() {
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = repositoryService.
        createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryByTenantIds() {
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  @Test
	public void queryByDefinitionsWithoutTenantId() {
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeDecisionRequirementDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeDecisionRequirementDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDecisionRequirementDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));
  }

  @Test
	public void queryByKey() {
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count(), is(1L));

    query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    Map<String, DecisionRequirementDefinition> definitionsForTenant = getDecisionRequirementDefinitionsForTenant(query.list());
    assertThat(definitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(definitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(definitionsForTenant.get(null).getVersion(), is(1));
  }

  @Test
	public void queryByLatestWithTenantId() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    DecisionRequirementDefinition DecisionRequirementDefinition = query.singleResult();
    assertThat(DecisionRequirementDefinition.getTenantId(), is(TENANT_ONE));
    assertThat(DecisionRequirementDefinition.getVersion(), is(2));

    query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));

    DecisionRequirementDefinition = query.singleResult();
    assertThat(DecisionRequirementDefinition.getTenantId(), is(TENANT_TWO));
    assertThat(DecisionRequirementDefinition.getVersion(), is(1));
  }

  @Test
	public void queryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc();
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    Map<String, DecisionRequirementDefinition> definitionsForTenant = getDecisionRequirementDefinitionsForTenant(query.list());
    assertThat(definitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(definitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
  }

  @Test
	public void queryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
    testRule.deploy(DMN);

    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count(), is(1L));

    DecisionRequirementDefinition DecisionRequirementDefinition = query.singleResult();
    assertThat(DecisionRequirementDefinition.getTenantId(), is(nullValue()));
    assertThat(DecisionRequirementDefinition.getVersion(), is(2));
  }

  @Test
	public void queryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
    testRule.deploy(DMN);
    // deploy a third version for tenant one
    testRule.deployForTenant(TENANT_ONE, DMN);
    testRule.deployForTenant(TENANT_ONE, DMN);

    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .decisionRequirementDefinitionKey(DECISION_REQUIREMENT_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeDecisionRequirementDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));

    Map<String, DecisionRequirementDefinition> definitionsForTenant = getDecisionRequirementDefinitionsForTenant(query.list());
    assertThat(definitionsForTenant.get(TENANT_ONE).getVersion(), is(3));
    assertThat(definitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(definitionsForTenant.get(null).getVersion(), is(2));
  }

  @Test
	public void queryByNonExistingTenantId() {
    DecisionRequirementDefinitionQuery query = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  @Test
  public void failQueryByTenantIdNull() {

    thrown.expect(NullValueException.class);

    repositoryService.createDecisionRequirementDefinitionQuery()
      .tenantIdIn((String) null);
  }

  @Test
	public void querySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<DecisionRequirementDefinition> DecisionRequirementDefinitions = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(DecisionRequirementDefinitions.size(), is(2));
    assertThat(DecisionRequirementDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(DecisionRequirementDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  @Test
	public void querySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<DecisionRequirementDefinition> DecisionRequirementDefinitions = repositoryService
        .createDecisionRequirementDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(DecisionRequirementDefinitions.size(), is(2));
    assertThat(DecisionRequirementDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(DecisionRequirementDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

  @Test
	public void queryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();
    assertThat(query.count(), is(1L));
  }

  @Test
	public void queryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeDecisionRequirementDefinitionsWithoutTenantId().count(), is(2L));
  }

  @Test
	public void queryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  @Test
	public void queryDisabledTenantCheck() {
    engineRule.getProcessEngineConfiguration().setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    DecisionRequirementDefinitionQuery query = repositoryService.createDecisionRequirementDefinitionQuery();
    assertThat(query.count(), is(3L));
  }

  protected Map<String, DecisionRequirementDefinition> getDecisionRequirementDefinitionsForTenant(List<DecisionRequirementDefinition> definitions) {
    Map<String, DecisionRequirementDefinition> definitionsForTenant = new HashMap<String, DecisionRequirementDefinition>();

    for (DecisionRequirementDefinition definition : definitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

}
