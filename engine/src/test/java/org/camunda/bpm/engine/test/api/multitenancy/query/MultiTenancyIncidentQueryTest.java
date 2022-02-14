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
import org.camunda.bpm.engine.runtime.Incident;
import org.camunda.bpm.engine.runtime.IncidentQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyIncidentQueryTest extends PluggableProcessEngineTest {

  protected static final BpmnModelInstance BPMN = Bpmn.createExecutableProcess("failingProcess")
      .startEvent()
      .serviceTask()
        .camundaExpression("${failing}")
        .camundaAsyncBefore()
      .endEvent()
      .done();

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    testRule.deployForTenant(TENANT_ONE, BPMN);
    testRule.deployForTenant(TENANT_TWO, BPMN);

    startProcessInstanceAndExecuteFailingJobForTenant(TENANT_ONE);
    startProcessInstanceAndExecuteFailingJobForTenant(TENANT_TWO);
  }

  @Test
  public void testQueryWithoutTenantId() {
    IncidentQuery query = runtimeService
        .createIncidentQuery();

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByTenantId() {
    IncidentQuery query = runtimeService
        .createIncidentQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = runtimeService
        .createIncidentQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    IncidentQuery query = runtimeService
        .createIncidentQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    IncidentQuery query = runtimeService
        .createIncidentQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      runtimeService.createIncidentQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    List<Incident> incidents = runtimeService.createIncidentQuery()
        .orderByTenantId()
        .asc()
        .list();

    assertThat(incidents).hasSize(2);
    assertThat(incidents.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(incidents.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    List<Incident> incidents = runtimeService.createIncidentQuery()
        .orderByTenantId()
        .desc()
        .list();

    assertThat(incidents).hasSize(2);
    assertThat(incidents.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(incidents.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    IncidentQuery query = runtimeService.createIncidentQuery();
    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    IncidentQuery query = runtimeService.createIncidentQuery();

    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    IncidentQuery query = runtimeService.createIncidentQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    IncidentQuery query = runtimeService.createIncidentQuery();
    assertThat(query.count()).isEqualTo(2L);
  }

  protected void startProcessInstanceAndExecuteFailingJobForTenant(String tenant) {
    runtimeService.createProcessInstanceByKey("failingProcess").processDefinitionTenantId(tenant).execute();

    testRule.executeAvailableJobs();
  }

}
