
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
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseExecution;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyProcessTaskTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String CMMN_LATEST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTask.cmmn";
  protected static final String CMMN_LATEST_WITH_MANUAL_ACTIVATION = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTaskWithManualActivation.cmmn";
  protected static final String CMMN_DEPLOYMENT = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTaskDeploymentBinding.cmmn";
  protected static final String CMMN_VERSION = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTaskVersionBinding.cmmn";
  protected static final String CMMN_VERSION_2 = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTaskVersionBinding_v2.cmmn";

  protected static final String CMMN_TENANT_CONST = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTaskTenantIdConst.cmmn";
  protected static final String CMMN_TENANT_EXPR = "org/camunda/bpm/engine/test/api/multitenancy/CaseWithProcessTaskTenantIdExpr.cmmn";

  protected static final String PROCESS_TASK_ID = "PI_ProcessTask_1";

  protected static final BpmnModelInstance PROCESS = Bpmn.createExecutableProcess("testProcess")
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  public void testStartProcessInstanceWithDeploymentBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_DEPLOYMENT, PROCESS);
    deploymentForTenant(TENANT_TWO, CMMN_DEPLOYMENT, PROCESS);

    createCaseInstance("testCaseDeployment", TENANT_ONE);
    createCaseInstance("testCaseDeployment", TENANT_TWO);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("testProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartProcessInstanceWithLatestBindingSameVersion() {

    deploymentForTenant(TENANT_ONE, CMMN_LATEST_WITH_MANUAL_ACTIVATION, PROCESS);
    deploymentForTenant(TENANT_TWO, CMMN_LATEST_WITH_MANUAL_ACTIVATION, PROCESS);

    createCaseInstance("testCase", TENANT_ONE);
    createCaseInstance("testCase", TENANT_TWO);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("testProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartProcessInstanceWithLatestBindingDifferentVersion() {

    deploymentForTenant(TENANT_ONE, CMMN_LATEST_WITH_MANUAL_ACTIVATION, PROCESS);

    deploymentForTenant(TENANT_TWO, CMMN_LATEST_WITH_MANUAL_ACTIVATION, PROCESS);
    deploymentForTenant(TENANT_TWO, PROCESS);

    createCaseInstance("testCase", TENANT_ONE);
    createCaseInstance("testCase", TENANT_TWO);

    ProcessDefinition latestProcessTenantTwo = repositoryService.createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO).processDefinitionKey("testProcess").latestVersion().singleResult();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("testProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).processDefinitionId(latestProcessTenantTwo.getId()).count(), is(1L));
  }

  public void testStartProcessInstanceWithVersionBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_VERSION, PROCESS);
    deploymentForTenant(TENANT_TWO, CMMN_VERSION, PROCESS);

    createCaseInstance("testCaseVersion", TENANT_ONE);
    createCaseInstance("testCaseVersion", TENANT_TWO);

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("testProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testFailStartProcessInstanceFromOtherTenantWithDeploymentBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_DEPLOYMENT);
    deploymentForTenant(TENANT_TWO, PROCESS);

    try {
      createCaseInstance("testCaseDeployment", TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no processes deployed with key = 'testProcess'"));
    }
  }

  public void testFailStartProcessInstanceFromOtherTenantWithLatestBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_LATEST);
    deploymentForTenant(TENANT_TWO, PROCESS);

    try {
      createCaseInstance("testCase", TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no processes deployed with key 'testProcess'"));
    }
  }

  public void testFailStartProcessInstanceFromOtherTenantWithVersionBinding() {

    deploymentForTenant(TENANT_ONE, CMMN_VERSION_2, PROCESS);

    deploymentForTenant(TENANT_TWO, PROCESS);
    deploymentForTenant(TENANT_TWO, PROCESS);

    try {
      createCaseInstance("testCaseVersion", TENANT_ONE);

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no processes deployed with key = 'testProcess'"));
    }
  }

  public void testProcessRefTenantIdConstant() {
    deployment(CMMN_TENANT_CONST);
    deploymentForTenant(TENANT_ONE, PROCESS);

    caseService.withCaseDefinitionByKey("testCase").create();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("testProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testProcessRefTenantIdExpression() {
    deployment(CMMN_TENANT_EXPR);
    deploymentForTenant(TENANT_ONE, PROCESS);

    caseService.withCaseDefinitionByKey("testCase").create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(PROCESS_TASK_ID).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("testProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  protected void createCaseInstance(String caseDefinitionKey, String tenantId) {
    caseService.withCaseDefinitionByKey(caseDefinitionKey).caseDefinitionTenantId(tenantId).create();

    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId(PROCESS_TASK_ID).tenantIdIn(tenantId).singleResult();
    caseService.withCaseExecution(caseExecution.getId()).manualStart();
  }

}
