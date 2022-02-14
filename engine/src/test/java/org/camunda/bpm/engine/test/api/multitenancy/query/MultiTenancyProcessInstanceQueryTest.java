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
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.engine.test.api.multitenancy.StaticTenantIdTestProvider;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyProcessInstanceQueryTest extends PluggableProcessEngineTest {

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    BpmnModelInstance oneTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
    .done();

   testRule.deploy(oneTaskProcess);
    testRule.deployForTenant(TENANT_ONE, oneTaskProcess);
    testRule.deployForTenant(TENANT_TWO, oneTaskProcess);

    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_TWO).execute();
  }

  @Test
  public void testQueryNoTenantIdSet() {
    ProcessInstanceQuery query = runtimeService.
        createProcessInstanceQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByInstancesWithoutTenantId() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    ProcessInstanceQuery query = runtimeService
        .createProcessInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      runtimeService.createProcessInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude instances without tenant id because of database-specific ordering
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(processInstances).hasSize(2);
    assertThat(processInstances.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(processInstances.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude instances without tenant id because of database-specific ordering
    List<ProcessInstance> processInstances = runtimeService.createProcessInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(processInstances).hasSize(2);
    assertThat(processInstances.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(processInstances.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByProcessDefinitionWithoutTenantId() {
    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processDefinitionWithoutTenantId();

    // then
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByProcessDefinitionWithoutTenantId_VaryingProcessInstanceTenantId() {
    // given
    StaticTenantIdTestProvider tenantIdProvider = new StaticTenantIdTestProvider(null);
    processEngineConfiguration.setTenantIdProvider(tenantIdProvider);

    tenantIdProvider.setTenantIdProvider("anotherTenantId");

    runtimeService.createProcessInstanceByKey("testProcess")
      .processDefinitionWithoutTenantId()
      .execute();

    // when
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
      .processDefinitionWithoutTenantId();

    // then
    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
    assertThat(query.tenantIdIn("anotherTenantId").count()).isEqualTo(1L);

    // cleanup
    processEngineConfiguration.setTenantIdProvider(null);
  }

}
