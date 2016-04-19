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

package org.camunda.bpm.engine.test.api.multitenancy.cmmn;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Arrays;

import org.camunda.bpm.engine.BadUserRequestException;
import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

public class MultiTenancyCreateCaseInstanceTest extends PluggableProcessEngineTestCase {

  protected static final String CMMN_FILE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected static final String CASE_DEFINITION_KEY = "oneTaskCase";

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  public void testFailToCreateCaseInstanceByIdWithoutTenantId() {
    deployment(CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    try {
      caseService.withCaseDefinition(caseDefinition.getId())
          .caseDefinitionWithoutTenantId()
          .create();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

  public void testFailToCreateCaseInstanceByIdWithTenantId() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();

    try {
      caseService.withCaseDefinition(caseDefinition.getId())
          .caseDefinitionTenantId(TENANT_ONE)
          .create();
      fail("BadUserRequestException exception");
    } catch(BadUserRequestException e) {
      assertThat(e.getMessage(), containsString("Cannot specify a tenant-id"));
    }
  }

  public void testFailToCreateCaseInstanceByKeyForNonExistingTenantID() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
          .caseDefinitionTenantId("nonExistingTenantId")
          .create();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key 'oneTaskCase' and tenant-id 'nonExistingTenantId'"));
    }
  }

  public void testFailToCreateCaseInstanceByKeyForMultipleTenants() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
          .create();
      fail("ProcessEngineException expected");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("multiple tenants."));
    }
  }

  public void testCreateCaseInstanceByKeyWithoutTenantId() {
    deployment(CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .caseDefinitionWithoutTenantId()
        .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.singleResult().getTenantId(), is(nullValue()));

  }

  public void testCreateCaseInstanceByKeyForAnyTenants() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .create();

    assertThat(caseService.createCaseInstanceQuery().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCreateCaseInstanceByKeyAndTenantId() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .caseDefinitionTenantId(TENANT_ONE)
        .create();

    assertThat(caseService.createCaseInstanceQuery().tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCreateCaseInstanceByKeyWithoutTenantIdNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    deployment(CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .caseDefinitionWithoutTenantId()
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
  }

  public void testFailToCreateCaseInstanceByKeyNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key 'oneTaskCase'"));
    }
  }

  public void testFailToCreateCaseInstanceByKeyWithTenantIdNoAuthenticatedTenants() {
    identityService.setAuthentication("user", null, null);

    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    try {
      caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
        .caseDefinitionTenantId(TENANT_ONE)
        .create();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot create an instance of the case definition"));
    }
  }

  public void testFailToCreateCaseInstanceByIdNoAuthenticatedTenants() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService
      .createCaseDefinitionQuery()
      .singleResult();

    identityService.setAuthentication("user", null, null);

    try {
      caseService.withCaseDefinition(caseDefinition.getId()).create();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("Cannot create an instance of the case definition"));
    }
  }

  public void testCreateCaseInstanceByKeyWithTenantIdAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .caseDefinitionTenantId(TENANT_ONE)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCreateCaseInstanceByIdAuthenticatedTenant() {
    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService
        .createCaseDefinitionQuery()
        .singleResult();

    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    caseService.withCaseDefinition(caseDefinition.getId()).create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCreateCaseInstanceByKeyWithAuthenticatedTenant() {
    identityService.setAuthentication("user", null, Arrays.asList(TENANT_ONE));

    deploymentForTenant(TENANT_ONE, CMMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_FILE);

    caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCreateCaseInstanceByKeyWithTenantIdDisabledTenantCheck() {
    processEngineConfiguration.setTenantCheckEnabled(false);
    identityService.setAuthentication("user", null, null);

    deploymentForTenant(TENANT_ONE, CMMN_FILE);

    caseService
      .withCaseDefinitionByKey(CASE_DEFINITION_KEY)
      .caseDefinitionTenantId(TENANT_ONE)
      .create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery();
    assertThat(query.count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

}
