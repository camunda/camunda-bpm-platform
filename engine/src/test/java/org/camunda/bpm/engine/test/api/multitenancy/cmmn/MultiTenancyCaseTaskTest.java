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
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;

public class MultiTenancyCaseTaskTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String CMMN_LATEST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTask.cmmn";
  protected static final String CMMN_LATEST_WITH_MANUAL_ACTIVATION = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskWithManualActivation.cmmn";
  protected static final String CMMN_DEPLOYMENT = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskDeploymentBinding.cmmn";
  protected static final String CMMN_VERSION = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskVersionBinding.cmmn";
  protected static final String CMMN_VERSION_2 = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskVersionBinding_v2.cmmn";

  protected static final String CMMN_TENANT_CONST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskTenantIdConst.cmmn";
  protected static final String CMMN_TENANT_EXPR = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithCaseTaskTenantIdExpr.cmmn";

  protected static final String CMMN_CASE = "org/camunda/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";

  protected static final String CASE_TASK_ID = "PI_CaseTask_1";

  public void testStartCaseInstanceWithDeploymentBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_DEPLOYMENT, CMMN_CASE);
    deploymentForTenant(TENANT_TWO, CMMN_DEPLOYMENT, CMMN_CASE);

    createCaseInstance("caseTaskCaseDeployment", TENANT_ONE);
    createCaseInstance("caseTaskCaseDeployment", TENANT_TWO);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartCaseInstanceWithLatestBindingSameVersion() {

    deploymentForTenant(TENANT_ONE, CMMN_LATEST_WITH_MANUAL_ACTIVATION, CMMN_CASE);
    deploymentForTenant(TENANT_TWO, CMMN_LATEST_WITH_MANUAL_ACTIVATION, CMMN_CASE);

    createCaseInstance("caseTaskCase", TENANT_ONE);
    createCaseInstance("caseTaskCase", TENANT_TWO);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartCaseInstanceWithLatestBindingDifferentVersion() {

    deploymentForTenant(TENANT_ONE, CMMN_LATEST_WITH_MANUAL_ACTIVATION, CMMN_CASE);

    deploymentForTenant(TENANT_TWO, CMMN_LATEST_WITH_MANUAL_ACTIVATION, CMMN_CASE);
    deploymentForTenant(TENANT_TWO, CMMN_CASE);

    createCaseInstance("caseTaskCase", TENANT_ONE);
    createCaseInstance("caseTaskCase", TENANT_TWO);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));

    CaseDefinition latestCaseDefinitionTenantTwo = repositoryService.createCaseDefinitionQuery().
        caseDefinitionKey("oneTaskCase").tenantIdIn(TENANT_TWO).latestVersion().singleResult();
    query = caseService.createCaseInstanceQuery().caseDefinitionId(latestCaseDefinitionTenantTwo.getId());
    assertThat(query.count(), is(1L));
  }

  public void testStartCaseInstanceWithVersionBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_VERSION, CMMN_CASE);
    deploymentForTenant(TENANT_TWO, CMMN_VERSION, CMMN_CASE);

    createCaseInstance("caseTaskCaseVersion", TENANT_ONE);
    createCaseInstance("caseTaskCaseVersion", TENANT_TWO);

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testFailStartCaseInstanceFromOtherTenantWithDeploymentBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_DEPLOYMENT);
    deploymentForTenant(TENANT_TWO, CMMN_CASE);

    try {
      createCaseInstance("caseTaskCaseDeployment", TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key = 'oneTaskCase'"));
    }
  }

  public void testFailStartCaseInstanceFromOtherTenantWithLatestBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_LATEST);
    deploymentForTenant(TENANT_TWO, CMMN_CASE);

    try {
      createCaseInstance("caseTaskCase", TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key 'oneTaskCase'"));
    }
  }

  public void testFailStartCaseInstanceFromOtherTenantWithVersionBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_VERSION_2, CMMN_CASE);

    deploymentForTenant(TENANT_TWO, CMMN_CASE);
    deploymentForTenant(TENANT_TWO, CMMN_CASE);

    try {
      createCaseInstance("caseTaskCaseVersion", TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key = 'oneTaskCase'"));
    }
  }

  public void testCaseRefTenantIdConstant() {
    deployment(CMMN_TENANT_CONST);
    deploymentForTenant(TENANT_ONE, CMMN_CASE);

    caseService.withCaseDefinitionByKey("caseTaskCase").create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCaseRefTenantIdExpression() {
    deployment(CMMN_TENANT_EXPR);
    deploymentForTenant(TENANT_ONE, CMMN_CASE);

    caseService.withCaseDefinitionByKey("caseTaskCase").create();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("oneTaskCase");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  protected void createCaseInstance(String caseDefinitionKey, String tenantId) {
    caseService.withCaseDefinitionByKey(caseDefinitionKey).caseDefinitionTenantId(tenantId).create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(CASE_TASK_ID).tenantIdIn(tenantId).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();
  }

}
