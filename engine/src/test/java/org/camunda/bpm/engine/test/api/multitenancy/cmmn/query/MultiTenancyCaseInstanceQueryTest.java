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
package org.camunda.bpm.engine.test.api.multitenancy.cmmn.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.repository.CaseDefinitionQuery;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Before;
import org.junit.Test;

public class MultiTenancyCaseInstanceQueryTest extends PluggableProcessEngineTest {

  protected final static String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected final static String TENANT_ONE = "tenant1";
  protected final static String TENANT_TWO = "tenant2";

  @Before
  public void setUp() {
    testRule.deploy(CMMN_FILE);
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    createCaseInstance(null);
    createCaseInstance(TENANT_ONE);
    createCaseInstance(TENANT_TWO);
  }

  @Test
  public void testQueryNoTenantIdSet() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery();

    assertThat(query.count()).isEqualTo(3L);
  }

  @Test
  public void testQueryByTenantId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE);

    assertThat(query.count()).isEqualTo(1L);

    query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn(TENANT_TWO);

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByTenantIds() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO);

    assertThat(query.count()).isEqualTo(2L);
  }

  @Test
  public void testQueryByInstancesWithoutTenantId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .withoutTenantId();

    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryByNonExistingTenantId() {
    CaseInstanceQuery query = caseService
        .createCaseInstanceQuery()
        .tenantIdIn("nonExisting");

    assertThat(query.count()).isEqualTo(0L);
  }

  @Test
  public void testFailQueryByTenantIdNull() {
    try {
      caseService.createCaseInstanceQuery()
        .tenantIdIn((String) null);

      fail("expected exception");
    } catch (NullValueException e) {
    }
  }

  @Test
  public void testQuerySortingAsc() {
    // exclude case instances without tenant id because of database-specific ordering
    List<CaseInstance> caseInstances = caseService.createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .asc()
        .list();

    assertThat(caseInstances).hasSize(2);
    assertThat(caseInstances.get(0).getTenantId()).isEqualTo(TENANT_ONE);
    assertThat(caseInstances.get(1).getTenantId()).isEqualTo(TENANT_TWO);
  }

  @Test
  public void testQuerySortingDesc() {
    // exclude case instances without tenant id because of database-specific ordering
    List<CaseInstance> caseInstances = caseService.createCaseInstanceQuery()
        .tenantIdIn(TENANT_ONE, TENANT_TWO)
        .orderByTenantId()
        .desc()
        .list();

    assertThat(caseInstances).hasSize(2);
    assertThat(caseInstances.get(0).getTenantId()).isEqualTo(TENANT_TWO);
    assertThat(caseInstances.get(1).getTenantId()).isEqualTo(TENANT_ONE);
  }

  @Test
  public void testQueryNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    assertThat(query.count()).isEqualTo(2L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(0L);
    assertThat(query.tenantIdIn(TENANT_ONE, TENANT_TWO).count()).isEqualTo(1L);
  }

  @Test
  public void testQueryAuthenticatedTenants() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE, TENANT_TWO));

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();

    assertThat(query.count()).isEqualTo(3L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_TWO).count()).isEqualTo(1L);
    assertThat(query.withoutTenantId().count()).isEqualTo(1L);
  }

  @Test
  public void testQueryDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(3L);
  }

  protected CaseInstance createCaseInstance(String tenantId) {
    String caseDefinitionId = null;

    CaseDefinitionQuery caseDefinitionQuery = repositoryService.createCaseDefinitionQuery().caseDefinitionKey("oneTaskCase");
    if (tenantId == null) {
      caseDefinitionId = caseDefinitionQuery.withoutTenantId().singleResult().getId();
    } else {
      caseDefinitionId = caseDefinitionQuery.tenantIdIn(tenantId).singleResult().getId();
    }

    return caseService.withCaseDefinition(caseDefinitionId).create();
  }

}
