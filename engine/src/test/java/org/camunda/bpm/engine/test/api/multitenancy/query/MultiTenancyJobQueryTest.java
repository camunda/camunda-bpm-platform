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
import org.camunda.bpm.engine.runtime.Job;
import org.camunda.bpm.engine.runtime.JobQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyJobQueryTest extends PluggableProcessEngineTest {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    BpmnModelInstance asyncTaskProcess = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
        .camundaAsyncBefore()
      .endEvent()
    .done();

   testRule.deploy(asyncTaskProcess);
    testRule.deployForTenant(TENANT_ONE, asyncTaskProcess);
    testRule.deployForTenant(TENANT_TWO, asyncTaskProcess);

    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionWithoutTenantId().execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("testProcess").processDefinitionTenantId(TENANT_TWO).execute();
  }

  @Test
  public void testQueryNoTenantIdSet() {
    JobQuery query = managementService
        .createJobQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByJobsWithoutTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIdsIncludeJobsWithoutTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE)
        .includeJobsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_TWO)
        .includeJobsWithoutTenantId();

    assertThat(query.count()).isEqualTo(2L);

    query = managementService
        .createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .includeJobsWithoutTenantId();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    JobQuery query = managementService
        .createJobQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      managementService.createJobQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude jobs without tenant id because of database-specific ordering
    List<Job> jobs = managementService.createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(jobs).hasSize(2);
    assertThat(jobs.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(jobs.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude jobs without tenant id because of database-specific ordering
    List<Job> jobs = managementService.createJobQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(jobs).hasSize(2);
    assertThat(jobs.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(jobs.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    JobQuery query = managementService.createJobQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    JobQuery query = managementService.createJobQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).includeJobsWithoutTenantId().count()).isEqualTo(2L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    JobQuery query = managementService.createJobQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    JobQuery query = managementService.createJobQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

}
