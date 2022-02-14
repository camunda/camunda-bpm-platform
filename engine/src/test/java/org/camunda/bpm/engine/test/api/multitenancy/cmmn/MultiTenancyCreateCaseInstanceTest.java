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
package org.camunda.bpm.engine.test.api.multitenancy.cmmn;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.fail;

import java.util.Arrays;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.test.util.PluggableProcessEngineTest;
import org.junit.Test;

public class MultiTenancyCreateCaseInstanceTest extends PluggableProcessEngineTest {

  protected static final String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected static final String CASE_DEFINITION_KEY = "oneTaskCase";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  @Test
  public void testFailToCreateCaseInstanceByIdWithoutTenantId() {
   testRule.deploy(CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    try {
      caseService.withCaseDefinition(caseDefinition.getId())
          .caseDefinitionWithoutTenantId()
          .create();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot specify a tenant-id");
    }
  }

  @Test
  public void testFailToCreateCaseInstanceByIdWithTenantId() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    try {
      caseService.withCaseDefinition(caseDefinition.getId())
          .caseDefinitionTenantId(TENANT_ONE)
          .create();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage()).contains("Cannot specify a tenant-id");
    }
  }

  @Test
  public void testFailToCreateCaseInstanceByKeyForNonExistingTenantID() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
          .caseDefinitionTenantId("nonExistingTenantId")
          .create();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("no case definition deployed with key 'oneTaskCase' and tenant-id 'nonExistingTenantId'");
    }
  }

  @Test
  public void testFailToCreateCaseInstanceByKeyForMultipleTenants() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
          .create();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("multiple tenants.");
    }
  }

  @Test
  public void testCreateCaseInstanceByKeyWithoutTenantId() {
   testRule.deploy(CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .caseDefinitionWithoutTenantId()
        .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.singleResult().getTenantId()).isNull();

  }

  @Test
  public void testCreateCaseInstanceByKeyForAnyTenants() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .create();

    assertThat(caseService.createCaseInstanceQuery().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void testCreateCaseInstanceByKeyAndTenantId() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .caseDefinitionTenantId(TENANT_ONE)
        .create();

    assertThat(caseService.createCaseInstanceQuery().tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void testCreateCaseInstanceByKeyWithoutTenantIdNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

   testRule.deploy(CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .caseDefinitionWithoutTenantId()
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
  }

  @Test
  public void testFailToCreateCaseInstanceByKeyNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("no case definition deployed with key 'oneTaskCase'");
    }
  }

  @Test
  public void testFailToCreateCaseInstanceByKeyWithTenantIdNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .caseDefinitionTenantId(TENANT_ONE)
        .create();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Cannot create an instance of the case definition");
    }
  }

  @Test
  public void testFailToCreateCaseInstanceByIdNoAuthenticatedTenants() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService
      .createCaseDefinitionQuery()
      .singleResult();

    identityService.setAuthentication("user", null, null);

    try {
      caseService.withCaseDefinition(caseDefinition.getId()).create();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Cannot create an instance of the case definition");
    }
  }

  @Test
  public void testCreateCaseInstanceByKeyWithTenantIdAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .caseDefinitionTenantId(TENANT_ONE)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void testCreateCaseInstanceByIdAuthenticatedTenant() {
    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .singleResult();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.withCaseDefinition(caseDefinition.getId()).create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void testCreateCaseInstanceByKeyWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);
    testRule.deployForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

  @Test
  public void testCreateCaseInstanceByKeyWithTenantIdDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    testRule.deployForTenant(TENANT_ONE, CMMN_FILE);

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .caseDefinitionTenantId(TENANT_ONE)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count()).isEqualTo(1L);
    assertThat(query.tenantIdIn(TENANT_ONE).count()).isEqualTo(1L);
  }

}
