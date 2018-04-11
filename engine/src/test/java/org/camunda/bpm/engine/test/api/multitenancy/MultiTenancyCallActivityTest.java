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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.test.PluggableProcessEngineTestCase;
import org.camunda.bpm.engine.repository.CaseDefinition;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.camunda.bpm.engine.runtime.CaseInstanceQuery;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;

public class MultiTenancyCallActivityTest extends PluggableProcessEngineTestCase {

  protected static final String TENANT_ONE = "tenant1";
  protected static final String TENANT_TWO = "tenant2";

  protected static final String CMMN = "org/camunda/bpm/engine/test/cmmn/deployment/CmmnDeploymentTest.testSimpleDeployment.cmmn";

  protected static final BpmnModelInstance SUB_PROCESS = Bpmn.createExecutableProcess("subProcess")
      .startEvent()
      .userTask()
      .endEvent()
      .done();

  public void testStartProcessInstanceWithDeploymentBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("deployment")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess, SUB_PROCESS);
    deploymentForTenant(TENANT_TWO, callingProcess, SUB_PROCESS);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartProcessInstanceWithLatestBindingSameVersion() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("latest")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess, SUB_PROCESS);
    deploymentForTenant(TENANT_TWO, callingProcess, SUB_PROCESS);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartProcessInstanceWithLatestBindingDifferentVersion() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("latest")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess, SUB_PROCESS);

    deploymentForTenant(TENANT_TWO, callingProcess, SUB_PROCESS);
    deploymentForTenant(TENANT_TWO, SUB_PROCESS);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    ProcessDefinition latestSubProcessTenantTwo = repositoryService.createProcessDefinitionQuery()
        .tenantIdIn(TENANT_TWO).processDefinitionKey("subProcess").latestVersion().singleResult();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).processDefinitionId(latestSubProcessTenantTwo.getId()).count(), is(1L));
  }

  public void testStartProcessInstanceWithVersionBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("version")
        .camundaCalledElementVersion("1")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess, SUB_PROCESS);
    deploymentForTenant(TENANT_TWO, callingProcess, SUB_PROCESS);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartProcessInstanceWithVersionTagBinding() {
    // given
    BpmnModelInstance callingProcess = createCallingProcess("callingProcess", "ver_tag_1");

    deploymentForTenant(TENANT_ONE, callingProcess);
    deploymentForTenant(TENANT_ONE, "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag.bpmn20.xml");
    deploymentForTenant(TENANT_TWO, callingProcess);
    deploymentForTenant(TENANT_TWO, "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag2.bpmn20.xml");

    // when
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    // then
    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.activityIdIn("Task_1").tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.activityIdIn("Task_2").tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testFailStartProcessInstanceFromOtherTenantWithDeploymentBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("deployment")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess);
    deploymentForTenant(TENANT_TWO, SUB_PROCESS);

    try {
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no processes deployed with key = 'subProcess'"));
    }
  }

  public void testFailStartProcessInstanceFromOtherTenantWithLatestBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("latest")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess);
    deploymentForTenant(TENANT_TWO, SUB_PROCESS);

    try {
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no processes deployed with key 'subProcess'"));
    }
  }

  public void testFailStartProcessInstanceFromOtherTenantWithVersionBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .calledElement("subProcess")
        .camundaCalledElementBinding("version")
        .camundaCalledElementVersion("2")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, callingProcess, SUB_PROCESS);

    deploymentForTenant(TENANT_TWO, SUB_PROCESS);
    deploymentForTenant(TENANT_TWO, SUB_PROCESS);

    try {
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no processes deployed with key = 'subProcess'"));
    }
  }

  public void testFailStartProcessInstanceFromOtherTenantWithVersionTagBinding() {
    // given
    BpmnModelInstance callingProcess = createCallingProcess("callingProcess", "ver_tag_2");
    deploymentForTenant(TENANT_ONE, callingProcess);
    deploymentForTenant(TENANT_TWO, "org/camunda/bpm/engine/test/bpmn/callactivity/subProcessWithVersionTag2.bpmn20.xml");

    try {
      // when
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();
      fail("expected exception");
    } catch (ProcessEngineException e) {
      // then
      assertThat(e.getMessage(), containsString("no processes deployed with key = 'subProcess'"));
    }
  }

  public void testStartCaseInstanceWithDeploymentBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .camundaCaseRef("Case_1")
        .camundaCaseBinding("deployment")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, CMMN, callingProcess);
    deploymentForTenant(TENANT_TWO, CMMN, callingProcess);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartCaseInstanceWithLatestBindingSameVersion() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .camundaCaseRef("Case_1")
        .camundaCaseBinding("latest")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, CMMN, callingProcess);
    deploymentForTenant(TENANT_TWO, CMMN, callingProcess);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testStartCaseInstanceWithLatestBindingDifferentVersion() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity()
          .camundaCaseRef("Case_1")
          .camundaCaseBinding("latest")
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, CMMN, callingProcess);

    deploymentForTenant(TENANT_TWO, CMMN, callingProcess);
    deploymentForTenant(TENANT_TWO, CMMN);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));

    CaseDefinition latestCaseDefinitionTenantTwo = repositoryService.createCaseDefinitionQuery().tenantIdIn(TENANT_TWO).latestVersion().singleResult();
    query = caseService.createCaseInstanceQuery().caseDefinitionId(latestCaseDefinitionTenantTwo.getId());
    assertThat(query.count(), is(1L));
  }

  public void testStartCaseInstanceWithVersionBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .camundaCaseRef("Case_1")
        .camundaCaseBinding("version")
        .camundaCaseVersion("1")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, CMMN, callingProcess);
    deploymentForTenant(TENANT_TWO, CMMN, callingProcess);

    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_ONE).execute();
    runtimeService.createProcessInstanceByKey("callingProcess").processDefinitionTenantId(TENANT_TWO).execute();

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
    assertThat(query.tenantIdIn(TENANT_TWO).count(), is(1L));
  }

  public void testFailStartCaseInstanceFromOtherTenantWithDeploymentBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity()
          .camundaCaseRef("Case_1")
          .camundaCaseBinding("deployment")
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, callingProcess);
    deploymentForTenant(TENANT_TWO, CMMN);

    try {
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key = 'Case_1'"));
    }
  }

  public void testFailStartCaseInstanceFromOtherTenantWithLatestBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity()
          .camundaCaseRef("Case_1")
          .camundaCaseBinding("latest")
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, callingProcess);
    deploymentForTenant(TENANT_TWO, CMMN);

    try {
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key 'Case_1'"));
    }
  }

  public void testFailStartCaseInstanceFromOtherTenantWithVersionBinding() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity()
          .camundaCaseRef("Case_1")
          .camundaCaseBinding("version")
          .camundaCaseVersion("2")
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, CMMN, callingProcess);

    deploymentForTenant(TENANT_TWO, CMMN);
    deploymentForTenant(TENANT_TWO, CMMN);

    try {
      runtimeService.createProcessInstanceByKey("callingProcess")
        .processDefinitionTenantId(TENANT_ONE)
        .execute();

      fail("expected exception");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage(), containsString("no case definition deployed with key = 'Case_1'"));
    }
  }

  public void testCalledElementTenantIdConstant() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity()
          .calledElement("subProcess")
          .camundaCalledElementTenantId(TENANT_ONE)
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, SUB_PROCESS);
    deployment(callingProcess);

    runtimeService.startProcessInstanceByKey("callingProcess");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCalledElementTenantIdExpression() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
        .startEvent()
        .callActivity()
          .calledElement("subProcess")
          .camundaCalledElementTenantId("${'"+TENANT_ONE+"'}")
        .endEvent()
        .done();

    deploymentForTenant(TENANT_ONE, SUB_PROCESS);
    deployment(callingProcess);

    runtimeService.startProcessInstanceByKey("callingProcess");

    ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery().processDefinitionKey("subProcess");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCaseRefTenantIdConstant() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .camundaCaseRef("Case_1")
        .camundaCaseTenantId(TENANT_ONE)
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, CMMN);
    deployment(callingProcess);

    runtimeService.startProcessInstanceByKey("callingProcess");

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));

  }

  public void testCaseRefTenantIdExpression() {

    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
        .camundaCaseRef("Case_1")
        .camundaCaseTenantId("${'"+TENANT_ONE+"'}")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, CMMN);
    deployment(callingProcess);

    runtimeService.startProcessInstanceByKey("callingProcess");

    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  public void testCaseRefTenantIdCompositeExpression() {
    // given
    BpmnModelInstance callingProcess = Bpmn.createExecutableProcess("callingProcess")
      .startEvent()
      .callActivity()
      .camundaCaseRef("Case_1")
      .camundaCaseTenantId("tenant${'1'}")
      .endEvent()
      .done();

    deploymentForTenant(TENANT_ONE, CMMN);
    deployment(callingProcess);

    // when
    runtimeService.startProcessInstanceByKey("callingProcess");

    // then
    CaseInstanceQuery query = caseService.createCaseInstanceQuery().caseDefinitionKey("Case_1");
    assertThat(query.tenantIdIn(TENANT_ONE).count(), is(1L));
  }

  protected BpmnModelInstance createCallingProcess(String processId, String versionTagValue) {
    return Bpmn.createExecutableProcess(processId)
        .startEvent()
        .callActivity()
          .calledElement("subProcess")
          .camundaCalledElementBinding("versionTag")
          .camundaCalledElementVersionTag(versionTagValue)
        .endEvent()
        .done();
  }

}
