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

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyProcessDefinitionQueryTest extends PluggableProcessEngineTestCase {

  protected static final String PROCESS_DEFINITION_KEY = "process";
  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).done();

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Override
  protected void setUp() {
    deployment(emptyProcess);
    deploymentForTenant(TENANT_ONE, emptyProcess);
    deploymentForTenant(TENANT_TWO, emptyProcess);
  }

  public void testQueryNoTenantIdSet() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    query = repositoryService.
        createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIds() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count(), is(2L));
  }

  public void testQueryByDefinitionsWithoutTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .withoutTenantId();

    assertThat(query.count(), is(1L));
  }

  public void testQueryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count(), is(2L));

    query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));
  }

  public void testQueryByKey() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count(), is(1L));

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count(), is(1L));
  }

  public void testQueryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count(), is(3L));

    Map<String, ProcessDefinition> processDefinitionsForTenant = getProcessDefinitionsForTenant(query.list());
    assertThat(processDefinitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(processDefinitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(processDefinitionsForTenant.get(null).getVersion(), is(1));
  }

  public void testQueryByLatestWithTenantId() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count(), is(1L));

    ProcessDefinition processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId(), is(TENANT_ONE));
    assertThat(processDefinition.getVersion(), is(2));

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count(), is(1L));

    processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId(), is(TENANT_TWO));
    assertThat(processDefinition.getVersion(), is(1));
  }

  public void testQueryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    deploymentForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);
    // one definition for each tenant
    assertThat(query.count(), is(2L));

    Map<String, ProcessDefinition> processDefinitionsForTenant = getProcessDefinitionsForTenant(query.list());
    assertThat(processDefinitionsForTenant.get(TENANT_ONE).getVersion(), is(2));
    assertThat(processDefinitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
  }

  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
    deployment(emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count(), is(1L));

    ProcessDefinition processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId(), is(nullValue()));
    assertThat(processDefinition.getVersion(), is(2));
  }

  public void testQueryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
    deployment(emptyProcess);
    // deploy a third version for tenant one
    deploymentForTenant(TENANT_ONE, emptyProcess);
    deploymentForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count(), is(3L));

    Map<String, ProcessDefinition> processDefinitionsForTenant = getProcessDefinitionsForTenant(query.list());
    assertThat(processDefinitionsForTenant.get(TENANT_ONE).getVersion(), is(3));
    assertThat(processDefinitionsForTenant.get(TENANT_TWO).getVersion(), is(1));
    assertThat(processDefinitionsForTenant.get(null).getVersion(), is(2));
  }

  public void testQueryByNonExistingTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count(), is(0L));
  }

  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createProcessDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  public void testQuerySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(processDefinitions.size(), is(2));
    assertThat(processDefinitions.get(0).getTenantId(), is(TENANT_ONE));
    assertThat(processDefinitions.get(1).getTenantId(), is(TENANT_TWO));
  }

  public void testQuerySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(processDefinitions.size(), is(2));
    assertThat(processDefinitions.get(0).getTenantId(), is(TENANT_TWO));
    assertThat(processDefinitions.get(1).getTenantId(), is(TENANT_ONE));
  }

  protected Map<String, ProcessDefinition> getProcessDefinitionsForTenant(List<ProcessDefinition> processDefinitions) {
    Map<String, ProcessDefinition> definitionsForTenant = new HashMap<String, ProcessDefinition>();

    for (ProcessDefinition definition : processDefinitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    assertThat(query.count(), is(1L));
  }

  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertThat(query.count(), is(2L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(0L));
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeProcessDefinitionsWithoutTenantId().count(), is(2L));
  }

  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertThat(query.count(), is(3L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
    assertThat(query.withoutTenantId().count(), is(1L));
  }

  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    assertThat(query.count(), is(3L));
  }

}
