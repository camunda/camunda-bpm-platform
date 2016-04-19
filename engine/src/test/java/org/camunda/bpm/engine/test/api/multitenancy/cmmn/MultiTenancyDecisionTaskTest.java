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
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;

public class MultiTenancyDecisionTaskTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String CMMN_LATEST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTask.cmmn";
  protected static final String CMMN_DEPLOYMENT = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskDeploymentBinding.cmmn";
  protected static final String CMMN_VERSION = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskVersionBinding.cmmn";
  protected static final String CMMN_VERSION_2 = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskVersionBinding_v2.cmmn";
  protected static final String CMMN_CONST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskTenantIdConst.cmmn";
  protected static final String CMMN_WITHOUT_TENANT = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskWithoutTenantId.cmmn";
  protected static final String CMMN_EXPR = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskTenantIdExpr.cmmn";

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";
  protected static final String DMN_FILE_VERSION_TWO = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable_v2.dmn";

  protected static final String CASE_DEFINITION_KEY = "caseDecisionTask";
  protected static final String DECISION_TASK_ID = "PI_DecisionTask_1";

  protected static final String RESULT_OF_VERSION_ONE = "A";
  protected static final String RESULT_OF_VERSION_TWO = "C";

  public void testEvaluateDecisionWithDeploymentBinding() {
    deploymentForTenant(TENANT_ONE, CMMN_DEPLOYMENT, DMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_DEPLOYMENT, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstanceOne = createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);
    CaseInstance caseInstanceTwo = createCaseInstance(CASE_DEFINITION_KEY, TENANT_TWO);

    assertThat((String)caseService.getVariable(caseInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)caseService.getVariable(caseInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithLatestBindingSameVersion() {
    deploymentForTenant(TENANT_ONE, CMMN_LATEST, DMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_LATEST, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstanceOne = createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);
    CaseInstance caseInstanceTwo = createCaseInstance(CASE_DEFINITION_KEY, TENANT_TWO);

    assertThat((String)caseService.getVariable(caseInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)caseService.getVariable(caseInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithLatestBindingDifferentVersions() {
    deploymentForTenant(TENANT_ONE, CMMN_LATEST, DMN_FILE);

    deploymentForTenant(TENANT_TWO, CMMN_LATEST, DMN_FILE);
    deploymentForTenant(TENANT_TWO, CMMN_LATEST, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstanceOne = createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);
    CaseInstance caseInstanceTwo = createCaseInstance(CASE_DEFINITION_KEY, TENANT_TWO);

    assertThat((String)caseService.getVariable(caseInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)caseService.getVariable(caseInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testEvaluateDecisionWithVersionBinding() {
    deploymentForTenant(TENANT_ONE, CMMN_VERSION, DMN_FILE);
    deploymentForTenant(TENANT_ONE, DMN_FILE_VERSION_TWO);

    deploymentForTenant(TENANT_TWO, CMMN_VERSION, DMN_FILE_VERSION_TWO);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    CaseInstance caseInstanceOne = createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);
    CaseInstance caseInstanceTwo = createCaseInstance(CASE_DEFINITION_KEY, TENANT_TWO);

    assertThat((String)caseService.getVariable(caseInstanceOne.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
    assertThat((String)caseService.getVariable(caseInstanceTwo.getId(), "decisionVar"), is(RESULT_OF_VERSION_TWO));
  }

  public void testFailEvaluateDecisionFromOtherTenantWithDeploymentBinding() {
    deploymentForTenant(TENANT_ONE, CMMN_DEPLOYMENT);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    try {
      createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no decision definition deployed with key = 'decision'"));
    }
  }

  public void testFailEvaluateDecisionFromOtherTenantWithLatestBinding() {
    deploymentForTenant(TENANT_ONE, CMMN_LATEST);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    try {
      createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no decision definition deployed with key 'decision'"));
    }
  }

  public void testFailEvaluateDecisionFromOtherTenantWithVersionBinding() {
    deploymentForTenant(TENANT_ONE, CMMN_VERSION_2, DMN_FILE);

    deploymentForTenant(TENANT_TWO, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE);

    try {
      createCaseInstance(CASE_DEFINITION_KEY, TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no decision definition deployed with key = 'decision', version = '2' and tenant-id 'tenant1'"));
    }
  }

  public void testEvaluateDecisionRefTenantIdConstant() {
    deployment(CMMN_CONST);
    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstance = createCaseInstance(CASE_DEFINITION_KEY);

    assertThat((String)caseService.getVariable(caseInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionRefWithoutTenantIdConstant() {
    deploymentForTenant(TENANT_ONE, CMMN_WITHOUT_TENANT);
    deployment(DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstance = createCaseInstance(CASE_DEFINITION_KEY);

    assertThat((String)caseService.getVariable(caseInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionRefTenantIdExpression() {
    deployment(CMMN_EXPR);
    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstance = createCaseInstance(CASE_DEFINITION_KEY);

    assertThat((String)caseService.getVariable(caseInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  protected CaseInstance createCaseInstance(String caseDefinitionKey, String tenantId) {
    CaseInstance caseInstance = caseService.withCaseDefinitionByKey(caseDefinitionKey).caseDefinitionTenantId(tenantId).create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(DECISION_TASK_ID).tenantIdIn(tenantId).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).setVariable("status", "gold").manualStart();
    return caseInstance;
  }

  protected CaseInstance createCaseInstance(String caseDefinitionKey) {
    CaseInstance caseInstance = caseService.withCaseDefinitionByKey(caseDefinitionKey).create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(DECISION_TASK_ID).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).setVariable("status", "gold").manualStart();
    return caseInstance;
  }

}
