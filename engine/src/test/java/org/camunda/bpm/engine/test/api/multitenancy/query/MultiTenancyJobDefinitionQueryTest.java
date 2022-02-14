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
import org.camunda.bpm.engine.management.JobDefinition;
import org.camunda.bpm.engine.management.JobDefinitionQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyJobDefinitionQueryTest extends PluggableProcessEngineTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    BpmnModelInstance process = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
        .timerWithDuration("PT1M")
      .userTask()
      .endEvent()
      .done();

   testRule.deploy(process);
    testRule.deployForTenant(TENANT_ONE, process);
    testRule.deployForTenant(TENANT_TWO, process);

    // the deployed process definition contains a timer start event
    // - so a job definition is created on deployment.
  }

  @Test
  public void testQueryNoTenantIdSet() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByDefinitionsWithoutTenantIds() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIdsIncludeDefinitionsWithoutTenantId() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE)
        .includeJobDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_TWO)
        .includeJobDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeJobDefinitionsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    JobDefinitionQuery query = managementService
        .createJobDefinitionQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      managementService.createJobDefinitionQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude job definitions without tenant id because of database-specific ordering
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(jobDefinitions).hasSize(2);
    assertThat(jobDefinitions.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(jobDefinitions.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude job definitions without tenant id because of database-specific ordering
    List<JobDefinition> jobDefinitions = managementService.createJobDefinitionQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(jobDefinitions).hasSize(2);
    assertThat(jobDefinitions.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(jobDefinitions.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeJobDefinitionsWithoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    JobDefinitionQuery query = managementService.createJobDefinitionQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

}
