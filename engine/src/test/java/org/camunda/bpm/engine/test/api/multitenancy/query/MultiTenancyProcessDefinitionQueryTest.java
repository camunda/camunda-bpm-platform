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
package org.camunda.bpm.engine.test.api.multitenancy.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinitionQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyProcessDefinitionQueryTest extends PluggableProcessEngineTest {

  protected static final String PROCESS_DEFINITION_KEY = "process";
  protected static final BpmnModelInstance emptyProcess = Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done();

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    testRule.deploy(emptyProcess);
    testRule.deployForTenant(TENANT_ONE, emptyProcess);
    testRule.deployForTenant(TENANT_TWO, emptyProcess);
  }

  @Test
  public void testQueryNoTenantIdSet() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = repositoryService.
        createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByDefinitionsWithoutTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByKey() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY);
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(3L);

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .withoutTenantId();
    // one definition without tenant id
    assertThat(query.count()).isEqualTo(1L);

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .tenantIdIn(TENANT_ONE);
    // one definition for tenant one
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByLatestNoTenantIdSet() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion();
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(3L);

    Map<String, ProcessDefinition> processDefinitionsForTenant = getProcessDefinitionsForTenant(query.list());
    assertThat(processDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(2);
    assertThat(processDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
    assertThat(processDefinitionsForTenant.get(null).getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithTenantId() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    ProcessDefinition processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(processDefinition.getVersion()).isEqualTo(2);

    query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);

    processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(processDefinition.getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithTenantIds() {
    // deploy a second version for tenant one
    testRule.deployForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);
    // one definition for each tenant
    assertThat(query.count()).isEqualTo(2L);

    Map<String, ProcessDefinition> processDefinitionsForTenant = getProcessDefinitionsForTenant(query.list());
    assertThat(processDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(2);
    assertThat(processDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
  }

  @Test
  public void testQueryByLatestWithoutTenantId() {
    // deploy a second version without tenant id
   testRule.deploy(emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);

    ProcessDefinition processDefinition = query.singleResult();
    assertThat(processDefinition.getTenantId()).isNull();
    assertThat(processDefinition.getVersion()).isEqualTo(2);
  }

  @Test
  public void testQueryByLatestWithTenantIdsIncludeDefinitionsWithoutTenantId() {
    // deploy a second version without tenant id
   testRule.deploy(emptyProcess);
    // deploy a third version for tenant one
    testRule.deployForTenant(TENANT_ONE, emptyProcess);
    testRule.deployForTenant(TENANT_ONE, emptyProcess);

    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(PROCESS_DEFINITION_KEY)
        .latestVersion()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeProcessDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);

    Map<String, ProcessDefinition> processDefinitionsForTenant = getProcessDefinitionsForTenant(query.list());
    assertThat(processDefinitionsForTenant.get(TENANT_ONE).getVersion()).isEqualTo(3);
    assertThat(processDefinitionsForTenant.get(TENANT_TWO).getVersion()).isEqualTo(1);
    assertThat(processDefinitionsForTenant.get(null).getVersion()).isEqualTo(2);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    ProcessDefinitionQuery query = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      repositoryService.createProcessDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(processDefinitions).hasSize(2);
    assertThat(processDefinitions.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(processDefinitions.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude definitions without tenant id because of database-specific ordering
    List<ProcessDefinition> processDefinitions = repositoryService
        .createProcessDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(processDefinitions).hasSize(2);
    assertThat(processDefinitions.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(processDefinitions.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  protected Map<String, ProcessDefinition> getProcessDefinitionsForTenant(List<ProcessDefinition> processDefinitions) {
    Map<String, ProcessDefinition> definitionsForTenant = new HashMap<String, ProcessDefinition>();

    for (ProcessDefinition definition : processDefinitions) {
      definitionsForTenant.put(definition.getTenantId(), definition);
    }
    return definitionsForTenant;
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeProcessDefinitionsWithoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

}
