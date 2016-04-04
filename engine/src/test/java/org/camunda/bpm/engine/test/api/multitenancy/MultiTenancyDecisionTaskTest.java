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

package org.camunda.bpm.engine.test.api.multitenancy;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.CaseInstance;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyDecisionTaskTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String CMMN_CONST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskTenantIdConst.cmmn";
  protected static final String CMMN_WITHOUT_TENANT = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskWithoutTenantId.cmmn";
  protected static final String CMMN_EXPR = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithDecisionTaskTenantIdExpr.cmmn";

  protected static final String DMN_FILE = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable.dmn";
  protected static final String DMN_FILE_VERSION_TWO = "org/camunda/bpm/engine/test/api/multitenancy/simpleDecisionTable_v2.dmn";

  protected static final String CASE_DEFINITION_KEY = "caseDecisionTask";
  protected static final String DECISION_TASK_ID = "PI_DecisionTask_1";

  protected static final String RESULT_OF_VERSION_ONE = "A";
  protected static final String RESULT_OF_VERSION_TWO = "C";

  public void testEvaluateDecisionRefTenantIdConstant() {
    deployment(CMMN_CONST);
    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstance = caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariable("status", "gold").create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(DECISION_TASK_ID).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();

    assertThat((String)caseService.getVariable(caseInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionRefWithoutTenantIdConstant() {
    deploymentForTenant(TENANT_ONE, CMMN_WITHOUT_TENANT);
    deployment(DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstance = caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariable("status", "gold").create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(DECISION_TASK_ID).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();

    assertThat((String)caseService.getVariable(caseInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  public void testEvaluateDecisionRefTenantIdExpression() {
    deployment(CMMN_EXPR);
    deploymentForTenant(TENANT_ONE, DMN_FILE);
    deploymentForTenant(TENANT_TWO, DMN_FILE_VERSION_TWO);

    CaseInstance caseInstance = caseService.withCaseDefinitionByKey(CASE_DEFINITION_KEY).setVariable("status", "gold").create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(DECISION_TASK_ID).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();

    assertThat((String)caseService.getVariable(caseInstance.getId(), "decisionVar"), is(RESULT_OF_VERSION_ONE));
  }

  protected String deploymentForTenant(String tenantId, String classpathResource, BpmnModelInstance modelInstance) {
    return deployment(repositoryService.createDeployment()
        .tenantId(tenantId)
        .addClasspathResource(classpathResource), modelInstance);
  }

}
